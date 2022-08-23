package com.fanzhang.framework.tx.localmessagetable;

import com.fanzhang.framework.tx.TXProperties;
import com.fanzhang.framework.tx.localmessagetable.delivery.DeliveryMan;
import com.fanzhang.framework.tx.localmessagetable.delivery.cron.CronDeliveryTask;
import com.fanzhang.framework.tx.localmessagetable.delivery.producerconsumer.TxMsgQueueConsumer;
import com.fanzhang.framework.tx.localmessagetable.monitor.actuator.LocalMessageTableStatisticsInfoEndpoint;
import com.fanzhang.framework.tx.localmessagetable.monitor.jmx.TransactionLocalMessageTableStatistics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 本地消息表自动配置
 *
 * @author Zhang Fan
 */
@ConditionalOnProperty(value = "tx.local-message-table.enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TXProperties.class)
@Configuration
public class TXLocalMessageTableAutoConfiguration {

    @Bean
    public LocalMessageTableTXTemplate localMessageTableTXTemplate() {
        return new LocalMessageTableTXTemplate();
    }

    @Bean
    public DeliveryMan deliveryMan() {
        return new DeliveryMan();
    }

    @Bean
    public TransactionLocalMessageTableStatistics transactionLocalMessageTableStatistics() {
        return new TransactionLocalMessageTableStatistics();
    }

    @Bean
    public LocalMessageTableStatisticsInfoEndpoint localMessageTableStatisticsInfoEndpoint() {
        return new LocalMessageTableStatisticsInfoEndpoint(transactionLocalMessageTableStatistics());
    }

    @ConditionalOnProperty(value = "tx.local-message-table.cron.enable", havingValue = "true")
    @Bean
    public CronDeliveryTask cornDeliveryTask() {
        return new CronDeliveryTask();
    }

    @ConditionalOnProperty(value = "tx.local-message-table.producerconsumer.enable", havingValue = "true", matchIfMissing = true)
    @Bean
    public TxMsgQueueConsumer txMsgQueueConsumer(TXProperties txProperties) {
        return new TxMsgQueueConsumer(txProperties, deliveryMan());
    }
}
