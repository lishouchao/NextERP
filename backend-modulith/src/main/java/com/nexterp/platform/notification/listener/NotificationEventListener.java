package com.nexterp.platform.notification.listener;

import com.nexterp.platform.notification.event.NotificationReadEvent;
import com.nexterp.platform.notification.event.NotificationSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 通知事件处理器
 *
 * @author NextERP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    /**
     * 处理通知发送事件
     */
    @ApplicationModuleListener
    public void handleNotificationSent(NotificationSentEvent event) {
        log.info("处理通知发送事件: notificationId={}, type={}, receiverId={}, success={}",
                event.notificationId(), event.notificationType(),
                event.receiverId(), event.success());

        if (!event.success()) {
            log.warn("通知发送失败: notificationId={}, reason={}",
                    event.notificationId(), event.failReason());
            // TODO: 记录失败日志，触发重试机制
        }
    }

    /**
     * 处理通知已读事件
     */
    @ApplicationModuleListener
    public void handleNotificationRead(NotificationReadEvent event) {
        log.info("处理通知已读事件: notificationId={}, receiverId={}",
                event.notificationId(), event.receiverId());
        // TODO: 统计通知阅读率，更新用户消息偏好
    }
}
