package com.nexterp.platform.notification.controller;

import com.nexterp.platform.notification.service.MessagePushService;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息推送控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notification/push")
@RequiredArgsConstructor
public class MessagePushController {

    private final MessagePushService messagePushService;

    /**
     * 发送推送消息
     *
     * @param request 推送请求
     * @return 成功响应
     */
    @PostMapping("/send")
    public Result<Void> sendPush(@RequestBody PushRequest request) {
        messagePushService.sendPush(
                request.getUserId(),
                request.getTitle(),
                request.getContent(),
                request.getData()
        );
        return Result.success();
    }

    /**
     * 获取用户待推送消息
     *
     * @param userId 用户ID
     * @return 消息
     */
    @GetMapping("/message")
    public Result<MessagePushService.PushMessage> getPushMessage(@RequestParam Long userId) {
        MessagePushService.PushMessage message = messagePushService.getPushMessage(userId);
        return Result.success(message);
    }

    /**
     * 获取用户待推送消息数量
     *
     * @param userId 用户ID
     * @return 消息数量
     */
    @GetMapping("/count")
    public Result<Long> getPushMessageCount(@RequestParam Long userId) {
        Long count = messagePushService.getPushMessageCount(userId);
        return Result.success(count);
    }

    /**
     * 检查是否有新推送
     *
     * @param userId 用户ID
     * @return 是否有新推送
     */
    @GetMapping("/has-new")
    public Result<Boolean> hasNewPush(@RequestParam Long userId) {
        boolean hasNew = messagePushService.hasNewPush(userId);
        return Result.success(hasNew);
    }

    /**
     * 清除推送标记
     *
     * @param userId 用户ID
     * @return 成功响应
     */
    @PostMapping("/clear-flag")
    public Result<Void> clearPushFlag(@RequestParam Long userId) {
        messagePushService.clearPushFlag(userId);
        return Result.success();
    }

    /**
     * 推送请求
     */
    public static class PushRequest {
        private Long userId;
        private String title;
        private String content;
        private Map<String, Object> data;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }
}
