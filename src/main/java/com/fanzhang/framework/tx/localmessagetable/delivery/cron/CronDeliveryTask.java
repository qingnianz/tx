package com.fanzhang.framework.tx.localmessagetable.delivery.cron;

import com.fanzhang.framework.tx.localmessagetable.delivery.DeliveryMan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * corn表达式方式事务本地消息投递任务
 *
 * @author Zhang Fan
 */
@Slf4j
public class CronDeliveryTask {
    @Autowired
    private DeliveryMan deliveryMan;

    /**
     * 默认每10秒执行一次
     */
    @Scheduled(fixedDelayString = "${tx.local-message-table.cron.fixed-delay}")
    public void run() {
        deliveryMan.deliveryDatabaseToBeDelivered();
    }


}
