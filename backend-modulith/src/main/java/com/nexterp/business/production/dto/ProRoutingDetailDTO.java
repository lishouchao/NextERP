package com.nexterp.business.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 工艺路线明细DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProRoutingDetailDTO {

    private Long id;

    private Long routingId;

    private Integer sequenceNo;

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

    private Boolean isParallel;

    private Boolean isOverlap;

    private Integer parallelGroupNo;

    private Integer nextSequenceNo;

    private Long alternativeProcessId;

    private String checkItems;

    private String remark;
}
