package com.fanzhang.framework.tx.localmessagetable;

import com.fanzhang.framework.tx.TXProperties;
import com.fanzhang.framework.tx.TXTemplate;
import com.fanzhang.framework.tx.TXTransactionCallback;
import com.fanzhang.framework.tx.localmessagetable.delivery.producerconsumer.TxMsgQueueFactory;
import com.fanzhang.framework.tx.localmessagetable.model.TransactionLocalMessageStatus;
import com.fanzhang.framework.tx.localmessagetable.model.TransactionLocalMessageTableModel;
import com.fanzhang.framework.tx.localmessagetable.repository.LocalMessageTableMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * 基于 {@link com.fanzhang.framework.tx.localmessagetable.repository.LocalMessageTableMapper} 本地消息表事务实现
 * </p>
 *
 * @author Zhang Fan
 */
public class LocalMessageTableTXTemplate implements TXTemplate {

    @Value("${spring.application.name}")
    private String serviceName;
    @Autowired
    private TXProperties txProperties;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LocalMessageTableMapper localMessageTableMapper;

    @Override
    public void convertAndSend(String destination, String payload) {
        throw new UnsupportedOperationException("LocalMessageTableTXTemplate un supported 'convertAndSend' operation.");
    }

    @Override
    public void send(String destination, Message<?> message) {
        throw new UnsupportedOperationException("LocalMessageTableTXTemplate un supported 'send' operation.");
    }

    @Override
    public void sendInTransaction(String destination, String payload, TXTransactionCallback callback) {
        throw new UnsupportedOperationException("LocalMessageTableTXTemplate un supported 'sendInTransaction' operation.");
    }

    @Override
    public void sendInTransaction(String destination, Message<?> message, TXTransactionCallback callback, Object object) {
        throw new UnsupportedOperationException("LocalMessageTableTXTemplate un supported 'sendInTransaction' operation.");
    }

    @Override
    public void sendInMessageTransaction(String destination, String payload, TXTransactionCallback callback) {
        throw new UnsupportedOperationException("LocalMessageTableTXTemplate un supported 'sendInMessageTransaction' operation.");
    }

    @Override
    public void sendInMessageTransaction(String destination, Message<?> message, TXTransactionCallback callback, Object object) {
        throw new UnsupportedOperationException("LocalMessageTableTXTemplate un supported 'sendInMessageTransaction' operation.");
    }

    @Override
    public void sendInLocalMessageTable(String destination, String payload) {
        sendInLocalMessageTable(destination, MessageBuilder.withPayload(payload).build());
    }

    @Override
    public void sendInLocalMessageTable(String destination, Message<?> message) {
        try {
            TransactionLocalMessageTableModel tlmtm = TransactionLocalMessageTableModel.builder().destination(destination).messageContent(objectMapper.writeValueAsString(message))
                    .messageStatus(TransactionLocalMessageStatus.NoDeliveryOrDeliveryFail.getStatus())
                    .createTime(LocalDateTime.now())
                    .serviceName(serviceName).build();
            localMessageTableMapper.insert(tlmtm);
            if (txProperties.getLocalMessageTable().getProducerConsumer().isEnable()) {
                // 注册事务提交hook
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //Tips 队列是无界阻塞队列，不需要异步投递，不会影响该方法耗时
                        TxMsgQueueFactory.putMsgToQueue(tlmtm);
                    }
                });
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
