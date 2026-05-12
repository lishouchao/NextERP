package com.nexterp.business.production.event;

import java.math.BigDecimal;

/**
 * 工序完工事件
 *
 * @author NextERP
 */
public record OperationCompletedEvent(
        /**
         * 工序记录ID
         */
        Long operationId,

        /**
         * 生产订单ID
         */
        Long productionOrderId,

        /**
         * 顺序号
         */
        Integer sequenceNo,

        /**
         * 工序编码
         */
        String processCode,

        /**
         * 完工数量
         */
        BigDecimal completedQty,

        /**
         * 合格数量
         */
        BigDecimal qualifiedQty,

        /**
         * 报废数量
         */
        BigDecimal scrappedQty,

        /**
         * 租户ID
         */
        Long tenantId
) {
}
