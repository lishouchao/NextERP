package com.nexterp.business.sales.event;

import java.math.BigDecimal;

/**
 * 开票单创建事件
 *
 * @author NextERP
 */
public record BillingCreatedEvent(
        /**
         * 开票单ID
         */
        Long billingId,

        /**
         * 开票单号
         */
        String billingNumber,

        /**
         * 开票类型
         */
        String billingType,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 净值
         */
        BigDecimal netValue
) {
}
