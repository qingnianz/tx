package com.fanzhang.framework.tx.localmessagetable.monitor.actuator;

import lombok.Builder;
import lombok.Data;

/**
 * 本地消息表统计信息
 *
 * @author Zhang Fan
 */
@Data
@Builder
public class LocalMessageTableStatisticsInfo {

    /**
     * 消息总数
     */
    private int messageTotal;

    /**
     * 未投递或投递失败的消息数量
     */
    private int noDeliveryOrDeliveryFailCount;

    /**
     * 过去超过10分钟的未投递或投递失败的数量
     */
    private int theNoDeliveryOrDeliveryFailCountOfThePastTenMinutes;

    /**
     * 死信消息总数
     */
    private int deadCount;

    /**
     * 已投递的消息数量
     */
    private int deliveredCount;
}
