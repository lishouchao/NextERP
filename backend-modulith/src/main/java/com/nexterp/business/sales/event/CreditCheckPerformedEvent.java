package com.nexterp.business.sales.event;

import java.math.BigDecimal;

/**
 * 信用检查执行事件
 *
 * @author NextERP
 */
public record CreditCheckPerformedEvent(
        /**
         * 客户ID
         */
        Long customerId,

        /**
         * 公司代码ID
         */
        Long companyId,

        /**
         * 检查结果
         */
        String checkResult,

        /**
         * 检查金额
         */
        BigDecimal checkAmount,

        /**
         * 租户ID
         */
        Long tenantId
) {
}
