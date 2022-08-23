package com.fanzhang.framework.tx.rocketmq;

import com.fanzhang.framework.tx.TXTransactionCallback;
import lombok.AllArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Zhang Fan
 */
public class TransactionListenerRegistry {

    private static final Map<String, RocketMQTransactionListenerMethod> transactionListenerMethodMap = new HashMap<>();

    private static Lock mapLock = new ReentrantLock();

    public static void registry(String destination, TXTransactionCallback txTransactionCallback) {
        mapLock.lock();
        try {
            Class<? extends TXTransactionCallback> callbackClass = txTransactionCallback.getClass();
            Method executeLocalTransaction = callbackClass.getDeclaredMethod("executeLocalTransaction", Message.class, Object.class);
            Method checkLocalTransaction = callbackClass.getDeclaredMethod("checkLocalTransaction", Message.class);
            transactionListenerMethodMap.put(destination, new RocketMQTransactionListenerMethod(txTransactionCallback, executeLocalTransaction, checkLocalTransaction));
        } catch (NoSuchMethodException e) {
            // never into here
            e.printStackTrace();
        } finally {
            mapLock.unlock();
        }
    }

    public static Map<String, RocketMQTransactionListenerMethod> get() {
        return new HashMap<>(transactionListenerMethodMap);
    }

    @AllArgsConstructor
    static class RocketMQTransactionListenerMethod {
        private Object target;
        private Method executeLocalTransaction;
        private Method checkLocalTransaction;

        /**
         * 执行本地事务方法
         *
         * @param message mq消息对象
         * @param o       执行本地事务方法需要的参数
         * @return {@link RocketMQLocalTransactionState} 本地事务执行状态
         */
        public RocketMQLocalTransactionState invokeExecuteLocalTransaction(Message message, Object o) {
            try {
                this.executeLocalTransaction.setAccessible(true);
                return (RocketMQLocalTransactionState) this.executeLocalTransaction.invoke(target, message, o);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return RocketMQLocalTransactionState.UNKNOWN;
        }

        /**
         * 执行回查接口
         *
         * @param message mq消息对象
         * @return {@link RocketMQLocalTransactionState} 本地事务执行状态
         */
        public RocketMQLocalTransactionState invokeCheckLocalTransaction(Message message) {
            try {
                this.checkLocalTransaction.setAccessible(true);
                return (RocketMQLocalTransactionState) this.checkLocalTransaction.invoke(target, message);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
