package com.nexterp.business.production.event;

/**
 * 工序开工事件
 *
 * @author NextERP
 */
public record OperationStartedEvent(
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
         * 工作中心ID
         */
        Long workCenterId,

        /**
         * 报工人员ID
         */
        Long workerId,

        /**
         * 租户ID
         */
        Long tenantId
) {
}
