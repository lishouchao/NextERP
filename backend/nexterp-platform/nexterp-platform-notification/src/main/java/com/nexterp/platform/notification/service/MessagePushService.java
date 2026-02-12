package com.nexterp.platform.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 消息推送服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePushService {

    private static final String PUSH_QUEUE_PREFIX = "push:queue:";
    private static final String USER_PUSH_PREFIX = "user:push:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 发送推送消息
     *
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @param data 附加数据
     */
    public void sendPush(Long userId, String title, String content, Map<String, Object> data) {
        PushMessage message = PushMessage.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();

        // 将消息加入推送队列
        String queueKey = PUSH_QUEUE_PREFIX + userId;
        redisTemplate.opsForList().rightPush(queueKey, message);

        // 设置过期时间（24小时）
        redisTemplate.expire(queueKey, Duration.ofHours(24));

        // 设置用户推送标记
        String userKey = USER_PUSH_PREFIX + userId;
        redisTemplate.opsForValue().set(userKey, "1", 1, TimeUnit.HOURS);

        log.info("发送推送消息: userId={}, title={}", userId, title);
    }

    /**
     * 获取用户待推送消息
     *
     * @param userId 用户ID
     * @return 消息列表
     */
    public PushMessage getPushMessage(Long userId) {
        String queueKey = PUSH_QUEUE_PREFIX + userId;
        Object message = redisTemplate.opsForList().leftPop(queueKey);

        if (message != null) {
            return (PushMessage) message;
        }

        return null;
    }

    /**
     * 获取用户待推送消息数量
     *
     * @param userId 用户ID
     * @return 消息数量
     */
    public Long getPushMessageCount(Long userId) {
        String queueKey = PUSH_QUEUE_PREFIX + userId;
        return redisTemplate.opsForList().size(queueKey);
    }

    /**
     * 清除用户推送标记
     *
     * @param userId 用户ID
     */
    public void clearPushFlag(Long userId) {
        String userKey = USER_PUSH_PREFIX + userId;
        redisTemplate.delete(userKey);
    }

    /**
     * 检查用户是否有新推送
     *
     * @param userId 用户ID
     * @return 是否有新推送
     */
    public boolean hasNewPush(Long userId) {
        String userKey = USER_PUSH_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(userKey));
    }

    /**
     * 推送消息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PushMessage {
        private Long userId;
        private String title;
        private String content;
        private Map<String, Object> data;
        private Long timestamp;
    }
}
