package com.nexterp.platform.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知 DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知信息")
public class NotificationDTO {

    @Schema(description = "通知ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "通知类型 (system-系统 email-邮件 sms-短信 push-推送)")
    private String notificationType;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "接收人ID")
    private Long receiverId;

    @Schema(description = "接收人名称")
    private String receiverName;

    @Schema(description = "发送状态 (0-待发送 1-发送中 2-成功 3-失败)")
    private Integer sendStatus;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "优先级 (0-普通 1-重要 2-紧急)")
    private Integer priority;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "关联业务类型")
    private String bizType;

    @Schema(description = "关联业务ID")
    private Long bizId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
