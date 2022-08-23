package com.fanzhang.framework.tx;

import com.fanzhang.framework.tx.localmessagetable.LocalMessageTableTXTemplate;
import com.fanzhang.framework.tx.rocketmq.RocketMQTXTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 事务模版默认实现
 *
 * @author Zhang Fan
 */
public class DefaultTXTemplate implements TXTemplate {

    @Autowired(required = false)
    private RocketMQTXTemplate rocketMQTXTemplate;
    @Autowired(required = false)
    private LocalMessageTableTXTemplate localMessageTableTXTemplate;

    @Override
    public void convertAndSend(String destination, String payload) {
        nonNull(rocketMQTXTemplate);
        rocketMQTXTemplate.convertAndSend(destination, payload);
    }

    @Override
    public void send(String destination, Message<?> message) {
        nonNull(rocketMQTXTemplate);
        rocketMQTXTemplate.send(destination, message);
    }

    @Override
    public void sendInTransaction(String destination, String payload, TXTransactionCallback callback) {
        nonNull(rocketMQTXTemplate);
        rocketMQTXTemplate.sendInTransaction(destination, payload, callback);
    }

    @Override
    public void sendInTransaction(String destination, Message<?> message, TXTransactionCallback callback, Object object) {
        nonNull(rocketMQTXTemplate);
        rocketMQTXTemplate.sendInTransaction(destination, message, callback, object);
    }

    @Override
    public void sendInMessageTransaction(String destination, String payload, TXTransactionCallback callback) {
        nonNull(rocketMQTXTemplate);
        rocketMQTXTemplate.sendInMessageTransaction(destination, payload, callback);
    }

    @Override
    public void sendInMessageTransaction(String destination, Message<?> message, TXTransactionCallback callback, Object object) {
        nonNull(rocketMQTXTemplate);
        rocketMQTXTemplate.sendInMessageTransaction(destination, message, callback, object);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void sendInLocalMessageTable(String destination, String payload) {
        nonNull(localMessageTableTXTemplate);
        localMessageTableTXTemplate.sendInLocalMessageTable(destination, payload);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void sendInLocalMessageTable(String destination, Message<?> message) {
        nonNull(localMessageTableTXTemplate);
        localMessageTableTXTemplate.sendInLocalMessageTable(destination, message);
    }

    private void nonNull(Object o) {
        Assert.notNull(o, "TXTemplate 'Autowired' field is null.");
    }
}
