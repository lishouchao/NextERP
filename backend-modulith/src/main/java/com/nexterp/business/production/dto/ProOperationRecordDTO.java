package com.nexterp.business.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工序执行记录响应DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProOperationRecordDTO {

    private Long id;

    private Long productionOrderId;

    private Integer sequenceNo;

    private Long processId;

    private String processCode;

    private String processName;

    private Long workCenterId;

    private String workCenterName;

    private BigDecimal plannedQty;

    private BigDecimal completedQty;

    private BigDecimal qualifiedQty;

    private BigDecimal scrappedQty;

    private LocalDateTime planStartTime;

    private LocalDateTime planEndTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private Long workerId;

    private String workerName;

    private Integer status;

    private String statusName;

    private BigDecimal actualManHours;

    private BigDecimal actualMachineHours;

    private String remark;

    private BigDecimal completionRate;

    private BigDecimal qualifiedRate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
