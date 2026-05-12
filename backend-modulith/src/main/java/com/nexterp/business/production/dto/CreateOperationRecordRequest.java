package com.nexterp.business.production.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建工序执行记录请求DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOperationRecordRequest {

    /**
     * 生产订单ID
     */
    @NotNull(message = "生产订单ID不能为空")
    private Long productionOrderId;

    /**
     * 顺序号
     */
    @NotNull(message = "顺序号不能为空")
    private Integer sequenceNo;

    /**
     * 工序ID
     */
    @NotNull(message = "工序ID不能为空")
    private Long processId;

    /**
     * 工序编码
     */
    private String processCode;

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 工作中心ID
     */
    private Long workCenterId;

    /**
     * 工作中心名称
     */
    private String workCenterName;

    /**
     * 计划数量
     */
    private BigDecimal plannedQty;

    /**
     * 计划开始时间
     */
    private LocalDateTime planStartTime;

    /**
     * 计划结束时间
     */
    private LocalDateTime planEndTime;

    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    private LocalDateTime actualEndTime;

    /**
     * 报工人员ID
     */
    private Long workerId;

    /**
     * 报工人员姓名
     */
    private String workerName;

    /**
     * 实际工时 (分钟)
     */
    private BigDecimal actualManHours;

    /**
     * 实际机时 (分钟)
     */
    private BigDecimal actualMachineHours;

    /**
     * 备注
     */
    private String remark;
}
