package com.nexterp.platform.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程实例信息 DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流程实例信息")
public class ProcessInstanceInfoDTO {

    @Schema(description = "流程实例ID")
    private String processInstanceId;

    @Schema(description = "流程定义Key")
    private String processDefinitionKey;

    @Schema(description = "流程定义名称")
    private String processDefinitionName;

    @Schema(description = "业务Key")
    private String businessKey;

    @Schema(description = "发起人")
    private String initiator;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "持续时间(毫秒)")
    private Long duration;

    @Schema(description = "状态 (running-运行中 suspended-已挂起 finished-已结束)")
    private String status;

    @Schema(description = "流程变量")
    private Map<String, Object> variables;
}
