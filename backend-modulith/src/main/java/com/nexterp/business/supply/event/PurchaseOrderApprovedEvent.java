package com.nexterp.business.supply.event;

/**
 * 采购订单审批通过事件
 *
 * @author NextERP
 */
public record PurchaseOrderApprovedEvent(
        /**
         * 采购订单ID
         */
        Long poId,

        /**
         * 采购订单号
         */
        String poNumber,

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
