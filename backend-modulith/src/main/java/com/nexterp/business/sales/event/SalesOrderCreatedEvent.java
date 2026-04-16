package com.nexterp.business.sales.event;

import java.math.BigDecimal;

/**
 * 销售订单创建事件
 *
 * @author NextERP
 */
public record SalesOrderCreatedEvent(
        /**
         * 订单ID
         */
        Long orderId,

        /**
         * 订单号
         */
        String orderNumber,

        /**
         * 订单类型
         */
        String orderType,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 售达方ID
         */
        Long soldToParty,

        /**
         * 净值
         */
        BigDecimal netValue
) {
}
