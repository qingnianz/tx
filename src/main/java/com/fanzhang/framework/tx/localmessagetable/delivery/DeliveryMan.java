package com.fanzhang.framework.tx.localmessagetable.delivery;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fanzhang.framework.tx.TXProperties;
import com.fanzhang.framework.tx.localmessagetable.delivery.producerconsumer.TxMsgQueueFactory;
import com.fanzhang.framework.tx.localmessagetable.model.CronDeliveryMode;
import com.fanzhang.framework.tx.localmessagetable.model.TransactionLocalMessageStatus;
import com.fanzhang.framework.tx.localmessagetable.model.TransactionLocalMessageTableModel;
import com.fanzhang.framework.tx.localmessagetable.repository.LocalMessageTableMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 投递员
 *
 * @author Zhang Fan
 */
@Slf4j
public final class DeliveryMan {
    private static final String MESSAGE_STATUS = "message_status";
    private static final String DIED = "died";
    private static final String CREAT_TIME = "create_time";
    private static final String ID = "id";
    private static final String LIMIT_1000 = "limit 1000";
    private static final String SELECT_LOCK = " for update ";
    private static final int NOT = 0;

    @Autowired
    private TXProperties txProperties;
    @Autowired
    private LocalMessageTableMapper localMessageTableMapper;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 投递当前消息表中所有待投递的消息
     */
    public void deliveryDatabaseToBeDelivered() {
        transactionTemplate.execute((TransactionCallback<Void>) status -> {
            QueryWrapper<TransactionLocalMessageTableModel> wrapper
                    = new QueryWrapper<TransactionLocalMessageTableModel>()
                    //未投递或投递失败
                    .eq(MESSAGE_STATUS, NOT)
                    //非死信
                    .eq(DIED, NOT)
                    //保持创建顺序
                    .orderByAsc(CREAT_TIME, ID)
                    // 前1000条
                    .last(LIMIT_1000 + SELECT_LOCK);
            List<TransactionLocalMessageTableModel> transactionLocalMessageTableModels = localMessageTableMapper.selectList(wrapper);
            if (transactionLocalMessageTableModels != null && !transactionLocalMessageTableModels.isEmpty()) {
                CronDeliveryMode mode = txProperties.getLocalMessageTable().getCron().getDeliveryMode();
                switch (mode) {
                    case EACH_AFTER_GROUP:
                        Map<String, List<TransactionLocalMessageTableModel>> groupedModels = transactionLocalMessageTableModels.stream().collect(Collectors.groupingBy(TransactionLocalMessageTableModel::getDestination));
                        groupedModels.keySet().parallelStream().forEach(localMessageKey ->
                                groupedModels.get(localMessageKey).forEach(localMessage -> deliveryOne(localMessage, false)));
                        break;
                    case RANDOM_EACH:
                        transactionLocalMessageTableModels.parallelStream().forEach(localMessage -> deliveryOne(localMessage, false));
                        break;
                    default:
                        transactionLocalMessageTableModels.forEach(localMessage -> deliveryOne(localMessage, false));
                }

            }
            return null;
        });
    }

    /**
     * 投递一个
     *
     * @param localMessage                事务本地消息表模型对象
     * @param reenterQueueInCaseOfFailure 失败时重新进入队列
     */
    public void deliveryOne(TransactionLocalMessageTableModel localMessage, boolean reenterQueueInCaseOfFailure) {
        try {
            if (tryLock(localMessage)) {
                log.debug("DeliveryMan processing local message currently: {}", localMessage);

                rocketMQTemplate.asyncSend(
                        localMessage.getDestination(),
                        convert2Message(localMessage.getMessageContent()),
                        reenterQueueInCaseOfFailure ?
                                new ReenteringQueueInCaseOfFailureSendCallback(localMessage)
                                : new DefaultSendCallback(localMessage));
            }
        } catch (Exception e) {
            e.printStackTrace();
            String exceptionMessage = e.getMessage();
            log.error("DeliveryMan processing local message occur exception: {}", exceptionMessage);
            localMessageDeliveryFailHandle(localMessage, false, exceptionMessage);
        }
    }

    class DefaultSendCallback implements SendCallback {
        private TransactionLocalMessageTableModel localMessage;

        public DefaultSendCallback(TransactionLocalMessageTableModel localMessage) {
            this.localMessage = localMessage;
        }

        @Override
        public void onSuccess(SendResult sendResult) {
            SendStatus sendStatus = sendResult.getSendStatus();
            //回填消息key
            localMessage.setMessageKey(sendResult.getMsgId());
            if (sendStatus == SendStatus.SEND_OK) {
                localMessageDeliveredHandle(localMessage);
            } else {
                localMessageDeliveryFailHandle(localMessage, true, sendStatus.name());
            }
        }

        @Override
        public void onException(Throwable throwable) {
            String throwableMessage = throwable.getMessage();
            log.error("DeliveryMan processing local message occur exception: {}", throwableMessage);
            localMessageDeliveryFailHandle(localMessage, true, throwableMessage);
        }
    }

    class ReenteringQueueInCaseOfFailureSendCallback implements SendCallback {
        private TransactionLocalMessageTableModel localMessage;

        public ReenteringQueueInCaseOfFailureSendCallback(TransactionLocalMessageTableModel localMessage) {
            this.localMessage = localMessage;
        }

        @Override
        public void onSuccess(SendResult sendResult) {
            SendStatus sendStatus = sendResult.getSendStatus();
            //回填消息key
            localMessage.setMessageKey(sendResult.getMsgId());
            if (sendStatus == SendStatus.SEND_OK) {
                localMessageDeliveredHandle(localMessage);
            } else {
                localMessageDeliveryFailHandle(localMessage, true, sendStatus.name());
                TxMsgQueueFactory.getTxMsgQueue().put(localMessage);
            }
        }

        @Override
        public void onException(Throwable throwable) {
            String throwableMessage = throwable.getMessage();
            log.error("DeliveryMan processing local message occur exception: {}", throwableMessage);
            localMessageDeliveryFailHandle(localMessage, true, throwableMessage);
            TxMsgQueueFactory.getTxMsgQueue().put(localMessage);
        }
    }


    /**
     * 转换消息内容文本为消息对象
     *
     * @param messageContent 消息内容文本
     * @return
     * @throws JsonProcessingException
     */
    private Message convert2Message(String messageContent) throws JsonProcessingException {
        Map hashMap = objectMapper.readValue(messageContent, Map.class);
        return new GenericMessage<Object>(MapUtils.getObject(hashMap, "payload"), MapUtils.getMap(hashMap, "headers"));
    }

    /**
     * 尝试加锁
     *
     * @param localMessage 事务本地消息表模型对象
     * @return boolean true成功 false失败
     */
    private boolean tryLock(TransactionLocalMessageTableModel localMessage) {
        String lockKey = "LocalMessage-" + localMessage.getId() + "-DeliveryLock";
        log.debug("DeliveryMan processing local message, trying lock: {}", lockKey);
        boolean lockResult = false;
        try {
            lockResult = redisTemplate.opsForValue().setIfAbsent(lockKey, 1, Duration.ofSeconds(30L));
        } catch (Exception e) {
            log.error("DeliveryMan processed local message, try lock[{}] occur error.", lockKey);
            e.printStackTrace();
            throw e;
        }
        return lockResult;
    }

    /**
     * 本地消息投递成功处理逻辑
     *
     * @param localMessage 事务本地消息表模型对象
     */
    private void localMessageDeliveredHandle(TransactionLocalMessageTableModel localMessage) {
        localMessage.setUpdateTime(LocalDateTime.now())
                .setMessageStatus(TransactionLocalMessageStatus.Delivered.getStatus());
        localMessageTableMapper.updateById(localMessage);
    }

    /**
     * 本地消息投递失败处理逻辑
     *
     * @param localMessage 事务本地消息表模型对象
     * @param incurRetry   是否增加重试计数
     * @param description  描述信息
     */
    private void localMessageDeliveryFailHandle(TransactionLocalMessageTableModel localMessage, boolean incurRetry, String description) {
        localMessage.setUpdateTime(LocalDateTime.now())
                .setDescription(description);
        if (incurRetry) {
            localMessage.setRetryCount(localMessage.getRetryCount() + 1);
        }
        //死信
        if (localMessage.getRetryCount() == txProperties.getLocalMessageTable().getMaxRetryCount()) {
            localMessage.setDied(Boolean.TRUE);
        }
        localMessageTableMapper.updateById(localMessage);
    }

}
