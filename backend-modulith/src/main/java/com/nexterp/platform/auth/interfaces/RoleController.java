package com.nexterp.platform.auth.interfaces;

import com.nexterp.platform.auth.api.dto.request.RoleCreateRequest;
import com.nexterp.platform.auth.api.dto.request.RoleQueryRequest;
import com.nexterp.platform.auth.api.dto.request.RoleUpdateRequest;
import com.nexterp.platform.auth.api.dto.response.RoleResponse;
import com.nexterp.platform.auth.application.service.RoleService;
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
 * 角色管理控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 创建角色
     *
     * @param request 创建请求
     * @return 角色响应
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request) {
        RoleResponse response = roleService.createRole(request);
        return Result.success(response);
    }

    /**
     * 更新角色
     *
     * @param id 角色ID
     * @param request 更新请求
     * @return 角色响应
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<RoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {
        RoleResponse response = roleService.updateRole(id, request);
        return Result.success(response);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色响应
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:view')")
    public Result<RoleResponse> getRoleById(@PathVariable Long id) {
        RoleResponse response = roleService.getRoleById(id);
        return Result.success(response);
    }

    /**
     * 分页查询角色
     *
     * @param request 查询请求
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('system:role:view')")
    public Result<PageResult<RoleResponse>> listRoles(
            @RequestBody RoleQueryRequest request,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        // 构建分页参数
        Sort sort = Sort.by(Sort.Direction.ASC, "roleSort");
        Pageable pageable = PageRequest.of(current - 1, size, sort);

        PageResult<RoleResponse> response = roleService.listRoles(request, pageable);
        return Result.success(response);
    }

    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 成功响应
     */
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('system:role:assign')")
    public Result<Void> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(roleId, permissionIds);
        return Result.success();
    }

    /**
     * 获取角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('system:role:view')")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        List<Long> permissions = roleService.getRolePermissions(roleId);
        return Result.success(permissions);
    }

    /**
     * 启用/禁用角色
     *
     * @param id 角色ID
     * @param status 状态
     * @return 成功响应
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> updateRoleStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        roleService.updateRoleStatus(id, status);
        return Result.success();
    }
}
