package com.nexterp.platform.notification.controller;

import com.nexterp.platform.notification.domain.model.SysNotification;
import com.nexterp.platform.notification.service.NotificationService;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 创建通知
     *
     * @param notification 通知
     * @return 通知ID
     */
    @PostMapping
    public Result<Long> createNotification(@RequestBody SysNotification notification) {
        Long id = notificationService.createNotification(notification);
        return Result.success(id);
    }

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @return 成功响应
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    /**
     * 批量标记通知为已读
     *
     * @param ids 通知ID列表
     * @return 成功响应
     */
    @PutMapping("/batch/read")
    public Result<Void> batchMarkAsRead(@RequestBody List<Long> ids) {
        notificationService.batchMarkAsRead(ids);
        return Result.success();
    }

    /**
     * 标记所有通知为已读
     *
     * @param receiverId 接收人ID
     * @return 成功响应
     */
    @PutMapping("/all/read")
    public Result<Void> markAllAsRead(@RequestParam Long receiverId) {
        notificationService.markAllAsRead(receiverId);
        return Result.success();
    }

    /**
     * 获取用户未读通知
     *
     * @param receiverId 接收人ID
     * @return 通知列表
     */
    @GetMapping("/unread")
    public Result<List<SysNotification>> getUnreadNotifications(@RequestParam Long receiverId) {
        List<SysNotification> notifications = notificationService.getUnreadNotifications(receiverId);
        return Result.success(notifications);
    }

    /**
     * 获取用户所有通知
     *
     * @param receiverId 接收人ID
     * @return 通知列表
     */
    @GetMapping("/all")
    public Result<List<SysNotification>> getAllNotifications(@RequestParam Long receiverId) {
        List<SysNotification> notifications = notificationService.getAllNotifications(receiverId);
        return Result.success(notifications);
    }

    /**
     * 获取未读通知数量
     *
     * @param receiverId 接收人ID
     * @return 未读数量
     */
    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount(@RequestParam Long receiverId) {
        Long count = notificationService.getUnreadCount(receiverId);
        return Result.success(count);
    }
}
