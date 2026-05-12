package com.nexterp.business.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工序响应DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProWorkProcessDTO {

    private Long id;
    private Long tenantId;
    private String processCode;
    private String processName;
    private Integer processType;
    private String processTypeName;
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
    private Boolean isBottleneck;
    private Boolean isQualityCheck;
    private Long qcPlanId;
    private Integer sortOrder;
    private Integer status;
    private String statusName;
    private String remark;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
