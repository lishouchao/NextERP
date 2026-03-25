package com.nexterp.business.finance.controller;

import com.nexterp.business.finance.application.service.FiAccountGroupService;
import com.nexterp.business.finance.domain.model.FiAccountGroup;
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
 * 科目组控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/finance/account-groups")
@RequiredArgsConstructor
public class FiAccountGroupController {

    private final FiAccountGroupService accountGroupService;

    /**
     * 创建科目组
     */
    @PostMapping
    @PreAuthorize("hasAuthority('finance:account-group:add')")
    public Result<Long> createAccountGroup(@Valid @RequestBody FiAccountGroup accountGroup) {
        Long id = accountGroupService.createAccountGroup(accountGroup);
        return Result.success(id);
    }

    /**
     * 更新科目组
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:account-group:edit')")
    public Result<FiAccountGroup> updateAccountGroup(
            @PathVariable Long id,
            @Valid @RequestBody FiAccountGroup accountGroup) {
        FiAccountGroup updated = accountGroupService.updateAccountGroup(id, accountGroup);
        return Result.success(updated);
    }

    /**
     * 删除科目组
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:account-group:delete')")
    public Result<Void> deleteAccountGroup(@PathVariable Long id) {
        accountGroupService.deleteAccountGroup(id);
        return Result.success();
    }

    /**
     * 获取科目组详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:account-group:view')")
    public Result<FiAccountGroup> getAccountGroupById(@PathVariable Long id) {
        FiAccountGroup accountGroup = accountGroupService.getAccountGroupById(id);
        return Result.success(accountGroup);
    }

    /**
     * 获取科目组树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('finance:account-group:view')")
    public Result<List<FiAccountGroup>> getAccountGroupTree(@RequestParam Long tenantId) {
        List<FiAccountGroup> tree = accountGroupService.getAccountGroupTree(tenantId);
        return Result.success(tree);
    }

    /**
     * 根据科目表ID查询
     */
    @GetMapping("/by-coa/{coaId}")
    @PreAuthorize("hasAuthority('finance:account-group:view')")
    public Result<List<FiAccountGroup>> listByCoaId(
            @PathVariable Long coaId,
            @RequestParam Long tenantId) {
        List<FiAccountGroup> list = accountGroupService.listByCoaId(coaId, tenantId);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('finance:account-group:view')")
    public Result<PageResult<FiAccountGroup>> listAccountGroups(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FiAccountGroup> page = accountGroupService.listAccountGroups(tenantId, PageRequest.of(current - 1, size));

        PageResult<FiAccountGroup> result = PageResult.<FiAccountGroup>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
