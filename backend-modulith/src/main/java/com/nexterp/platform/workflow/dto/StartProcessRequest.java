package com.nexterp.platform.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 启动流程请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "启动流程请求")
public class StartProcessRequest {

    @Schema(description = "流程定义Key", required = true)
    @NotBlank(message = "流程定义Key不能为空")
    private String processDefinitionKey;

    @Schema(description = "业务Key")
    private String businessKey;

    @Schema(description = "流程变量")
    private Map<String, Object> variables;

    @Schema(description = "发起人ID")
    private String initiator;
}
