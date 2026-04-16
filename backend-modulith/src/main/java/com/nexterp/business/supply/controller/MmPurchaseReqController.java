package com.nexterp.business.supply.controller;

import com.nexterp.business.supply.application.service.MmPurchaseReqService;
import com.nexterp.business.supply.dto.CreatePurchaseReqRequest;
import com.nexterp.business.supply.dto.PurchaseReqDTO;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/supply/purchase-reqs")
@RequiredArgsConstructor
public class MmPurchaseReqController {

    private final MmPurchaseReqService purchaseReqService;

    @PostMapping
    @PreAuthorize("hasAuthority('mm:purchase:add')")
    public Result<Long> createPurchaseReq(@Valid @RequestBody CreatePurchaseReqRequest request) {
        Long id = purchaseReqService.createPurchaseReq(request);
        return Result.success(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mm:purchase:view')")
    public Result<PurchaseReqDTO> getPurchaseReq(@PathVariable Long id) {
        return Result.success(purchaseReqService.getPurchaseReqById(id));
    }

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('mm:purchase:view')")
    public Result<PageResult<PurchaseReqDTO>> listPurchaseReqs(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(purchaseReqService.listPurchaseReqs(tenantId, status, current, size));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('mm:purchase:edit')")
    public Result<Void> submitPurchaseReq(@PathVariable Long id) {
        purchaseReqService.submitPurchaseReq(id);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('mm:purchase:approve')")
    public Result<Void> approvePurchaseReq(@PathVariable Long id, @RequestParam String approvedBy) {
        purchaseReqService.approvePurchaseReq(id, approvedBy);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('mm:purchase:approve')")
    public Result<Void> rejectPurchaseReq(@PathVariable Long id, @RequestParam String rejectedBy, @RequestParam String reason) {
        purchaseReqService.rejectPurchaseReq(id, rejectedBy, reason);
        return Result.success();
    }
}
