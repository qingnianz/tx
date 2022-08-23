package com.fanzhang.framework.tx.rocketmq;

import com.fanzhang.framework.tx.TXTemplate;
import com.fanzhang.framework.tx.TXTransactionCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * 基于 {@link RocketMQTemplate} 实现事务模版
 * </p>
 *
 * @author Zhang Fan
 */
public class RocketMQTXTemplate implements TXTemplate {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void convertAndSend(String destination, String payload) {
        rocketMQTemplate.convertAndSend(destination, payload);
    }

    @Override
    public void send(String destination, Message<?> message) {
        rocketMQTemplate.send(destination, message);
    }

    @Override
    public void sendInTransaction(String destination, String payload, TXTransactionCallback callback) {
        sendInMessageTransaction(destination, payload, callback);
    }

    @Override
    public void sendInTransaction(String destination, Message<?> message, TXTransactionCallback callback, Object object) {
        sendInMessageTransaction(destination, message, callback, object);
    }

    @Override
    public void sendInMessageTransaction(String destination, String payload, TXTransactionCallback callback) {
        sendInMessageTransaction(destination, MessageBuilder.withPayload(payload).build(), callback, null);
    }

    @Override
    public void sendInMessageTransaction(String destination, Message<?> message, TXTransactionCallback callback, Object object) {
        TransactionListenerRegistry.registry(destination, callback);
        rocketMQTemplate.sendMessageInTransaction(destination, message, object);
    }

    @Override
    public void sendInLocalMessageTable(String destination, String payload) {
        throw new UnsupportedOperationException("RocketMQTXTemplate un supported 'sendInLocalMessageTable' operation.");
    }

    @Override
    public void sendInLocalMessageTable(String destination, Message<?> message) {
        throw new UnsupportedOperationException("RocketMQTXTemplate un supported 'sendInLocalMessageTable' operation.");
    }
}
