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

/**
 * 成本构成
 * 对标: SAP KEPH
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_cost_component")
public class CoCostComponent extends TenantAwareEntity {

    /**
     * 成本构成ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 成本估算ID
     */
    @Column(name = "cost_estimate_id", nullable = false)
    private Long costEstimateId;

    /**
     * 成本估算 (关联)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_estimate_id", insertable = false, updatable = false)
    private CoCostEstimate costEstimate;

    /**
     * 行号
     */
    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    /**
     * 成本构成视图 (01-成本构成 02-原始成本构成)
     */
    @Column(name = "cost_component_view", length = 2)
    private String costComponentView;

    /**
     * 成本要素ID
     */
    @Column(name = "cost_element_id")
    private Long costElementId;

    /**
     * 成本要素代码
     */
    @Column(name = "cost_element_code", length = 10)
    private String costElementCode;

    /**
     * 成本要素名称
     */
    @Column(name = "cost_element_name", length = 100)
    private String costElementName;

    /**
     * 成本类别 (01-直接材料 02-直接人工 03-制造费用 04-外协加工 05-其他)
     */
    @Column(name = "cost_category", length = 2)
    private String costCategory;

    /**
     * 作业类型ID
     */
    @Column(name = "activity_type_id")
    private Long activityTypeId;

    /**
     * 作业类型代码
     */
    @Column(name = "activity_type_code", length = 6)
    private String activityTypeCode;

    /**
     * 成本中心ID
     */
    @Column(name = "cost_center_id")
    private Long costCenterId;

    /**
     * 成本中心代码
     */
    @Column(name = "cost_center_code", length = 10)
    private String costCenterCode;

    /**
     * 固定成本
     */
    @Column(name = "fixed_cost", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal fixedCost = BigDecimal.ZERO;

    /**
     * 变动成本
     */
    @Column(name = "variable_cost", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal variableCost = BigDecimal.ZERO;

    /**
     * 总成本
     */
    @Column(name = "total_cost", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    /**
     * 获取成本类别名称
     */
    public String getCostCategoryName() {
        return switch (costCategory) {
            case "01" -> "直接材料";
            case "02" -> "直接人工";
            case "03" -> "制造费用";
            case "04" -> "外协加工";
            case "05" -> "其他";
            default -> "未知";
        };
    }

    /**
     * 计算总成本
     */
    public void calculateTotalCost() {
        this.totalCost = (fixedCost != null ? fixedCost : BigDecimal.ZERO)
                .add(variableCost != null ? variableCost : BigDecimal.ZERO);
    }
}
