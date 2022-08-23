package com.fanzhang.framework.tx.localmessagetable.model;

import lombok.Getter;

/**
 * 事务本地消息状态枚举
 *
 * @author Zhang Fan
 */
public enum TransactionLocalMessageStatus {

    /**
     * 未投递或投递失败
     */
    NoDeliveryOrDeliveryFail(0),

    /**
     * 已成功投递
     */
    Delivered(1);

    @Getter
    private int status;

    TransactionLocalMessageStatus(int status) {
        this.status = status;
    }
}
