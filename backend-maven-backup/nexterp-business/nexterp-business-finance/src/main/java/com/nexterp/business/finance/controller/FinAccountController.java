package com.nexterp.business.finance.controller;

import com.nexterp.business.finance.application.service.FinAccountService;
import com.nexterp.business.finance.domain.model.FinAccount;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 财务科目控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/finance/accounts")
@RequiredArgsConstructor
public class FinAccountController {

    private final FinAccountService accountService;

    /**
     * 创建科目
     *
     * @param account 科目
     * @return 科目ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('finance:account:add')")
    public Result<Long> createAccount(@Valid @RequestBody FinAccount account) {
        Long id = accountService.createAccount(account);
        return Result.success(id);
    }

    /**
     * 更新科目
     *
     * @param id      科目ID
     * @param account 科目
     * @return 更新后的科目
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:account:edit')")
    public Result<FinAccount> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody FinAccount account) {
        FinAccount updated = accountService.updateAccount(id, account);
        return Result.success(updated);
    }

    /**
     * 删除科目
     *
     * @param id 科目ID
     * @return 成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:account:delete')")
    public Result<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return Result.success();
    }

    /**
     * 获取科目详情
     *
     * @param id 科目ID
     * @return 科目
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:account:view')")
    public Result<FinAccount> getAccountById(@PathVariable Long id) {
        FinAccount account = accountService.getAccountById(id);
        return Result.success(account);
    }

    /**
     * 获取科目树
     *
     * @param tenantId 租户ID
     * @return 科目树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('finance:account:view')")
    public Result<List<FinAccount>> getAccountTree(@RequestParam Long tenantId) {
        List<FinAccount> tree = accountService.getAccountTree(tenantId);
        return Result.success(tree);
    }

    /**
     * 获取指定类型的科目树
     *
     * @param accountType 科目类型
     * @param tenantId    租户ID
     * @return 科目树
     */
    @GetMapping("/tree/{accountType}")
    @PreAuthorize("hasAuthority('finance:account:view')")
    public Result<List<FinAccount>> getAccountTreeByType(
            @PathVariable Integer accountType,
            @RequestParam Long tenantId) {
        List<FinAccount> tree = accountService.getAccountTreeByType(accountType, tenantId);
        return Result.success(tree);
    }

    /**
     * 分页查询科目
     *
     * @param tenantId 租户ID
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('finance:account:view')")
    public Result<PageResult<FinAccount>> listAccounts(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(current - 1, size);
        PageResult<FinAccount> result = accountService.listAccounts(tenantId, pageable);
        return Result.success(result);
    }

    /**
     * 查询叶子科目
     *
     * @param tenantId 租户ID
     * @return 科目列表
     */
    @GetMapping("/leaf")
    @PreAuthorize("hasAuthority('finance:account:view')")
    public Result<List<FinAccount>> listLeafAccounts(@RequestParam Long tenantId) {
        List<FinAccount> accounts = accountService.listLeafAccounts(tenantId);
        return Result.success(accounts);
    }

    /**
     * 查询现金科目
     *
     * @param tenantId 租户ID
     * @return 科目列表
     */
    @GetMapping("/cash")
    @PreAuthorize("hasAuthority('finance:account:view')")
    public Result<List<FinAccount>> listCashAccounts(@RequestParam Long tenantId) {
        List<FinAccount> accounts = accountService.listCashAccounts(tenantId);
        return Result.success(accounts);
    }

    /**
     * 查询银行科目
     *
     * @param tenantId 租户ID
     * @return 科目列表
     */
    @GetMapping("/bank")
    @PreAuthorize("hasAuthority('finance:account:view')")
    public Result<List<FinAccount>> listBankAccounts(@RequestParam Long tenantId) {
        List<FinAccount> accounts = accountService.listBankAccounts(tenantId);
        return Result.success(accounts);
    }
}
