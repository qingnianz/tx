package com.fanzhang.framework.tx.localmessagetable.delivery.producerconsumer;

import com.fanzhang.framework.tx.localmessagetable.model.TransactionLocalMessageTableModel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * 事务消息队列工厂
 *
 * @author Zhang Fan
 */
public final class TxMsgQueueFactory {

    private static final TxMsgQueueManager TX_MSG_QUEUE_MANAGER = new TxMsgQueueManager();

    public static TxMsgQueueManager getTxMsgQueue() {
        return TX_MSG_QUEUE_MANAGER;
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
