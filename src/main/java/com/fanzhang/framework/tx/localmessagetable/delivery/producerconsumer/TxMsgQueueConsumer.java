package com.fanzhang.framework.tx.localmessagetable.delivery.producerconsumer;

import com.fanzhang.framework.tx.TXProperties;
import com.fanzhang.framework.tx.localmessagetable.delivery.DeliveryMan;
import com.fanzhang.framework.tx.localmessagetable.model.TransactionLocalMessageTableModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事务消息队列消费者
 *
 * @author Zhang Fan
 */
@Slf4j
public class TxMsgQueueConsumer implements InitializingBean {
    private final ExecutorService executor;

    private TXProperties txProperties;
    private DeliveryMan deliveryMan;
    private int nThreads;

    public TxMsgQueueConsumer(TXProperties txProperties, DeliveryMan deliveryMan) {
        this.txProperties = txProperties;
        this.deliveryMan = deliveryMan;
        nThreads = txProperties.getLocalMessageTable().getProducerConsumer().getConsumerThreadNum();
        log.debug("TXMsgQueueConsumerPool the number of thread is {}", nThreads);
        executor = new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new TxMsgQueueConsumerThreadFactory());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (int i = 0; i < nThreads; i++) {
            executor.submit(new TxMsgQueueDeliveryTask(deliveryMan));
        }
        // FIXME 启动默认执行一下数据库待处理的（存在多副本争抢问题，选主执行？？？2022-08-23: select ... for update）
        Thread oldDataProcessThread = new Thread(() -> deliveryMan.deliveryDatabaseToBeDelivered());
        oldDataProcessThread.setName("oldDataProcessThread");
        oldDataProcessThread.setDaemon(true);
        oldDataProcessThread.start();
    }

    static class TxMsgQueueDeliveryTask implements Runnable {
        private DeliveryMan deliveryMan;

        public TxMsgQueueDeliveryTask(DeliveryMan deliveryMan) {
            this.deliveryMan = deliveryMan;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    TransactionLocalMessageTableModel txMsg = TxMsgQueueFactory.getTxMsgQueue().take();
                    if (txMsg == null)
                        continue;
                    deliveryMan.deliveryOne(txMsg, true);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    // Copy from the internal class DefaultThreadFactory of Executors.
    static class TxMsgQueueConsumerThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        TxMsgQueueConsumerThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "txMsgPool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
