package com.nexterp.platform.auth.interfaces;

import com.nexterp.platform.auth.api.dto.LoginRequest;
import com.nexterp.platform.auth.api.dto.LoginResponse;
import com.nexterp.platform.auth.application.service.AuthService;
import com.nexterp.shared.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author NextERP
 */
@Slf4j
@Tag(name = "认证管理", description = "用户认证、登出、令牌刷新")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request: username={}", request.getUsername());
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户登出
     *
     * @param token 访问令牌
     * @return 响应结果
     */
    @Operation(summary = "用户登出", description = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout request");
        authService.logout(token);
        return Result.success();
    }

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public Result<String> refreshToken(@RequestParam String refreshToken) {
        log.info("Token refresh request");
        String newAccessToken = authService.refreshToken(refreshToken);
        return Result.success(newAccessToken);
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/userinfo")
    public Result<LoginResponse.UserInfo> getUserInfo() {
        // TODO: 实现获取当前用户信息
        return Result.success();
    }
}
