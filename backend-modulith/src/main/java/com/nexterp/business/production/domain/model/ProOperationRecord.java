package com.nexterp.business.production.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工序执行记录
 *
 * @author NextERP
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pro_operation_record")
public class ProOperationRecord extends BaseEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 生产订单ID
     */
    @Column(name = "production_order_id", nullable = false)
    private Long productionOrderId;

    /**
     * 顺序号
     */
    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;

    /**
     * 工序ID
     */
    @Column(name = "process_id", nullable = false)
    private Long processId;

    /**
     * 工序编码
     */
    @Column(name = "process_code", length = 50)
    private String processCode;

    /**
     * 工序名称
     */
    @Column(name = "process_name", length = 100)
    private String processName;

    /**
     * 工作中心ID
     */
    @Column(name = "work_center_id")
    private Long workCenterId;

    /**
     * 工作中心名称
     */
    @Column(name = "work_center_name", length = 100)
    private String workCenterName;

    /**
     * 计划数量
     */
    @Column(name = "planned_qty", precision = 19, scale = 4)
    private BigDecimal plannedQty;

    /**
     * 完工数量
     */
    @Column(name = "completed_qty", precision = 19, scale = 4)
    private BigDecimal completedQty;

    /**
     * 合格数量
     */
    @Column(name = "qualified_qty", precision = 19, scale = 4)
    private BigDecimal qualifiedQty;

    /**
     * 报废数量
     */
    @Column(name = "scrapped_qty", precision = 19, scale = 4)
    private BigDecimal scrappedQty;

    /**
     * 计划开始时间
     */
    @Column(name = "plan_start_time")
    private LocalDateTime planStartTime;

    /**
     * 计划结束时间
     */
    @Column(name = "plan_end_time")
    private LocalDateTime planEndTime;

    /**
     * 实际开始时间
     */
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * 报工人员ID
     */
    @Column(name = "worker_id")
    private Long workerId;

    /**
     * 报工人员姓名
     */
    @Column(name = "worker_name", length = 50)
    private String workerName;

    /**
     * 状态 (0-待开工 1-进行中 2-已完成 3-已暂停)
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 实际工时 (分钟)
     */
    @Column(name = "actual_man_hours", precision = 10, scale = 2)
    private BigDecimal actualManHours;

    /**
     * 实际机时 (分钟)
     */
    @Column(name = "actual_machine_hours", precision = 10, scale = 2)
    private BigDecimal actualMachineHours;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 关联生产订单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", insertable = false, updatable = false)
    private ProProductionOrder productionOrder;

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        return switch (status) {
            case 0 -> "待开工";
            case 1 -> "进行中";
            case 2 -> "已完成";
            case 3 -> "已暂停";
            default -> "未知";
        };
    }

    /**
     * 计算完工率
     *
     * @return 完工率
     */
    public BigDecimal getCompletionRate() {
        if (plannedQty == null || plannedQty.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal completed = completedQty != null ? completedQty : BigDecimal.ZERO;
        return completed.divide(plannedQty, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 计算合格率
     *
     * @return 合格率
     */
    public BigDecimal getQualifiedRate() {
        BigDecimal completed = completedQty != null ? completedQty : BigDecimal.ZERO;
        BigDecimal qualified = qualifiedQty != null ? qualifiedQty : BigDecimal.ZERO;
        if (completed.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return qualified.divide(completed, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
