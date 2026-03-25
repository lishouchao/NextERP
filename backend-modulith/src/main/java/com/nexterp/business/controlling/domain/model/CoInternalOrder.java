package com.nexterp.business.controlling.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 内部订单
 * 对标: SAP AUFK
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_internal_order")
public class CoInternalOrder extends TenantAwareEntity {

    /**
     * 订单ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单号
     */
    @Column(name = "order_number", nullable = false, length = 12)
    private String orderNumber;

    /**
     * 订单描述
     */
    @Column(name = "order_description", nullable = false, length = 100)
    private String orderDescription;

    /**
     * 订单类型 (01-间接费用订单 02-投资订单 03-维修订单 04-研发订单)
     */
    @Column(name = "order_type", nullable = false, length = 2)
    private String orderType;

    /**
     * 成本控制范围
     */
    @Column(name = "controlling_area", length = 4)
    private String controllingArea;

    /**
     * 公司代码
     */
    @Column(name = "company_code", length = 4)
    private String companyCode;

    /**
     * 业务范围
     */
    @Column(name = "business_area", length = 4)
    private String businessArea;

    /**
     * 利润中心
     */
    @Column(name = "profit_center", length = 10)
    private String profitCenter;

    /**
     * 负责成本中心ID
     */
    @Column(name = "responsible_cost_center_id")
    private Long responsibleCostCenterId;

    /**
     * 负责人ID
     */
    @Column(name = "person_responsible_id")
    private Long personResponsibleId;

    /**
     * 负责人姓名
     */
    @Column(name = "person_responsible_name", length = 50)
    private String personResponsibleName;

    /**
     * 订单开始日期
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 订单结束日期
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 订单状态 (01-创建 02-已下达 03-技术完成 04-已关闭)
     */
    @Column(name = "order_status", nullable = false, length = 2)
    @Builder.Default
    private String orderStatus = "01";

    /**
     * 结算规则类型 (01-按比例 02-按金额 03-全额)
     */
    @Column(name = "settlement_type", length = 2)
    private String settlementType;

    /**
     * 预算金额
     */
    @Column(name = "budget_amount", precision = 19, scale = 2)
    private BigDecimal budgetAmount;

    /**
     * 已分配金额
     */
    @Column(name = "allocated_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal allocatedAmount = BigDecimal.ZERO;

    /**
     * 实际成本
     */
    @Column(name = "actual_cost", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal actualCost = BigDecimal.ZERO;

    /**
     * 货币代码
     */
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 下达时间
     */
    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    /**
     * 完成时间
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 结算规则列表
     */
    @OneToMany(mappedBy = "internalOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoSettlementRule> settlementRules = new ArrayList<>();

    /**
     * 获取订单类型名称
     */
    public String getOrderTypeName() {
        return switch (orderType) {
            case "01" -> "间接费用订单";
            case "02" -> "投资订单";
            case "03" -> "维修订单";
            case "04" -> "研发订单";
            default -> "未知";
        };
    }

    /**
     * 获取订单状态名称
     */
    public String getOrderStatusName() {
        return switch (orderStatus) {
            case "01" -> "创建";
            case "02" -> "已下达";
            case "03" -> "技术完成";
            case "04" -> "已关闭";
            default -> "未知";
        };
    }

    /**
     * 判断是否可以下达
     */
    public boolean canRelease() {
        return "01".equals(orderStatus);
    }

    /**
     * 判断是否可以关闭
     */
    public boolean canClose() {
        return "02".equals(orderStatus) || "03".equals(orderStatus);
    }

    /**
     * 判断预算是否超支
     */
    public boolean isBudgetExceeded() {
        if (budgetAmount == null) {
            return false;
        }
        return actualCost.compareTo(budgetAmount) > 0;
    }
}
