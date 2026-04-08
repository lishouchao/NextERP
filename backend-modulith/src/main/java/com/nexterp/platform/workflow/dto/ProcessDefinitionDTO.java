package com.nexterp.platform.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 流程定义 DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流程定义")
public class ProcessDefinitionDTO {

    @Schema(description = "定义ID")
    private Long id;

    @Schema(description = "流程Key", required = true)
    @NotBlank(message = "流程Key不能为空")
    private String processKey;

    @Schema(description = "流程名称", required = true)
    @NotBlank(message = "流程名称不能为空")
    private String processName;

    @Schema(description = "流程版本")
    private Integer version;

    @Schema(description = "流程描述")
    private String description;

    @Schema(description = "BPMN XML")
    private String bpmnXml;

    @Schema(description = "流程分类")
    private String category;

    @Schema(description = "状态 (0-草稿 1-发布 2-已归档)")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
