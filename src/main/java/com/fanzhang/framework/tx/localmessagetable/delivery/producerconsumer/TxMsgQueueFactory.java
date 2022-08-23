package com.fanzhang.framework.tx.localmessagetable.delivery.producerconsumer;

import com.fanzhang.framework.tx.localmessagetable.model.TransactionLocalMessageTableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * 事务消息队列工厂
 *
 * @author Zhang Fan
 */
public final class TxMsgQueueFactory {

    private static List<TxMsgQueueManager> TX_MSG_QUEUE_MANAGERS;
    private static int queueCount;

    public TxMsgQueueFactory(int queueCount) {
        TxMsgQueueFactory.queueCount = queueCount;
        TxMsgQueueFactory.TX_MSG_QUEUE_MANAGERS = new ArrayList<>(queueCount);
    }

    public static TxMsgQueueManager getTxMsgQueue(int queueIndex) {
        return Optional.ofNullable(TX_MSG_QUEUE_MANAGERS.get(queueIndex)).orElseGet(TxMsgQueueManager::new);
    }

    public static void putMsgToQueue(TransactionLocalMessageTableModel txMsg) {
        getTxMsgQueue(txMsg.getDestination().hashCode() % queueCount).put(txMsg);
    }

    /**
     * 事务消息队列管理器
     */
    public static final class TxMsgQueueManager {

        private final BlockingQueue<TransactionLocalMessageTableModel> txMsgQueue;

        private TxMsgQueueManager() {
            txMsgQueue = new LinkedTransferQueue<>();
        }

        public void put(TransactionLocalMessageTableModel txMsg) {
            try {
                txMsgQueue.put(txMsg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public TransactionLocalMessageTableModel take() {
            try {
                return txMsgQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
