package com.nexterp.business.finance.controller;

import com.nexterp.business.finance.application.service.FiGlAccountService;
import com.nexterp.business.finance.domain.model.FiGlAccount;
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
 * 总账科目控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/finance/gl-accounts")
@RequiredArgsConstructor
public class FiGlAccountController {

    private final FiGlAccountService glAccountService;

    /**
     * 创建总账科目
     */
    @PostMapping
    @PreAuthorize("hasAuthority('finance:gl-account:add')")
    public Result<Long> createGlAccount(@Valid @RequestBody FiGlAccount account) {
        Long id = glAccountService.createGlAccount(account);
        return Result.success(id);
    }

    /**
     * 更新总账科目
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:gl-account:edit')")
    public Result<FiGlAccount> updateGlAccount(
            @PathVariable Long id,
            @Valid @RequestBody FiGlAccount account) {
        FiGlAccount updated = glAccountService.updateGlAccount(id, account);
        return Result.success(updated);
    }

    /**
     * 删除总账科目
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:gl-account:delete')")
    public Result<Void> deleteGlAccount(@PathVariable Long id) {
        glAccountService.deleteGlAccount(id);
        return Result.success();
    }

    /**
     * 获取科目详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:gl-account:view')")
    public Result<FiGlAccount> getGlAccountById(@PathVariable Long id) {
        FiGlAccount account = glAccountService.getGlAccountById(id);
        return Result.success(account);
    }

    /**
     * 根据代码获取
     */
    @GetMapping("/code/{accountCode}")
    @PreAuthorize("hasAuthority('finance:gl-account:view')")
    public Result<FiGlAccount> getGlAccountByCode(
            @PathVariable String accountCode,
            @RequestParam Long coaId,
            @RequestParam Long tenantId) {
        FiGlAccount account = glAccountService.getGlAccountByCode(accountCode, coaId, tenantId);
        return Result.success(account);
    }

    /**
     * 获取科目树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('finance:gl-account:view')")
    public Result<List<FiGlAccount>> getGlAccountTree(
            @RequestParam Long coaId,
            @RequestParam Long tenantId) {
        List<FiGlAccount> tree = glAccountService.getGlAccountTree(coaId, tenantId);
        return Result.success(tree);
    }

    /**
     * 获取当前有效的可记账科目
     */
    @GetMapping("/valid-postable")
    @PreAuthorize("hasAuthority('finance:gl-account:view')")
    public Result<List<FiGlAccount>> getValidPostableAccounts(@RequestParam Long tenantId) {
        List<FiGlAccount> list = glAccountService.getValidPostableAccounts(tenantId);
        return Result.success(list);
    }

    /**
     * 根据科目类型查询
     */
    @GetMapping("/by-type/{accountType}")
    @PreAuthorize("hasAuthority('finance:gl-account:view')")
    public Result<List<FiGlAccount>> listByAccountType(
            @PathVariable String accountType,
            @RequestParam Long tenantId) {
        List<FiGlAccount> list = glAccountService.listByAccountType(accountType, tenantId);
        return Result.success(list);
    }

    /**
     * 根据科目表查询
     */
    @GetMapping("/by-coa/{coaId}")
    @PreAuthorize("hasAuthority('finance:gl-account:view')")
    public Result<List<FiGlAccount>> listByCoaId(
            @PathVariable Long coaId,
            @RequestParam Long tenantId) {
        List<FiGlAccount> list = glAccountService.listByCoaId(coaId, tenantId);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('finance:gl-account:view')")
    public Result<PageResult<FiGlAccount>> listGlAccounts(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FiGlAccount> page = glAccountService.listGlAccounts(tenantId, PageRequest.of(current - 1, size));

        PageResult<FiGlAccount> result = PageResult.<FiGlAccount>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
