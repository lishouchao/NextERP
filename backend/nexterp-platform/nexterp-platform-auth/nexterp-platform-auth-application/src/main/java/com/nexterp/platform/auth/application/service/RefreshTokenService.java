package com.nexterp.platform.auth.application.service;

import com.nexterp.platform.auth.api.dto.request.RefreshTokenRequest;
import com.nexterp.platform.auth.api.dto.response.RefreshTokenResponse;
import com.nexterp.platform.auth.infrastructure.security.JwtTokenProvider;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.security.properties.JwtProperties;
import com.nexterp.shared.security.userdetails.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 刷新令牌服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    // 内存存储刷新令牌（生产环境应使用Redis）
    private static final Map<String, TokenInfo> REFRESH_TOKENS = new ConcurrentHashMap<>();
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 7;

    /**
     * 创建刷新令牌
     *
     * @param username 用户名
     * @return 刷新令牌
     */
    public String createRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_DAYS);

        TokenInfo tokenInfo = new TokenInfo(username, expiryTime);
        REFRESH_TOKENS.put(refreshToken, tokenInfo);

        log.debug("创建刷新令牌: username={}", username);
        return refreshToken;
    }

    /**
     * 刷新访问令牌
     *
     * @param request 刷新令牌请求
     * @return 刷新令牌响应
     */
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 从内存获取用户名
        TokenInfo tokenInfo = REFRESH_TOKENS.get(refreshToken);
        if (tokenInfo == null || tokenInfo.isExpired()) {
            throw new BusinessException("刷新令牌无效或已过期");
        }

        String username = tokenInfo.getUsername();

        // 构建UserInfo对象
        UserInfo userInfo = UserInfo.builder()
                .username(username)
                .tenantId(0L) // 刷新令牌时租户ID需要从数据库查询
                .build();

        // 生成新的访问令牌
        String newAccessToken = jwtTokenProvider.generateToken(userInfo);

        // 生成新的刷新令牌
        String newRefreshToken = createRefreshToken(username);

        // 删除旧的刷新令牌
        REFRESH_TOKENS.remove(refreshToken);

        log.info("刷新令牌成功: username={}", username);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpiration())
                .build();
    }

    /**
     * 验证刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 是否有效
     */
    public boolean validateRefreshToken(String refreshToken) {
        TokenInfo tokenInfo = REFRESH_TOKENS.get(refreshToken);
        return tokenInfo != null && !tokenInfo.isExpired();
    }

    /**
     * 撤销刷新令牌
     *
     * @param refreshToken 刷新令牌
     */
    public void revokeRefreshToken(String refreshToken) {
        REFRESH_TOKENS.remove(refreshToken);
        log.debug("撤销刷新令牌");
    }

    /**
     * 定时清理过期令牌
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanExpiredTokens() {
        REFRESH_TOKENS.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired();
            if (expired) {
                log.debug("清理过期刷新令牌");
            }
            return expired;
        });
    }

    /**
     * 令牌信息
     */
    private static class TokenInfo {
        private final String username;
        private final LocalDateTime expiryTime;

        public TokenInfo(String username, LocalDateTime expiryTime) {
            this.username = username;
            this.expiryTime = expiryTime;
        }

        public String getUsername() {
            return username;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}
