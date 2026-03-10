package com.nexterp.business.production.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 工序
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pro_work_process")
public class ProWorkProcess extends TenantAwareEntity {

    /**
     * 工序编码
     */
    @Column(name = "process_code", nullable = false, length = 50)
    private String processCode;

    /**
     * 工序名称
     */
    @Column(name = "process_name", nullable = false, length = 100)
    private String processName;

    /**
     * 工序类型 (1-普通工序 2-外协工序 3-质检工序)
     */
    @Column(name = "process_type", nullable = false)
    private Integer processType;

    /**
     * 工序分类ID
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 工序分类名称
     */
    @Column(name = "category_name", length = 50)
    private String categoryName;

    /**
     * 负责部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 负责部门名称
     */
    @Column(name = "department_name", length = 100)
    private String departmentName;

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
     * 标准工时 (分钟)
     */
    @Column(name = "standard_man_hours", precision = 19, scale = 4)
    private BigDecimal standardManHours;

    /**
     * 标准机时 (分钟)
     */
    @Column(name = "standard_machine_hours", precision = 19, scale = 4)
    private BigDecimal standardMachineHours;

    /**
     * 准备时间 (分钟)
     */
    @Column(name = "setup_time", precision = 10, scale = 2)
    private BigDecimal setupTime;

    /**
     * 等待时间 (分钟)
     */
    @Column(name = "wait_time", precision = 10, scale = 2)
    private BigDecimal waitTime;

    /**
     * 人工费率
     */
    @Column(name = "labor_rate", precision = 19, scale = 4)
    private BigDecimal laborRate;

    /**
     * 机器费率
     */
    @Column(name = "machine_rate", precision = 19, scale = 4)
    private BigDecimal machineRate;

    /**
     * 变动制造费率
     */
    @Column(name = "variable_overhead_rate", precision = 19, scale = 4)
    private BigDecimal variableOverheadRate;

    /**
     * 固定制造费率
     */
    @Column(name = "fixed_overhead_rate", precision = 19, scale = 4)
    private BigDecimal fixedOverheadRate;

    /**
     * 最小批量
     */
    @Column(name = "min_batch_qty", precision = 19, scale = 4)
    private BigDecimal minBatchQty;

    /**
     * 最大批量
     */
    @Column(name = "max_batch_qty", precision = 19, scale = 4)
    private BigDecimal maxBatchQty;

    /**
     * 是否瓶颈工序
     */
    @Column(name = "is_bottleneck", nullable = false)
    private Boolean isBottleneck;

    /**
     * 是否质检工序
     */
    @Column(name = "is_quality_check", nullable = false)
    private Boolean isQualityCheck;

    /**
     * 质检方案ID
     */
    @Column(name = "qc_plan_id")
    private Long qcPlanId;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 状态 (0-禁用 1-启用)
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 工艺路线明细列表
     */
    @OneToMany(mappedBy = "workProcess", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNo ASC")
    private List<ProRoutingDetail> routingDetails = new ArrayList<>();

    /**
     * 获取工序类型名称
     */
    public String getProcessTypeName() {
        return switch (processType) {
            case 1 -> "普通工序";
            case 2 -> "外协工序";
            case 3 -> "质检工序";
            default -> "未知";
        };
    }

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
