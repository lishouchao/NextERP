package com.nexterp.platform.auth.service;

import com.nexterp.platform.auth.dto.request.RefreshTokenRequest;
import com.nexterp.platform.auth.dto.response.RefreshTokenResponse;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.security.properties.JwtProperties;
import com.nexterp.shared.security.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 7;

    /**
     * 创建刷新令牌
     *
     * @param username 用户名
     * @return 刷新令牌
     */
    public String createRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();
        String key = REFRESH_TOKEN_PREFIX + refreshToken;

        // 存储刷新令牌与用户名的映射
        redisTemplate.opsForValue().set(
                key,
                username,
                REFRESH_TOKEN_EXPIRE_DAYS,
                TimeUnit.DAYS
        );

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
        String key = REFRESH_TOKEN_PREFIX + refreshToken;

        // 从Redis获取用户名
        String username = (String) redisTemplate.opsForValue().get(key);
        if (username == null) {
            throw new BusinessException("刷新令牌无效或已过期");
        }

        // 生成新的访问令牌
        String newAccessToken = jwtTokenProvider.generateToken(username);

        // 生成新的刷新令牌
        String newRefreshToken = createRefreshToken(username);

        // 删除旧的刷新令牌
        redisTemplate.delete(key);

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
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 撤销刷新令牌
     *
     * @param refreshToken 刷新令牌
     */
    public void revokeRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.delete(key);
        log.debug("撤销刷新令牌");
    }
}
