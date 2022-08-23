package com.fanzhang.framework.tx;

import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

/**
 * @author Zhang Fan
 */
public interface TXTransactionCallback {

    RocketMQLocalTransactionState executeLocalTransaction(Message message, Object object);

    RocketMQLocalTransactionState checkLocalTransaction(Message message);
}
