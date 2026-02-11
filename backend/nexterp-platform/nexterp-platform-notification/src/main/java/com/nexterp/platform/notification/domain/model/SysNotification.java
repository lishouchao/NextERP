package com.nexterp.platform.notification.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知实体
 *
 * @author NextERP
 */
@Entity
@Table(name = "sys_notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysNotification extends BaseEntity {

    /**
     * 通知ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * 通知类型 (system-系统 message-消息 email-邮件)
     */
    @Column(name = "notification_type", nullable = false, length = 20)
    private String notificationType;

    /**
     * 标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 接收人ID
     */
    @Column(name = "receiver_id")
    private Long receiverId;

    /**
     * 接收人名称
     */
    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    /**
     * 发送状态 (0-待发送 1-发送中 2-发送成功 3-发送失败)
     */
    @Column(name = "send_status", nullable = false)
    private Integer sendStatus = 0;

    /**
     * 发送时间
     */
    @Column(name = "send_time")
    private LocalDateTime sendTime;

    /**
     * 失败原因
     */
    @Column(name = "fail_reason", length = 500)
    private String failReason;

    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /**
     * 优先级 (0-普通 1-重要 2-紧急)
     */
    @Column(name = "priority")
    private Integer priority = 0;

    /**
     * 是否已读
     */
    @Column(name = "is_read")
    private Boolean isRead = false;

    /**
     * 阅读时间
     */
    @Column(name = "read_time")
    private LocalDateTime readTime;

    /**
     * 关联业务类型
     */
    @Column(name = "biz_type", length = 50)
    private String bizType;

    /**
     * 关联业务ID
     */
    @Column(name = "biz_id")
    private Long bizId;
}
