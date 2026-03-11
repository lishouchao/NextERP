package com.nexterp.platform.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务
 *
 * @author NextERP
 */
@Slf4j
@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";
    private final RedisTemplate<String, Object> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 将Token加入黑名单
     *
     * @param token        Token
     * @param expireSeconds 过期时间(秒)
     */
    public void addToBlacklist(String token, long expireSeconds) {
        String key = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expireSeconds, TimeUnit.SECONDS);
        log.debug("Token已加入黑名单: expireSeconds={}", expireSeconds);
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param token Token
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 将用户所有Token加入黑名单
     *
     * @param userId 用户ID
     */
    public void blacklistAllUserTokens(String userId) {
        // 这里需要维护一个用户Token列表
        // 简化实现：在Redis中存储用户当前有效的Token
        String userTokensKey = "user:tokens:" + userId;
        Object tokens = redisTemplate.opsForValue().get(userTokensKey);

        if (tokens instanceof List) {
            for (Object token : (List<?>) tokens) {
                addToBlacklist(token.toString(), 86400); // 24小时
            }
        }

        // 清除用户Token列表
        redisTemplate.delete(userTokensKey);
        log.info("用户所有Token已加入黑名单: userId={}", userId);
    }
}
