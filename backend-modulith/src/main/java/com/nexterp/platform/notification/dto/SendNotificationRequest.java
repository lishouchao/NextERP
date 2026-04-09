package com.nexterp.platform.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 发送通知请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送通知请求")
public class SendNotificationRequest {

    @Schema(description = "通知类型", required = true)
    @NotBlank(message = "通知类型不能为空")
    private String notificationType;

    @Schema(description = "标题", required = true)
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "接收人ID", required = true)
    @NotNull(message = "接收人ID不能为空")
    private Long receiverId;

    @Schema(description = "接收人名称")
    private String receiverName;

    @Schema(description = "优先级 (0-普通 1-重要 2-紧急)")
    private Integer priority;

    @Schema(description = "关联业务类型")
    private String bizType;

    @Schema(description = "关联业务ID")
    private Long bizId;

    @Schema(description = "模板编码 (使用模板发送时)")
    private String templateCode;

    @Schema(description = "模板变量")
    private Map<String, Object> templateVariables;
}
