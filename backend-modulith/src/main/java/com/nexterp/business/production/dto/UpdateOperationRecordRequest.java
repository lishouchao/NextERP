package com.nexterp.business.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新工序执行记录请求DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOperationRecordRequest {

    /**
     * 完工数量
     */
    private BigDecimal completedQty;

    /**
     * 合格数量
     */
    private BigDecimal qualifiedQty;

    /**
     * 报废数量
     */
    private BigDecimal scrappedQty;

    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    private LocalDateTime actualEndTime;

    /**
     * 实际工时 (分钟)
     */
    private BigDecimal actualManHours;

    /**
     * 实际机时 (分钟)
     */
    private BigDecimal actualMachineHours;

    /**
     * 状态 (0-待开工 1-进行中 2-已完成 3-已暂停)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
