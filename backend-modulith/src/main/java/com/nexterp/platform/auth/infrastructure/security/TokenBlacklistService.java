package com.nexterp.platform.auth.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token黑名单服务
 *
 * @author NextERP
 */
@Slf4j
@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";

    // 内存存储黑名单（生产环境应使用Redis）
    private static final Set<BlacklistedToken> BLACKLIST = ConcurrentHashMap.newKeySet();

    /**
     * 将Token加入黑名单
     *
     * @param token        Token
     * @param expireSeconds 过期时间(秒)
     */
    public void addToBlacklist(String token, long expireSeconds) {
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(expireSeconds);
        BLACKLIST.add(new BlacklistedToken(token, expiryTime));
        log.debug("Token已加入黑名单: expireSeconds={}", expireSeconds);
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param token Token
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        return BLACKLIST.stream()
                .anyMatch(bt -> bt.token.equals(token) && !bt.isExpired());
    }

    /**
     * 将用户所有Token加入黑名单
     *
     * @param userId 用户ID
     */
    public void blacklistAllUserTokens(String userId) {
        // 内存实现简化：只记录黑名单，不维护用户Token列表
        log.info("用户所有Token请求加入黑名单: userId={}", userId);
    }

    /**
     * 定时清理过期令牌
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanExpiredTokens() {
        BLACKLIST.removeIf(bt -> {
            boolean expired = bt.isExpired();
            if (expired) {
                log.debug("清理过期黑名单Token");
            }
            return expired;
        });
    }

    /**
     * 黑名单Token
     */
    private static class BlacklistedToken {
        private final String token;
        private final LocalDateTime expiryTime;

        public BlacklistedToken(String token, LocalDateTime expiryTime) {
            this.token = token;
            this.expiryTime = expiryTime;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}
