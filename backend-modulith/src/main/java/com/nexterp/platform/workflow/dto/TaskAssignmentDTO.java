package com.nexterp.platform.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务分配规则 DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务分配规则")
public class TaskAssignmentDTO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "流程定义Key")
    private String processKey;

    @Schema(description = "任务定义Key", required = true)
    @NotBlank(message = "任务Key不能为空")
    private String taskKey;

    @Schema(description = "分配类型 (user-用户 role-角色 dept-部门 expression-表达式)", required = true)
    @NotBlank(message = "分配类型不能为空")
    private String assignmentType;

    @Schema(description = "分配值", required = true)
    @NotBlank(message = "分配值不能为空")
    private String assignmentValue;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
