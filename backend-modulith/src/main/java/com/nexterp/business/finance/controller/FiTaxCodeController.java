package com.nexterp.business.finance.controller;

import com.nexterp.business.finance.application.service.FiTaxCodeService;
import com.nexterp.business.finance.domain.model.FiTaxCode;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 税码控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/finance/tax-codes")
@RequiredArgsConstructor
public class FiTaxCodeController {

    private final FiTaxCodeService taxCodeService;

    /**
     * 创建税码
     */
    @PostMapping
    @PreAuthorize("hasAuthority('finance:tax-code:add')")
    public Result<Long> createTaxCode(@Valid @RequestBody FiTaxCode taxCode) {
        Long id = taxCodeService.createTaxCode(taxCode);
        return Result.success(id);
    }

    /**
     * 更新税码
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:tax-code:edit')")
    public Result<FiTaxCode> updateTaxCode(
            @PathVariable Long id,
            @Valid @RequestBody FiTaxCode taxCode) {
        FiTaxCode updated = taxCodeService.updateTaxCode(id, taxCode);
        return Result.success(updated);
    }

    /**
     * 删除税码
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:tax-code:delete')")
    public Result<Void> deleteTaxCode(@PathVariable Long id) {
        taxCodeService.deleteTaxCode(id);
        return Result.success();
    }

    /**
     * 获取税码详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:tax-code:view')")
    public Result<FiTaxCode> getTaxCodeById(@PathVariable Long id) {
        FiTaxCode taxCode = taxCodeService.getTaxCodeById(id);
        return Result.success(taxCode);
    }

    /**
     * 根据代码获取
     */
    @GetMapping("/code/{taxCode}")
    @PreAuthorize("hasAuthority('finance:tax-code:view')")
    public Result<FiTaxCode> getTaxCodeByCode(
            @PathVariable String taxCode,
            @RequestParam Long tenantId) {
        FiTaxCode tc = taxCodeService.getTaxCodeByCode(taxCode, tenantId);
        return Result.success(tc);
    }

    /**
     * 获取当前有效的税码
     */
    @GetMapping("/valid")
    @PreAuthorize("hasAuthority('finance:tax-code:view')")
    public Result<List<FiTaxCode>> getValidTaxCodes(@RequestParam Long tenantId) {
        List<FiTaxCode> list = taxCodeService.getValidTaxCodes(tenantId);
        return Result.success(list);
    }

    /**
     * 根据税类型查询
     */
    @GetMapping("/by-type/{taxType}")
    @PreAuthorize("hasAuthority('finance:tax-code:view')")
    public Result<List<FiTaxCode>> listByTaxType(
            @PathVariable String taxType,
            @RequestParam Long tenantId) {
        List<FiTaxCode> list = taxCodeService.listByTaxType(taxType, tenantId);
        return Result.success(list);
    }

    /**
     * 获取所有启用的税码
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('finance:tax-code:view')")
    public Result<List<FiTaxCode>> listEnabledTaxCodes(@RequestParam Long tenantId) {
        List<FiTaxCode> list = taxCodeService.listEnabledTaxCodes(tenantId);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('finance:tax-code:view')")
    public Result<PageResult<FiTaxCode>> listTaxCodes(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FiTaxCode> page = taxCodeService.listTaxCodes(tenantId, PageRequest.of(current - 1, size));

        PageResult<FiTaxCode> result = PageResult.<FiTaxCode>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
