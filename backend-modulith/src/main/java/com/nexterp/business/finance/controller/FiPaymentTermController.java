package com.nexterp.business.finance.controller;

import com.nexterp.business.finance.application.service.FiPaymentTermService;
import com.nexterp.business.finance.domain.model.FiPaymentTerm;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 付款条件控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/finance/payment-terms")
@RequiredArgsConstructor
public class FiPaymentTermController {

    private final FiPaymentTermService paymentTermService;

    /**
     * 创建付款条件
     */
    @PostMapping
    @PreAuthorize("hasAuthority('finance:payment-term:add')")
    public Result<Long> createPaymentTerm(@Valid @RequestBody FiPaymentTerm paymentTerm) {
        Long id = paymentTermService.createPaymentTerm(paymentTerm);
        return Result.success(id);
    }

    /**
     * 更新付款条件
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:payment-term:edit')")
    public Result<FiPaymentTerm> updatePaymentTerm(
            @PathVariable Long id,
            @Valid @RequestBody FiPaymentTerm paymentTerm) {
        FiPaymentTerm updated = paymentTermService.updatePaymentTerm(id, paymentTerm);
        return Result.success(updated);
    }

    /**
     * 删除付款条件
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:payment-term:delete')")
    public Result<Void> deletePaymentTerm(@PathVariable Long id) {
        paymentTermService.deletePaymentTerm(id);
        return Result.success();
    }

    /**
     * 获取付款条件详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:payment-term:view')")
    public Result<FiPaymentTerm> getPaymentTermById(@PathVariable Long id) {
        FiPaymentTerm paymentTerm = paymentTermService.getPaymentTermById(id);
        return Result.success(paymentTerm);
    }

    /**
     * 根据代码获取
     */
    @GetMapping("/code/{paymentTermCode}")
    @PreAuthorize("hasAuthority('finance:payment-term:view')")
    public Result<FiPaymentTerm> getPaymentTermByCode(
            @PathVariable String paymentTermCode,
            @RequestParam Long tenantId) {
        FiPaymentTerm pt = paymentTermService.getPaymentTermByCode(paymentTermCode, tenantId);
        return Result.success(pt);
    }

    /**
     * 获取默认付款条件
     */
    @GetMapping("/default")
    @PreAuthorize("hasAuthority('finance:payment-term:view')")
    public Result<FiPaymentTerm> getDefaultPaymentTerm(@RequestParam Long tenantId) {
        return paymentTermService.getDefaultPaymentTerm(tenantId)
                .map(Result::success)
                .orElse(Result.success(null));
    }

    /**
     * 获取所有启用的付款条件
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('finance:payment-term:view')")
    public Result<List<FiPaymentTerm>> listEnabledPaymentTerms(@RequestParam Long tenantId) {
        List<FiPaymentTerm> list = paymentTermService.listEnabledPaymentTerms(tenantId);
        return Result.success(list);
    }

    /**
     * 根据类型查询
     */
    @GetMapping("/by-type/{termType}")
    @PreAuthorize("hasAuthority('finance:payment-term:view')")
    public Result<List<FiPaymentTerm>> listByTermType(
            @PathVariable String termType,
            @RequestParam Long tenantId) {
        List<FiPaymentTerm> list = paymentTermService.listByTermType(termType, tenantId);
        return Result.success(list);
    }

    /**
     * 根据适用范围查询
     */
    @GetMapping("/by-scope/{applyScope}")
    @PreAuthorize("hasAuthority('finance:payment-term:view')")
    public Result<List<FiPaymentTerm>> listByApplyScope(
            @PathVariable String applyScope,
            @RequestParam Long tenantId) {
        List<FiPaymentTerm> list = paymentTermService.listByApplyScope(applyScope, tenantId);
        return Result.success(list);
    }

    /**
     * 计算到期日
     */
    @GetMapping("/calculate-due-date")
    @PreAuthorize("hasAuthority('finance:payment-term:view')")
    public Result<LocalDate> calculateDueDate(
            @RequestParam String paymentTermCode,
            @RequestParam Long tenantId,
            @RequestParam LocalDate baselineDate) {
        LocalDate dueDate = paymentTermService.calculateDueDate(paymentTermCode, tenantId, baselineDate);
        return Result.success(dueDate);
    }

    /**
     * 分页查询
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('finance:payment-term:view')")
    public Result<PageResult<FiPaymentTerm>> listPaymentTerms(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FiPaymentTerm> page = paymentTermService.listPaymentTerms(tenantId, PageRequest.of(current - 1, size));

        PageResult<FiPaymentTerm> result = PageResult.<FiPaymentTerm>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
