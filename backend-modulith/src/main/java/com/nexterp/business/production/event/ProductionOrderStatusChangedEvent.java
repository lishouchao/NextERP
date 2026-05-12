package com.nexterp.business.production.event;

/**
 * 生产订单状态变更事件
 *
 * @param orderId   生产订单ID
 * @param orderNo   生产订单号
 * @param oldStatus 原状态
 * @param newStatus 新状态
 * @param tenantId  租户ID
 * @author NextERP
 */
public record ProductionOrderStatusChangedEvent(Long orderId, String orderNo,
                                                 Integer oldStatus, Integer newStatus, Long tenantId) {
}
