package com.nexterp.platform.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 完成任务请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "完成任务请求")
public class CompleteTaskRequest {

    @Schema(description = "任务ID", required = true)
    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    @Schema(description = "流程变量")
    private Map<String, Object> variables;

    @Schema(description = "审批意见")
    private String comment;

    @Schema(description = "审批结果 (approved-通过 rejected-驳回)")
    private String approvalResult;
}
