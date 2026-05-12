package com.nexterp.business.production.event;

import java.math.BigDecimal;

/**
 * 生产订单创建事件
 *
 * @param orderId    生产订单ID
 * @param orderNo    生产订单号
 * @param orderType  订单类型
 * @param productId  产品ID
 * @param tenantId   租户ID
 * @param plannedQty 计划数量
 * @author NextERP
 */
public record ProductionOrderCreatedEvent(Long orderId, String orderNo, Integer orderType,
                                          Long productId, Long tenantId, BigDecimal plannedQty) {
}
