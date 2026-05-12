package com.nexterp.business.finance.listener;

import com.nexterp.business.supply.event.PurchaseOrderApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FiMmPurchaseOrderEventListener {

    @ApplicationModuleListener
    public void handlePurchaseOrderApproved(PurchaseOrderApprovedEvent event) {
        log.info("收到采购订单审批通过事件: poId={}, poNumber={}, tenantId={}, approvedBy={}",
                event.poId(), event.poNumber(), event.tenantId(), event.approvedBy());

        // PO审批不产生FI凭证，仅记录预算承诺
        // 后续在预算管理模块实现时，此处可创建预算占用记录
        log.info("采购订单 {} 审批通过，预算承诺待实现", event.poNumber());
    }
}
