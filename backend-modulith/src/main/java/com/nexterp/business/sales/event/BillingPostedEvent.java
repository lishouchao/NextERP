package com.nexterp.business.sales.event;

import java.math.BigDecimal;

/**
 * 开票过账事件
 *
 * @author NextERP
 */
public record BillingPostedEvent(
        /**
         * 开票单ID
         */
        Long billingId,

        /**
         * 开票单号
         */
        String billingNumber,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 净值
         */
        BigDecimal netValue,

        /**
         * 税额
         */
        BigDecimal taxAmount
) {
}
