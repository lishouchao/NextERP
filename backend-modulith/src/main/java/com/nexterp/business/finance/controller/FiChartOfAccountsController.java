package com.nexterp.business.finance.controller;

import com.nexterp.business.finance.application.service.FiChartOfAccountsService;
import com.nexterp.business.finance.domain.model.FiChartOfAccounts;
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
 * 科目表控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/finance/chart-of-accounts")
@RequiredArgsConstructor
public class FiChartOfAccountsController {

    private final FiChartOfAccountsService coaService;

    /**
     * 创建科目表
     */
    @PostMapping
    @PreAuthorize("hasAuthority('finance:coa:add')")
    public Result<Long> createCoa(@Valid @RequestBody FiChartOfAccounts coa) {
        Long id = coaService.createCoa(coa);
        return Result.success(id);
    }

    /**
     * 更新科目表
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:coa:edit')")
    public Result<FiChartOfAccounts> updateCoa(
            @PathVariable Long id,
            @Valid @RequestBody FiChartOfAccounts coa) {
        FiChartOfAccounts updated = coaService.updateCoa(id, coa);
        return Result.success(updated);
    }

    /**
     * 删除科目表
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:coa:delete')")
    public Result<Void> deleteCoa(@PathVariable Long id) {
        coaService.deleteCoa(id);
        return Result.success();
    }

    /**
     * 获取科目表详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:coa:view')")
    public Result<FiChartOfAccounts> getCoaById(@PathVariable Long id) {
        FiChartOfAccounts coa = coaService.getCoaById(id);
        return Result.success(coa);
    }

    /**
     * 根据代码获取
     */
    @GetMapping("/code/{coaCode}")
    @PreAuthorize("hasAuthority('finance:coa:view')")
    public Result<FiChartOfAccounts> getCoaByCode(
            @PathVariable String coaCode,
            @RequestParam Long tenantId) {
        FiChartOfAccounts coa = coaService.getCoaByCode(coaCode, tenantId);
        return Result.success(coa);
    }

    /**
     * 获取当前有效的科目表列表
     */
    @GetMapping("/valid")
    @PreAuthorize("hasAuthority('finance:coa:view')")
    public Result<List<FiChartOfAccounts>> getValidCoaList(@RequestParam Long tenantId) {
        List<FiChartOfAccounts> list = coaService.getValidCoaList(tenantId);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('finance:coa:view')")
    public Result<PageResult<FiChartOfAccounts>> listCoa(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FiChartOfAccounts> page = coaService.listCoa(tenantId, PageRequest.of(current - 1, size));

        PageResult<FiChartOfAccounts> result = PageResult.<FiChartOfAccounts>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
