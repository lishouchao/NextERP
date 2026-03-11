package com.nexterp.platform.auth.infrastructure.security;

import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.nexterp.platform.auth.infrastructure.security.LoginAttemptData;

/**
 * 登录尝试服务
 *
 * @author NextERP
 */
@Slf4j
@Service
public class LoginAttemptService {

    private static final String ATTEMPT_KEY_PREFIX = "login_attempt:";
    private static final String LOCK_KEY_PREFIX = "login_lock:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);

    // 内存存储（开发环境替代Redis）
    private static final java.util.concurrent.ConcurrentHashMap<String, LoginAttemptData> attemptStorage = new java.util.concurrent.ConcurrentHashMap<>();

    public LoginAttemptService() {
    }

    /**
     * 登录失败
     *
     * @param username 用户名
     */
    public void loginFailed(String username) {
        String key = ATTEMPT_KEY_PREFIX + username;
        LoginAttemptData data = attemptStorage.computeIfAbsent(username, k -> {
            LoginAttemptData newData = new LoginAttemptData();
            attemptStorage.put(username, newData);
            return newData;
        });

        // 增加失败次数
        data.setAttempts(data.getAttempts() + 1);

        log.debug("登录失败: username={}, attempts={}", username, data.getAttempts());
    }

    /**
     * 登录成功
     *
     * @param username 用户名
     */
    public void loginSucceeded(String username) {
        LoginAttemptData data = attemptStorage.computeIfAbsent(username, k -> {
            LoginAttemptData newData = new LoginAttemptData();
            attemptStorage.put(username, newData);
            return newData;
        });

        // 重置尝试次数
        data.setAttempts(0);

        log.debug("登录成功，清除失败记录: username={}", username);
    }

    /**
     * 检查是否被锁定
     *
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean isLocked(String username) {
        LoginAttemptData data = attemptStorage.get(username);
        if (data == null) {
            return false;
        }

        // 检查是否超过最大尝试次数且在锁定时间内
        boolean isLocked = data.getAttempts() >= MAX_ATTEMPTS &&
                           data.getLastAttemptTime().isAfter(java.time.LocalDateTime.now().minusMinutes(5));

        log.debug("检查锁定状态: username={}, isLocked={}", username, isLocked);
        return isLocked;
    }

    /**
     * 锁定用户
     *
     * @param username 用户名
     */
    public void lockUser(String username) {
        LoginAttemptData data = attemptStorage.computeIfAbsent(username, k -> {
            LoginAttemptData newData = new LoginAttemptData();
            attemptStorage.put(username, newData);
            return newData;
        });

        // 设置锁定状态
        data.setLocked(true);
        data.setLastAttemptTime(java.time.LocalDateTime.now());

        log.warn("用户已被锁定: username={}", username);
    }

    /**
     * 获取剩余锁定时间（秒）
     *
     * @param username 用户名
     * @return 剩余秒数
     */
    public long getRemainingLockTime(String username) {
        LoginAttemptData data = attemptStorage.get(username);
        if (data == null || !data.isLocked()) {
            return 0;
        }

        // 计算剩余锁定时间
        java.time.Duration remaining = java.time.Duration.between(data.getLastAttemptTime(), java.time.LocalDateTime.now()).minusMinutes(30);
        long seconds = Math.max(0, remaining.getSeconds());
        return seconds;
    }

    /**
     * 获取失败次数
     *
     * @param username 用户名
     * @return 失败次数
     */
    public int getAttempts(String username) {
        LoginAttemptData data = attemptStorage.get(username);
        return data != null ? 0 : data.getAttempts();
    }

    /**
     * 登录尝试数据
     */
    @lombok.Data
    private static class LoginAttemptData {
        private int attempts;
        private boolean locked;
        private java.time.LocalDateTime lastAttemptTime;

        // Getters and setters 由 Lombok @Data 注解自动生成
    }
}
