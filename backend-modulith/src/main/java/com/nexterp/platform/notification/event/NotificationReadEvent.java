package com.nexterp.platform.notification.event;

import java.time.LocalDateTime;

/**
 * 通知已读事件
 *
 * @author NextERP
 */
public record NotificationReadEvent(
        /**
         * 通知ID
         */
        Long notificationId,

        /**
         * 接收人ID
         */
        Long receiverId,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 阅读时间
         */
        LocalDateTime readTime
) {
    public NotificationReadEvent {
        if (readTime == null) {
            readTime = LocalDateTime.now();
        }
    }
}
