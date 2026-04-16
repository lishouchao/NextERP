package com.nexterp.business.supply.event;

/**
 * 采购申请审批通过事件
 *
 * @author NextERP
 */
public record PurchaseReqApprovedEvent(
        /**
         * 采购申请ID
         */
        Long prId,

        /**
         * 采购申请号
         */
        String prNumber,

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
