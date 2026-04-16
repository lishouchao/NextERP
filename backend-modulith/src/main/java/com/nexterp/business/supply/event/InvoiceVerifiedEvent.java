package com.nexterp.business.supply.event;

import java.math.BigDecimal;

/**
 * 发票校验事件
 *
 * @author NextERP
 */
public record InvoiceVerifiedEvent(
        /**
         * 发票ID
         */
        Long invoiceId,

        /**
         * 发票号
         */
        String invoiceNumber,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 供应商ID
         */
        Long vendorId,

        /**
         * 发票金额
         */
        BigDecimal grossAmount
) {
}
