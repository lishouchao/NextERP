package com.nexterp.business.production.event;

/**
 * BOM创建事件
 *
 * @param bomId     BOM ID
 * @param bomCode   BOM编码
 * @param productId 成品物料ID
 * @param tenantId  租户ID
 * @author NextERP
 */
public record BomCreatedEvent(Long bomId, String bomCode, Long productId, Long tenantId) {
}
