package com.nexterp.business.production.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 工艺路线明细请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingDetailRequest {

    @NotNull(message = "顺序号不能为空")
    private Integer sequenceNo;

    @NotNull(message = "工序ID不能为空")
    private Long processId;

    private String processCode;

    private String processName;

    private Long workCenterId;

    private String workCenterName;

    private BigDecimal standardManHours;

    private BigDecimal standardMachineHours;

    private BigDecimal setupTime;

    private BigDecimal waitTime;

    private BigDecimal moveTime;

    private BigDecimal laborRate;

    private BigDecimal machineRate;

    private BigDecimal variableOverheadRate;

    private BigDecimal fixedOverheadRate;

    private BigDecimal minBatchQty;

    private BigDecimal maxBatchQty;

    @NotNull(message = "是否并行工序不能为空")
    private Boolean isParallel;

    @NotNull(message = "是否重叠工序不能为空")
    private Boolean isOverlap;

    private String remark;
}
