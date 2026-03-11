package com.nexterp.platform.auth.application.service;

import com.nexterp.platform.auth.infrastructure.security.JwtTokenProvider;
import com.nexterp.platform.auth.infrastructure.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 登出服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @param refreshToken 刷新令牌
     */
    public void logout(HttpServletRequest request, String refreshToken) {
        // 获取访问令牌
        String token = extractToken(request);
        if (token != null) {
            // 计算Token剩余有效期
            Date expiration = jwtTokenProvider.getExpirationDate(token);
            long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000; // 转换为秒

            if (ttl > 0) {
                // 将Token加入黑名单
                tokenBlacklistService.addToBlacklist(token, (int) ttl);
            }
        }

        // 撤销刷新令牌
        if (refreshToken != null) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }

        log.info("用户登出成功");
    }

    /**
     * 从请求中提取Token
     *
     * @param request HTTP请求
     * @return Token字符串
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
