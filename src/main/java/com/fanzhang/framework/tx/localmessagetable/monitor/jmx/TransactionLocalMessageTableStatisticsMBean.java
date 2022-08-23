package com.fanzhang.framework.tx.localmessagetable.monitor.jmx;

/**
 * 事务本地消息表统计MBean
 *
 * @author Zhang Fan
 */
public interface TransactionLocalMessageTableStatisticsMBean {

    /**
     * 获取消息总数
     *
     * @return
     */
    int getMessageTotal();

    /**
     * 获取未投递或投递失败的消息数量
     *
     * @return
     */
    int getNoDeliveryOrDeliveryFailCount();

    /**
     * 获取过去超过10分钟的未投递或投递失败的数量
     *
     * @return
     */
    int getTheNoDeliveryOrDeliveryFailCountOfThePastTenMinutes();

    /**
     * 获取死信消息总数
     *
     * @return
     */
    int getDeadCount();

    /**
     * 获取已投递的消息数量
     *
     * @return
     */
    int getDeliveredCount();

}
