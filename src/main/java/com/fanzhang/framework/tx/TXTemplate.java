package com.fanzhang.framework.tx;

import org.springframework.messaging.Message;

/**
 * 事务模版定义
 *
 * @author Zhang Fan
 */
public interface TXTemplate {

    void convertAndSend(String destination, String payload);

    void send(String destination, Message<?> message);

    @Deprecated
    void sendInTransaction(String destination, String payload, TXTransactionCallback callback);

    @Deprecated
    void sendInTransaction(String destination, Message<?> message, TXTransactionCallback callback, Object object);

    void sendInMessageTransaction(String destination, String payload, TXTransactionCallback callback);

    void sendInMessageTransaction(String destination, Message<?> message, TXTransactionCallback callback, Object object);

    void sendInLocalMessageTable(String destination, String payload);

    void sendInLocalMessageTable(String destination, Message<?> message);
}
