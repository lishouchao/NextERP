package com.nexterp.business.production.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 生产订单
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pro_production_order")
public class ProProductionOrder extends TenantAwareEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 生产订单号
     */
    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;

    /**
     * 订单类型 (1-标准订单 2-返工订单 3-拆解订单)
     */
    @Column(name = "order_type", nullable = false)
    private Integer orderType;

    /**
     * 产品ID
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 产品编码
     */
    @Column(name = "product_code", length = 50)
    private String productCode;

    /**
     * 产品名称
     */
    @Column(name = "product_name", length = 100)
    private String productName;

    /**
     * 规格型号
     */
    @Column(name = "specification", length = 200)
    private String specification;

    /**
     * 单位
     */
    @Column(name = "unit", length = 20)
    private String unit;

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
     * 报废数量
     */
    @Column(name = "scrapped_qty", precision = 19, scale = 4)
    private BigDecimal scrappedQty;

    /**
     * BOM ID
     */
    @Column(name = "bom_id")
    private Long bomId;

    /**
     * BOM版本
     */
    @Column(name = "bom_version", length = 20)
    private String bomVersion;

    /**
     * 工艺路线ID
     */
    @Column(name = "routing_id")
    private Long routingId;

    /**
     * 计划开始日期
     */
    @Column(name = "plan_start_date")
    private LocalDate planStartDate;

    /**
     * 计划结束日期
     */
    @Column(name = "plan_end_date")
    private LocalDate planEndDate;

    /**
     * 实际开始日期
     */
    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    /**
     * 实际结束日期
     */
    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    /**
     * 生产车间ID
     */
    @Column(name = "workshop_id")
    private Long workshopId;

    /**
     * 生产车间名称
     */
    @Column(name = "workshop_name", length = 100)
    private String workshopName;

    /**
     * 生产产线ID
     */
    @Column(name = "production_line_id")
    private Long productionLineId;

    /**
     * 生产产线名称
     */
    @Column(name = "production_line_name", length = 100)
    private String productionLineName;

    /**
     * 状态 (0-草稿 1-已审核 2-生产中 3-已完工 4-已关闭 5-已取消)
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 优先级 (1-紧急 2-高 3-正常 4-低)
     */
    @Column(name = "priority", nullable = false)
    private Integer priority;

    /**
     * 来源类型
     */
    @Column(name = "source_type", length = 50)
    private String sourceType;

    /**
     * 来源单据ID
     */
    @Column(name = "source_id")
    private Long sourceId;

    /**
     * 来源单据号
     */
    @Column(name = "source_no", length = 50)
    private String sourceNo;

    /**
     * 需求人ID
     */
    @Column(name = "demand_user_id")
    private Long demandUserId;

    /**
     * 需求人姓名
     */
    @Column(name = "demand_user_name", length = 50)
    private String demandUserName;

    /**
     * 制单人ID
     */
    @Column(name = "created_by_id")
    private Long createdById;

    /**
     * 制单人姓名
     */
    @Column(name = "created_by_name", length = 50)
    private String createdByName;

    /**
     * 审核人ID
     */
    @Column(name = "approved_by_id")
    private Long approvedById;

    /**
     * 审核人姓名
     */
    @Column(name = "approved_by_name", length = 50)
    private String approvedByName;

    /**
     * 审核时间
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 附件 (JSON格式)
     */
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    /**
     * 生产订单明细列表
     */
    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNo ASC")
    private List<ProProductionOrderDetail> details = new ArrayList<>();

    /**
     * 工序执行记录列表
     */
    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNo ASC")
    private List<ProOperationRecord> operationRecords = new ArrayList<>();

    /**
     * 获取订单类型名称
     */
    public String getOrderTypeName() {
        return switch (orderType) {
            case 1 -> "标准订单";
            case 2 -> "返工订单";
            case 3 -> "拆解订单";
            default -> "未知";
        };
    }

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "已审核";
            case 2 -> "生产中";
            case 3 -> "已完工";
            case 4 -> "已关闭";
            case 5 -> "已取消";
            default -> "未知";
        };
    }

    /**
     * 获取优先级名称
     */
    public String getPriorityName() {
        return switch (priority) {
            case 1 -> "紧急";
            case 2 -> "高";
            case 3 -> "正常";
            case 4 -> "低";
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
     * 判断是否可以编辑
     */
    public boolean canEdit() {
        return status == 0 || status == 5;
    }

    /**
     * 判断是否可以审核
     */
    public boolean canApprove() {
        return status == 0;
    }

    /**
     * 判断是否可以开工
     */
    public boolean canStart() {
        return status == 1;
    }

    /**
     * 判断是否可以完工
     */
    public boolean canComplete() {
        return status == 2;
    }

    /**
     * 判断是否可以关闭
     */
    public boolean canClose() {
        return status == 3;
    }
}
