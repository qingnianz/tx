package com.fanzhang.framework.tx.localmessagetable.monitor.actuator;

import com.fanzhang.framework.tx.localmessagetable.monitor.jmx.TransactionLocalMessageTableStatistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * @author Zhang Fan
 */
@Endpoint(
        id = "local-message-table-statistics-info"
)
public class LocalMessageTableStatisticsInfoEndpoint {

    private final TransactionLocalMessageTableStatistics transactionLocalMessageTableStatistics;

    public LocalMessageTableStatisticsInfoEndpoint(TransactionLocalMessageTableStatistics transactionLocalMessageTableStatistics) {
        this.transactionLocalMessageTableStatistics = transactionLocalMessageTableStatistics;
    }

    @ReadOperation
    public LocalMessageTableStatisticsInfo info() {
        return LocalMessageTableStatisticsInfo.builder()
                .deadCount(transactionLocalMessageTableStatistics.getDeadCount())
                .deliveredCount(transactionLocalMessageTableStatistics.getDeliveredCount())
                .messageTotal(transactionLocalMessageTableStatistics.getMessageTotal())
                .noDeliveryOrDeliveryFailCount(transactionLocalMessageTableStatistics.getNoDeliveryOrDeliveryFailCount())
                .theNoDeliveryOrDeliveryFailCountOfThePastTenMinutes(transactionLocalMessageTableStatistics.getTheNoDeliveryOrDeliveryFailCountOfThePastTenMinutes())
                .build();
    }
}
