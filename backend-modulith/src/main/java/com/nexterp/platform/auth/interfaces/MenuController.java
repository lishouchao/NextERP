package com.nexterp.platform.auth.interfaces;

import com.nexterp.platform.auth.api.dto.request.MenuCreateRequest;
import com.nexterp.platform.auth.api.dto.request.MenuUpdateRequest;
import com.nexterp.platform.auth.api.dto.response.MenuResponse;
import com.nexterp.platform.auth.application.service.MenuService;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 创建菜单
     *
     * @param request 创建请求
     * @return 菜单响应
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<MenuResponse> createMenu(@Valid @RequestBody MenuCreateRequest request) {
        MenuResponse response = menuService.createMenu(request);
        return Result.success(response);
    }

    /**
     * 更新菜单
     *
     * @param id 菜单ID
     * @param request 更新请求
     * @return 菜单响应
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<MenuResponse> updateMenu(
            @PathVariable Long id,
            @Valid @RequestBody MenuUpdateRequest request) {
        MenuResponse response = menuService.updateMenu(id, request);
        return Result.success(response);
    }

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单响应
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:view')")
    public Result<MenuResponse> getMenuById(@PathVariable Long id) {
        MenuResponse response = menuService.getMenuById(id);
        return Result.success(response);
    }

    /**
     * 获取菜单树
     *
     * @param tenantId 租户ID
     * @return 菜单树列表
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:view')")
    public Result<List<MenuResponse>> getMenuTree(
            @RequestParam Long tenantId) {
        List<MenuResponse> response = menuService.getMenuTree(tenantId);
        return Result.success(response);
    }

    /**
     * 获取用户菜单树
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 菜单树列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<MenuResponse>> getUserMenuTree(
            @PathVariable Long userId,
            @RequestParam Long tenantId) {
        List<MenuResponse> response = menuService.getUserMenuTree(userId, tenantId);
        return Result.success(response);
    }
}
