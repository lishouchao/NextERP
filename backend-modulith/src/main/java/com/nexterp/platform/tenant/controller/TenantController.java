package com.nexterp.platform.tenant.controller;

import com.nexterp.platform.tenant.dto.request.TenantCreateRequest;
import com.nexterp.platform.tenant.dto.request.TenantUpdateRequest;
import com.nexterp.platform.tenant.dto.response.TenantResponse;
import com.nexterp.platform.tenant.application.service.TenantService;
import com.nexterp.shared.core.result.Result;
import com.nexterp.shared.core.result.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户管理控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * 创建租户
     *
     * @param request 创建请求
     * @return 租户响应
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:tenant:add')")
    public Result<TenantResponse> createTenant(@Valid @RequestBody TenantCreateRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return Result.success(response);
    }

    /**
     * 更新租户
     *
     * @param id 租户ID
     * @param request 更新请求
     * @return 租户响应
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:tenant:edit')")
    public Result<TenantResponse> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody TenantUpdateRequest request) {
        TenantResponse response = tenantService.updateTenant(id, request);
        return Result.success(response);
    }

    /**
     * 删除租户
     *
     * @param id 租户ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:tenant:delete')")
    public Result<Void> deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return Result.success();
    }

    /**
     * 获取租户详情
     *
     * @param id 租户ID
     * @return 租户响应
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:tenant:view')")
    public Result<TenantResponse> getTenantById(@PathVariable Long id) {
        TenantResponse response = tenantService.getTenantById(id);
        return Result.success(response);
    }

    /**
     * 分页查询租户
     *
     * @param tenantCode 租户编码（模糊查询）
     * @param tenantName 租户名称（模糊查询）
     * @param status 状态
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('system:tenant:view')")
    public Result<PageResult<TenantResponse>> listTenants(
            @RequestParam(required = false) String tenantCode,
            @RequestParam(required = false) String tenantName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        // 构建分页参数
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(current - 1, size, sort);

        PageResult<TenantResponse> response = tenantService.listTenants(tenantCode, tenantName, status, pageable);
        return Result.success(response);
    }

    /**
     * 获取所有启用状态的租户
     *
     * @return 租户列表
     */
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('system:tenant:view')")
    public Result<List<TenantResponse>> listActiveTenants() {
        List<TenantResponse> response = tenantService.listActiveTenants();
        return Result.success(response);
    }

    /**
     * 启用/禁用租户
     *
     * @param id 租户ID
     * @param status 状态
     * @return 成功响应
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:tenant:edit')")
    public Result<Void> updateTenantStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        tenantService.updateTenantStatus(id, status);
        return Result.success();
    }

    /**
     * 获取即将过期的租户
     *
     * @param days 天数
     * @return 租户列表
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasAuthority('system:tenant:view')")
    public Result<List<TenantResponse>> getExpiringTenants(
            @RequestParam(defaultValue = "30") Integer days) {
        List<TenantResponse> response = tenantService.getExpiringTenants(days);
        return Result.success(response);
    }
}
