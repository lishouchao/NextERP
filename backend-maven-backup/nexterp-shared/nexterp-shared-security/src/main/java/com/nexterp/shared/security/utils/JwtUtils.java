package com.nexterp.shared.security.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * JWT 工具类
 *
 * @author NextERP
 */
public class JwtUtils {

    /**
     * 从请求中提取令牌
     *
     * @param request      HTTP 请求
     * @param headerName   头名称
     * @param tokenPrefix  令牌前缀
     * @return JWT 令牌
     */
    public static String extractToken(HttpServletRequest request, String headerName, String tokenPrefix) {
        String bearerToken = request.getHeader(headerName);
        if (bearerToken != null && bearerToken.startsWith(tokenPrefix)) {
            return bearerToken.substring(tokenPrefix.length());
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     *
     * @return IP地址
     */
    public static String getClientIP() {
        // TODO: 实现获取真实客户端IP的逻辑
        // 需要考虑代理服务器的情况
        return "127.0.0.1";
    }

    /**
     * 验证令牌格式
     *
     * @param token JWT 令牌
     * @return 是否有效
     */
    public static boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        // 简单的格式验证
        String[] parts = token.split("\\.");
        return parts.length == 3; // JWT 格式: header.payload.signature
    }
}
