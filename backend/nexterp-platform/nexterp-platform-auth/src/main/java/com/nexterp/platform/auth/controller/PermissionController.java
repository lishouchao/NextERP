package com.nexterp.platform.auth.controller;

import com.nexterp.platform.auth.dto.request.PermissionCreateRequest;
import com.nexterp.platform.auth.dto.request.PermissionQueryRequest;
import com.nexterp.platform.auth.dto.request.PermissionUpdateRequest;
import com.nexterp.platform.auth.dto.response.PermissionResponse;
import com.nexterp.platform.auth.application.service.PermissionService;
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
 * 权限管理控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 创建权限
     *
     * @param request 创建请求
     * @return 权限响应
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:permission:add')")
    public Result<PermissionResponse> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        PermissionResponse response = permissionService.createPermission(request);
        return Result.success(response);
    }

    /**
     * 更新权限
     *
     * @param id 权限ID
     * @param request 更新请求
     * @return 权限响应
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:permission:edit')")
    public Result<PermissionResponse> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionUpdateRequest request) {
        PermissionResponse response = permissionService.updatePermission(id, request);
        return Result.success(response);
    }

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:permission:delete')")
    public Result<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success();
    }

    /**
     * 获取权限详情
     *
     * @param id 权限ID
     * @return 权限响应
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:permission:view')")
    public Result<PermissionResponse> getPermissionById(@PathVariable Long id) {
        PermissionResponse response = permissionService.getPermissionById(id);
        return Result.success(response);
    }

    /**
     * 分页查询权限
     *
     * @param request 查询请求
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('system:permission:view')")
    public Result<PageResult<PermissionResponse>> listPermissions(
            @RequestBody PermissionQueryRequest request,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        // 构建分页参数
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        Pageable pageable = PageRequest.of(current - 1, size, sort);

        PageResult<PermissionResponse> response = permissionService.listPermissions(request, pageable);
        return Result.success(response);
    }

    /**
     * 获取权限树
     *
     * @param tenantId 租户ID
     * @return 权限树列表
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:permission:view')")
    public Result<List<PermissionResponse>> getPermissionTree(
            @RequestParam Long tenantId) {
        List<PermissionResponse> response = permissionService.getPermissionTree(tenantId);
        return Result.success(response);
    }

    /**
     * 启用/禁用权限
     *
     * @param id 权限ID
     * @param status 状态
     * @return 成功响应
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:permission:edit')")
    public Result<Void> updatePermissionStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        permissionService.updatePermissionStatus(id, status);
        return Result.success();
    }
}
