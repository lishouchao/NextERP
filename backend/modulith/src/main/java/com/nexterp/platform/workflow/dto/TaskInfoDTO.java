package com.nexterp.platform.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 任务信息 DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务信息")
public class TaskInfoDTO {

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "任务描述")
    private String taskDescription;

    @Schema(description = "分配人")
    private String assignee;

    @Schema(description = "候选用户/组")
    private String candidateUsers;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "到期时间")
    private LocalDateTime dueDate;

    @Schema(description = "流程实例ID")
    private String processInstanceId;

    @Schema(description = "流程定义Key")
    private String processDefinitionKey;

    @Schema(description = "流程定义名称")
    private String processDefinitionName;

    @Schema(description = "业务Key")
    private String businessKey;

    @Schema(description = "任务状态 (active-活动 completed-完成)")
    private String status;

    @Schema(description = "流程变量")
    private Map<String, Object> variables;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "表单Key")
    private String formKey;
}
