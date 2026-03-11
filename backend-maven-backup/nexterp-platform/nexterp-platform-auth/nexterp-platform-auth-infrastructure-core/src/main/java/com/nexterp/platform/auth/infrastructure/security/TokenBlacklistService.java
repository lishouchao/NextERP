package com.nexterp.platform.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";
    private final RedisTemplate<String, Object> redisTemplate;

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
        // 获取用户当前有效的Token列表
        String userTokensKey = "user:tokens:" + userId;
        Object tokens = redisTemplate.opsForValue().get(userTokensKey);

        if (tokens instanceof List) {
            // 将所有Token加入黑名单（24小时过期）
            for (Object token : (List<?>) tokens) {
                addToBlacklist(token.toString(), 86400);
            }
        }

        // 清除用户Token列表
        redisTemplate.delete(userTokensKey);
        log.info("用户所有Token已加入黑名单: userId={}", userId);
    }

    /**
     * 定时清理过期令牌（每天凌晨3点）
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredBlacklistTokens() {
        // Redis 中可以设置 key 的过期时间来实现自动清理
        // 这里简化处理，只记录日志
        log.debug("定时清理过期黑名单Token");
    }
}
