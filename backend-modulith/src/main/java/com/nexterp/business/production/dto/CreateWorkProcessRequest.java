package com.nexterp.business.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建/更新工序请求DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkProcessRequest {

    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @NotBlank(message = "工序编码不能为空")
    private String processCode;

    @NotBlank(message = "工序名称不能为空")
    private String processName;

    @NotNull(message = "工序类型不能为空")
    private Integer processType;

    private Long categoryId;
    private String categoryName;
    private Long departmentId;
    private String departmentName;
    private Long workCenterId;
    private String workCenterName;
    private BigDecimal standardManHours;
    private BigDecimal standardMachineHours;
    private BigDecimal setupTime;
    private BigDecimal waitTime;
    private BigDecimal laborRate;
    private BigDecimal machineRate;
    private BigDecimal variableOverheadRate;
    private BigDecimal fixedOverheadRate;
    private BigDecimal minBatchQty;
    private BigDecimal maxBatchQty;

    @NotNull(message = "是否瓶颈工序不能为空")
    private Boolean isBottleneck;

    @NotNull(message = "是否质检工序不能为空")
    private Boolean isQualityCheck;

    private Long qcPlanId;
    private Integer sortOrder;
    private String remark;
}
