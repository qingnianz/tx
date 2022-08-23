package com.fanzhang.framework.tx.localmessagetable.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fanzhang.framework.tx.localmessagetable.model.TransactionLocalMessageTableModel;
import org.apache.ibatis.annotations.Select;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

/**
 * @author Zhang Fan
 */
@ConditionalOnProperty(value = "tx.local-message-table.enable", havingValue = "true", matchIfMissing = true)
@Repository
public interface LocalMessageTableMapper extends BaseMapper<TransactionLocalMessageTableModel> {

    @Select("select count(*) from transaction_local_message_table")
    int getMessageTotal();

    @Select("select count(*) from transaction_local_message_table where message_status = 0 and died = 0")
    int getNoDeliveryOrDeliveryFailCount();

    @Select("select count(*) from transaction_local_message_table where message_status = 0 and died = 0 and update_time < DATE_SUB(NOW(),INTERVAL 10 MINUTE)")
    int getTheNoDeliveryOrDeliveryFailCountOfThePastTenMinutes();

    @Select("select count(*) from transaction_local_message_table where died = 1")
    int getDeadCount();

    @Select("select count(*) from transaction_local_message_table where message_status = 1")
    int getDeliveredCount();
}
