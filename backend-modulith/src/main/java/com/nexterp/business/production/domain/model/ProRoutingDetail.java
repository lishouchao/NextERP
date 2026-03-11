package com.nexterp.business.production.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 工艺路线明细
 *
 * @author NextERP
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pro_routing_detail")
public class ProRoutingDetail extends BaseEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 工艺路线ID
     */
    @Column(name = "routing_id", nullable = false)
    private Long routingId;

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
     * 移动时间 (分钟)
     */
    @Column(name = "move_time", precision = 10, scale = 2)
    private BigDecimal moveTime;

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
     * 是否并行工序
     */
    @Column(name = "is_parallel", nullable = false)
    private Boolean isParallel;

    /**
     * 是否重叠工序
     */
    @Column(name = "is_overlap", nullable = false)
    private Boolean isOverlap;

    /**
     * 并行组号
     */
    @Column(name = "parallel_group_no")
    private Integer parallelGroupNo;

    /**
     * 下一工序顺序号
     */
    @Column(name = "next_sequence_no")
    private Integer nextSequenceNo;

    /**
     * 替代工序ID
     */
    @Column(name = "alternative_process_id")
    private Long alternativeProcessId;

    /**
     * 检查项 (JSON格式)
     */
    @Column(name = "check_items", columnDefinition = "TEXT")
    private String checkItems;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 关联工艺路线
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routing_id", insertable = false, updatable = false)
    private ProRouting routing;

    /**
     * 关联工序
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", insertable = false, updatable = false)
    private ProWorkProcess workProcess;
}
