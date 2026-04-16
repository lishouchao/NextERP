package com.nexterp.business.sales.event;

/**
 * 销售订单审批通过事件
 *
 * @author NextERP
 */
public record SalesOrderApprovedEvent(
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
         * 审批人
         */
        String approvedBy
) {
}
