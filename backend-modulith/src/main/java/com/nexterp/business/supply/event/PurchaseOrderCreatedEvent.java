package com.nexterp.business.supply.event;

public record PurchaseOrderCreatedEvent(Long poId, String poNumber, Long tenantId, Long vendorId) {}
