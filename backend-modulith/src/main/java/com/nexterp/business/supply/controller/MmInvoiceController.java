package com.nexterp.business.supply.controller;

import com.nexterp.business.supply.application.service.MmInvoiceVerificationService;
import com.nexterp.business.supply.dto.InvoiceDTO;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/supply/invoices")
@RequiredArgsConstructor
public class MmInvoiceController {

    private final MmInvoiceVerificationService invoiceService;

    @PostMapping("/verify")
    @PreAuthorize("hasAuthority('mm:invoice:add')")
    public Result<Long> verifyInvoice(
            @RequestParam Long poId,
            @RequestParam Long tenantId,
            @RequestParam String invoiceType,
            @RequestParam BigDecimal grossAmount,
            @RequestParam BigDecimal netAmount,
            @RequestParam BigDecimal taxAmount,
            @RequestParam String supplierInvoice) {
        Map<String, Object> params = new HashMap<>();
        params.put("poId", poId);
        params.put("tenantId", tenantId);
        params.put("invoiceType", invoiceType);
        params.put("grossAmount", grossAmount);
        params.put("netAmount", netAmount);
        params.put("taxAmount", taxAmount);
        params.put("supplierInvoice", supplierInvoice);
        Long id = invoiceService.createInvoice(params);
        return Result.success(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mm:invoice:view')")
    public Result<InvoiceDTO> getInvoice(@PathVariable Long id) {
        return Result.success(invoiceService.getInvoiceById(id));
    }

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('mm:invoice:view')")
    public Result<PageResult<InvoiceDTO>> listInvoices(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(invoiceService.listInvoices(tenantId, status, current, size));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('mm:invoice:post')")
    public Result<Void> postInvoice(@PathVariable Long id) {
        invoiceService.postInvoice(id);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('mm:invoice:cancel')")
    public Result<Void> cancelInvoice(@PathVariable Long id, @RequestParam(required = false) String reason) {
        invoiceService.reverseInvoice(id);
        return Result.success();
    }
}
