package com.nexterp.platform.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 登录尝试服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String ATTEMPT_KEY_PREFIX = "login_attempt:";
    private static final String LOCK_KEY_PREFIX = "login_lock:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 登录失败
     *
     * @param username 用户名
     */
    public void loginFailed(String username) {
        String key = ATTEMPT_KEY_PREFIX + username;
        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts == null) {
            attempts = 1L;
            redisTemplate.opsForValue().set(key, attempts, Duration.ofHours(1));
        }

        log.debug("登录失败: username={}, attempts={}", username, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            lockUser(username);
            log.warn("用户已被锁定: username={}, attempts={}", username, attempts);
        }
    }

    /**
     * 登录成功
     *
     * @param username 用户名
     */
    public void loginSucceeded(String username) {
        String attemptKey = ATTEMPT_KEY_PREFIX + username;
        String lockKey = LOCK_KEY_PREFIX + username;

        redisTemplate.delete(attemptKey);
        redisTemplate.delete(lockKey);

        log.debug("登录成功，清除失败记录: username={}", username);
    }

    /**
     * 检查是否被锁定
     *
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean isLocked(String username) {
        String lockKey = LOCK_KEY_PREFIX + username;
        Boolean isLocked = redisTemplate.hasKey(lockKey);
        return Boolean.TRUE.equals(isLocked);
    }

    /**
     * 获取剩余锁定时间（秒）
     *
     * @param username 用户名
     * @return 剩余秒数
     */
    public long getRemainingLockTime(String username) {
        String lockKey = LOCK_KEY_PREFIX + username;
        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return ttl != null ? ttl : 0;
    }

    /**
     * 锁定用户
     *
     * @param username 用户名
     */
    private void lockUser(String username) {
        String lockKey = LOCK_KEY_PREFIX + username;
        redisTemplate.opsForValue().set(lockKey, "locked", LOCK_DURATION);
        log.warn("用户已被锁定: username={}", username);
    }

    /**
     * 获取失败次数
     *
     * @param username 用户名
     * @return 失败次数
     */
    public int getAttempts(String username) {
        String key = ATTEMPT_KEY_PREFIX + username;
        Object attempts = redisTemplate.opsForValue().get(key);
        return attempts != null ? (Integer) attempts : 0;
    }

    /**
     * 定时清理过期登录记录（每天凌晨3点）
     */
    @Scheduled(cron = "0 0 3 * *")
    public void cleanExpiredLoginAttempts() {
        // Redis 实现：通过设置过期时间来自动清理
        log.debug("定时清理过期登录记录");
    }
}
