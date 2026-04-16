package com.nexterp.business.sales.event;

/**
 * 销售订单审批拒绝事件
 *
 * @author NextERP
 */
public record SalesOrderRejectedEvent(
        /**
         * 订单ID
         */
        Long orderId,

        /**
         * 订单号
         */
        String orderNumber,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 拒绝人
         */
        String rejectedBy,

        /**
         * 拒绝原因
         */
        String rejectReason
) {
}
