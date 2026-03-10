package com.nexterp.platform.notification.service;

import com.nexterp.platform.notification.domain.model.SysNotification;
import com.nexterp.platform.notification.domain.repository.SysNotificationRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SysNotificationRepository notificationRepository;
    private final EmailService emailService;

    /**
     * 创建通知
     *
     * @param notification 通知
     * @return 通知ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createNotification(SysNotification notification) {
        SysNotification saved = notificationRepository.save(notification);
        log.info("创建通知成功: id={}, type={}, receiverId={}",
            saved.getId(), saved.getNotificationType(), saved.getReceiverId());

        // 异步发送通知
        sendNotificationAsync(saved.getId());

        return saved.getId();
    }

    /**
     * 批量创建通知
     *
     * @param notifications 通知列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateNotifications(List<SysNotification> notifications) {
        List<SysNotification> saved = notificationRepository.saveAll(notifications);
        log.info("批量创建通知成功: count={}", saved.size());

        // 异步发送通知
        for (SysNotification notification : saved) {
            sendNotificationAsync(notification.getId());
        }
    }

    /**
     * 异步发送通知
     *
     * @param notificationId 通知ID
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void sendNotificationAsync(Long notificationId) {
        try {
            SysNotification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new BusinessException("通知不存在"));

            // 更新发送状态为发送中
            notification.setSendStatus(1);
            notificationRepository.save(notification);

            // 根据类型发送通知
            switch (notification.getNotificationType()) {
                case "email":
                    sendEmailNotification(notification);
                    break;
                case "system":
                    // 系统通知直接标记为已发送
                    notification.setSendStatus(2);
                    notification.setSendTime(LocalDateTime.now());
                    break;
                default:
                    log.warn("不支持的通知类型: {}", notification.getNotificationType());
                    notification.setSendStatus(3);
                    notification.setFailReason("不支持的通知类型");
            }

            notificationRepository.save(notification);
            log.info("发送通知成功: id={}", notificationId);
        } catch (Exception e) {
            log.error("发送通知失败: id={}", notificationId, e);

            // 更新失败状态
            notificationRepository.findById(notificationId).ifPresent(notification -> {
                notification.setSendStatus(3);
                notification.setFailReason(e.getMessage());
                notification.setRetryCount(notification.getRetryCount() + 1);
                notificationRepository.save(notification);
            });
        }
    }

    /**
     * 发送邮件通知
     *
     * @param notification 通知
     */
    private void sendEmailNotification(SysNotification notification) {
        // 这里需要获取接收人的邮箱地址
        // 暂时使用一个默认的邮箱，实际应该从用户表中获取
        String toEmail = notification.getReceiverName(); // 假设这里存储的是邮箱

        try {
            emailService.sendHtmlEmail(toEmail, notification.getTitle(), notification.getContent());
            notification.setSendStatus(2);
            notification.setSendTime(LocalDateTime.now());
        } catch (Exception e) {
            notification.setSendStatus(3);
            notification.setFailReason(e.getMessage());
            throw e;
        }
    }

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId) {
        SysNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("通知不存在"));

        notification.setIsRead(true);
        notification.setReadTime(LocalDateTime.now());
        notificationRepository.save(notification);

        log.info("标记通知为已读: id={}", notificationId);
    }

    /**
     * 批量标记通知为已读
     *
     * @param notificationIds 通知ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkAsRead(List<Long> notificationIds) {
        for (Long notificationId : notificationIds) {
            notificationRepository.findById(notificationId).ifPresent(notification -> {
                notification.setIsRead(true);
                notification.setReadTime(LocalDateTime.now());
                notificationRepository.save(notification);
            });
        }
        log.info("批量标记通知为已读: count={}", notificationIds.size());
    }

    /**
     * 标记所有通知为已读
     *
     * @param receiverId 接收人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long receiverId) {
        List<SysNotification> unreadNotifications =
                notificationRepository.findUnreadByReceiverId(receiverId);

        for (SysNotification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadTime(LocalDateTime.now());
        }
        notificationRepository.saveAll(unreadNotifications);

        log.info("标记所有通知为已读: receiverId={}, count={}", receiverId, unreadNotifications.size());
    }

    /**
     * 获取用户未读通知
     *
     * @param receiverId 接收人ID
     * @return 通知列表
     */
    public List<SysNotification> getUnreadNotifications(Long receiverId) {
        return notificationRepository.findUnreadByReceiverId(receiverId);
    }

    /**
     * 获取用户所有通知
     *
     * @param receiverId 接收人ID
     * @return 通知列表
     */
    public List<SysNotification> getAllNotifications(Long receiverId) {
        return notificationRepository.findByReceiverId(receiverId);
    }

    /**
     * 获取未读通知数量
     *
     * @param receiverId 接收人ID
     * @return 未读数量
     */
    public Long getUnreadCount(Long receiverId) {
        return notificationRepository.countUnreadByReceiverId(receiverId);
    }

    /**
     * 处理待发送的通知
     */
    @Transactional(rollbackFor = Exception.class)
    public void processPendingNotifications() {
        List<SysNotification> pendingNotifications =
                notificationRepository.findPendingNotifications(LocalDateTime.now());

        log.info("处理待发送通知: count={}", pendingNotifications.size());

        for (SysNotification notification : pendingNotifications) {
            sendNotificationAsync(notification.getId());
        }
    }
}
