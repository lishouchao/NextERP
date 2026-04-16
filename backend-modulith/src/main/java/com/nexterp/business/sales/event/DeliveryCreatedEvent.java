package com.nexterp.business.sales.event;

/**
 * 交货单创建事件
 *
 * @author NextERP
 */
public record DeliveryCreatedEvent(
        /**
         * 交货单ID
         */
        Long deliveryId,

        /**
         * 交货单号
         */
        String deliveryNumber,

        /**
         * 交货类型
         */
        String deliveryType,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 关联订单ID
         */
        Long orderId
) {
}
