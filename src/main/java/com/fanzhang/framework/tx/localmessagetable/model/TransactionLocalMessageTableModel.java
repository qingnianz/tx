package com.fanzhang.framework.tx.localmessagetable.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 事务本地消息表模型
 *
 * @author Zhang Fan
 */
@ToString
@Data
@Builder
@Accessors(chain = true)
@TableName("transaction_local_message_table")
public class TransactionLocalMessageTableModel {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 投递目的地 topic
     */
    private String destination;

    /**
     * 投递二级目的地 tag
     */
    private String secondaryDestination;

    /**
     * 消息key
     */
    private String messageKey;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 消息状态
     *
     * @see TransactionLocalMessageStatus 消息状态
     */
    private Integer messageStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 死信 0否 1是
     */
    private Boolean died;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 备注
     */
    private String description;

    /**
     * 服务名称
     */
    private String serviceName;
}
