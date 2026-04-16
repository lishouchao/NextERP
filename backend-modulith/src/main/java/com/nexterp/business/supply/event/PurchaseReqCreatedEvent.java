package com.nexterp.business.supply.event;

public record PurchaseReqCreatedEvent(Long reqId, String prNumber, Long tenantId) {}
