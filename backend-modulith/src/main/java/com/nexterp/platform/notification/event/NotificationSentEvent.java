package com.nexterp.platform.notification.event;

import java.time.LocalDateTime;

/**
 * 通知发送事件
 *
 * @author NextERP
 */
public record NotificationSentEvent(
        /**
         * 通知ID
         */
        Long notificationId,

        /**
         * 通知类型
         */
        String notificationType,

        /**
         * 标题
         */
        String title,

        /**
         * 接收人ID
         */
        Long receiverId,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 发送时间
         */
        LocalDateTime sentTime,

        /**
         * 是否成功
         */
        boolean success,

        /**
         * 失败原因
         */
        String failReason
) {
    public NotificationSentEvent {
        if (sentTime == null) {
            sentTime = LocalDateTime.now();
        }
    }
}
