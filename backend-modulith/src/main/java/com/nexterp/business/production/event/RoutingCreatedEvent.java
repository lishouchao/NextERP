package com.nexterp.business.production.event;

/**
 * 工艺路线创建事件
 *
 * @param routingId   工艺路线ID
 * @param routingCode 工艺路线编码
 * @param productId   产品ID
 * @param tenantId    租户ID
 * @author NextERP
 */
public record RoutingCreatedEvent(Long routingId, String routingCode, Long productId, Long tenantId) {
}
