package com.fanzhang.framework.tx.localmessagetable.monitor.jmx;

import com.fanzhang.framework.tx.localmessagetable.repository.LocalMessageTableMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * 事务本地消息表统计
 *
 * @author Zhang Fan
 */
@Slf4j
public class TransactionLocalMessageTableStatistics implements TransactionLocalMessageTableStatisticsMBean, InitializingBean {

    @Autowired
    private LocalMessageTableMapper localMessageTableMapper;

    @Override
    public int getMessageTotal() {
        return localMessageTableMapper.getMessageTotal();
    }

    @Override
    public int getNoDeliveryOrDeliveryFailCount() {
        return localMessageTableMapper.getNoDeliveryOrDeliveryFailCount();
    }

    @Override
    public int getTheNoDeliveryOrDeliveryFailCountOfThePastTenMinutes() {
        return localMessageTableMapper.getTheNoDeliveryOrDeliveryFailCountOfThePastTenMinutes();
    }

    @Override
    public int getDeadCount() {
        return localMessageTableMapper.getDeadCount();
    }

    @Override
    public int getDeliveredCount() {
        return localMessageTableMapper.getDeliveredCount();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        final ObjectName statisticsName = new ObjectName("TransactionLocalMessageTableStatistics:name=LocalMessageTableStatistics");
        if (!mBeanServer.isRegistered(statisticsName)) {
            mBeanServer.registerMBean(this, statisticsName);

            log.info("JMX name (LocalMessageTableStatistics) register success.");
        } else {
            log.error("JMX name (LocalMessageTableStatistics) is already registered.");
        }
    }
}
