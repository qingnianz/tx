package com.fanzhang.framework.tx;

import com.fanzhang.framework.tx.localmessagetable.model.CronDeliveryMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Zhang Fan
 */
@Data
@ConfigurationProperties(prefix = "tx")
public class TXProperties {

    private TXLocalMessageTableProperties localMessageTable = new TXLocalMessageTableProperties();

    @Data
    public static class TXLocalMessageTableProperties {
        /**
         * 是否启用
         */
        private boolean enable = true;
        /**
         * 最大重试次数
         */
        private int maxRetryCount = 16;
        private TXLocalMessageTableCronProperties cron = new TXLocalMessageTableCronProperties();
        private TXLocalMessageTableProducerConsumerProperties producerConsumer = new TXLocalMessageTableProducerConsumerProperties();
    }

    @Data
    public static class TXLocalMessageTableCronProperties {
        /**
         * 是否启用cron定时任务方式
         */
        private boolean enable = false;
        /**
         * cron定时任务固定延迟，默认10s
         */
        private long fixedDelay = 10000;
        /**
         * 投递模式 单线程循环、多线程随机循环、多线程分组后循环
         */
        private CronDeliveryMode deliveryMode = CronDeliveryMode.EACH;
    }

    @Data
    public static class TXLocalMessageTableProducerConsumerProperties {
        /**
         * 是否启用生产者/消费者方式
         */
        private boolean enable = true;
        /**
         * 消费者线程数
         */
        private int consumerThreadNum = 1;
    }
}
