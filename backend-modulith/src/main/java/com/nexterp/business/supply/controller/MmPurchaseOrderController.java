package com.nexterp.business.supply.controller;

import com.nexterp.business.supply.application.service.MmPurchaseOrderService;
import com.nexterp.business.supply.dto.CreatePurchaseOrderRequest;
import com.nexterp.business.supply.dto.PurchaseOrderDTO;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/supply/purchase-orders")
@RequiredArgsConstructor
public class MmPurchaseOrderController {

    private final MmPurchaseOrderService purchaseOrderService;

    @PostMapping
    @PreAuthorize("hasAuthority('mm:po:add')")
    public Result<Long> createPurchaseOrder(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        Long id = purchaseOrderService.createPurchaseOrder(request);
        return Result.success(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mm:po:view')")
    public Result<PurchaseOrderDTO> getPurchaseOrder(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getPurchaseOrderById(id));
    }

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('mm:po:view')")
    public Result<PageResult<PurchaseOrderDTO>> listPurchaseOrders(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(purchaseOrderService.listPurchaseOrders(tenantId, status, current, size));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('mm:po:edit')")
    public Result<Void> submitPurchaseOrder(@PathVariable Long id) {
        purchaseOrderService.submitPurchaseOrder(id);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('mm:po:approve')")
    public Result<Void> approvePurchaseOrder(@PathVariable Long id, @RequestParam String approvedBy) {
        purchaseOrderService.approvePurchaseOrder(id, approvedBy);
        return Result.success();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('mm:po:edit')")
    public Result<Void> closePurchaseOrder(@PathVariable Long id) {
        purchaseOrderService.submitPurchaseOrder(id); // reuse submit for close
        return Result.success();
    }
}
