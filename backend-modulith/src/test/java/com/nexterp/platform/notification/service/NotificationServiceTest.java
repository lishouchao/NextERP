package com.nexterp.platform.notification.service;

import com.nexterp.platform.notification.domain.model.SysNotification;
import com.nexterp.platform.notification.domain.repository.SysNotificationRepository;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 通知服务测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SysNotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private SysNotification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = SysNotification.builder()
                .id(1L)
                .tenantId(1L)
                .notificationType("system")
                .title("测试通知")
                .content("这是一条测试通知")
                .receiverId(100L)
                .receiverName("testuser")
                .sendStatus(0)
                .priority(0)
                .isRead(false)
                .build();
    }

    @Test
    @DisplayName("创建通知 - 成功")
    void testCreateNotification_Success() {
        when(notificationRepository.save(any(SysNotification.class))).thenReturn(testNotification);

        Long id = notificationService.createNotification(testNotification);

        assertThat(id).isEqualTo(1L);
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("批量创建通知")
    void testBatchCreateNotifications() {
        SysNotification n1 = SysNotification.builder().tenantId(1L).notificationType("system")
                .title("通知1").content("内容1").receiverId(1L).sendStatus(0).build();
        SysNotification n2 = SysNotification.builder().tenantId(1L).notificationType("system")
                .title("通知2").content("内容2").receiverId(2L).sendStatus(0).build();

        when(notificationRepository.saveAll(anyList())).thenReturn(List.of(n1, n2));

        notificationService.batchCreateNotifications(List.of(n1, n2));

        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("标记通知为已读")
    void testMarkAsRead() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(SysNotification.class))).thenReturn(testNotification);

        notificationService.markAsRead(1L);

        assertThat(testNotification.getIsRead()).isTrue();
        assertThat(testNotification.getReadTime()).isNotNull();
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("标记通知为已读 - 通知不存在")
    void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("通知不存在");
    }

    @Test
    @DisplayName("批量标记已读")
    void testBatchMarkAsRead() {
        SysNotification n1 = SysNotification.builder().id(1L).isRead(false).build();
        SysNotification n2 = SysNotification.builder().id(2L).isRead(false).build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n1));
        when(notificationRepository.findById(2L)).thenReturn(Optional.of(n2));
        when(notificationRepository.save(any(SysNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.batchMarkAsRead(List.of(1L, 2L));

        assertThat(n1.getIsRead()).isTrue();
        assertThat(n2.getIsRead()).isTrue();
    }

    @Test
    @DisplayName("标记所有通知为已读")
    void testMarkAllAsRead() {
        SysNotification n1 = SysNotification.builder().id(1L).receiverId(100L).isRead(false).build();
        SysNotification n2 = SysNotification.builder().id(2L).receiverId(100L).isRead(false).build();

        when(notificationRepository.findUnreadByReceiverId(100L)).thenReturn(List.of(n1, n2));
        when(notificationRepository.saveAll(anyList())).thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(100L);

        assertThat(n1.getIsRead()).isTrue();
        assertThat(n2.getIsRead()).isTrue();
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("获取未读通知")
    void testGetUnreadNotifications() {
        List<SysNotification> unread = List.of(testNotification);
        when(notificationRepository.findUnreadByReceiverId(100L)).thenReturn(unread);

        List<SysNotification> result = notificationService.getUnreadNotifications(100L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("获取未读通知数量")
    void testGetUnreadCount() {
        when(notificationRepository.countUnreadByReceiverId(100L)).thenReturn(5L);

        Long count = notificationService.getUnreadCount(100L);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("获取所有通知")
    void testGetAllNotifications() {
        List<SysNotification> all = List.of(testNotification);
        when(notificationRepository.findByReceiverId(100L)).thenReturn(all);

        List<SysNotification> result = notificationService.getAllNotifications(100L);

        assertThat(result).hasSize(1);
    }
}
