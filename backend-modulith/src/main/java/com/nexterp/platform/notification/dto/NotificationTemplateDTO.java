package com.nexterp.platform.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知模板 DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知模板")
public class NotificationTemplateDTO {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "模板编码", required = true)
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    @Schema(description = "模板名称", required = true)
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    @Schema(description = "通知类型 (system-系统 email-邮件 sms-短信 push-推送)")
    @NotBlank(message = "通知类型不能为空")
    private String notificationType;

    @Schema(description = "标题模板")
    private String titleTemplate;

    @Schema(description = "内容模板")
    private String contentTemplate;

    @Schema(description = "变量说明 (JSON)")
    private String variables;

    @Schema(description = "状态 (0-禁用 1-启用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
