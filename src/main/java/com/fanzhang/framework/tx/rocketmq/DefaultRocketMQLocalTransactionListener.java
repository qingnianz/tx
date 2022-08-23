package com.fanzhang.framework.tx.rocketmq;

import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.Message;

/**
 * RocketMQ事务监听器默认实现
 * </p>
 *
 * @author Zhang Fan
 */
@ConditionalOnBean(RocketMQTXTemplate.class)
@RocketMQTransactionListener
public class DefaultRocketMQLocalTransactionListener implements RocketMQLocalTransactionListener, InitializingBean {

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        TransactionListenerRegistry.RocketMQTransactionListenerMethod transactionListenerMethod = TransactionListenerRegistry.get().get(message.getHeaders().get("rocketmq_TOPIC"));
        return transactionListenerMethod.invokeExecuteLocalTransaction(message, o);
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        TransactionListenerRegistry.RocketMQTransactionListenerMethod transactionListenerMethod = TransactionListenerRegistry.get().get(message.getHeaders().get("rocketmq_TOPIC"));
        return transactionListenerMethod.invokeCheckLocalTransaction(message);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

}
