package com.nexterp.platform.auth.interfaces;

import com.nexterp.platform.auth.api.dto.request.UserCreateRequest;
import com.nexterp.platform.auth.api.dto.request.UserQueryRequest;
import com.nexterp.platform.auth.api.dto.request.UserUpdateRequest;
import com.nexterp.platform.auth.api.dto.response.UserResponse;
import com.nexterp.platform.auth.application.service.UserService;
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

/**
 * 用户管理控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     *
     * @param request 创建请求
     * @return 用户响应
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return Result.success(response);
    }

    /**
     * 更新用户
     *
     * @param id 用户ID
     * @param request 更新请求
     * @return 用户响应
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return Result.success(response);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户响应
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:view')")
    public Result<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return Result.success(response);
    }

    /**
     * 分页查询用户
     *
     * @param request 查询请求
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('system:user:view')")
    public Result<PageResult<UserResponse>> listUsers(
            @RequestBody UserQueryRequest request,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        // 构建分页参数
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(current - 1, size, sort);

        PageResult<UserResponse> response = userService.listUsers(request, pageable);
        return Result.success(response);
    }

    /**
     * 修改密码
     *
     * @param id 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 成功响应
     */
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        userService.changePassword(id, oldPassword, newPassword);
        return Result.success();
    }

    /**
     * 重置密码
     *
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 成功响应
     */
    @PutMapping("/{id}/password/reset")
    @PreAuthorize("hasAuthority('system:user:reset')")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }

    /**
     * 启用/禁用用户
     *
     * @param id 用户ID
     * @param status 状态
     * @return 成功响应
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }
}
