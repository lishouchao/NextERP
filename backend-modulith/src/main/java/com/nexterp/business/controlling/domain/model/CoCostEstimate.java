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
 * 成本估算
 * 对标: SAP KEKO
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_cost_estimate")
public class CoCostEstimate extends TenantAwareEntity {

    /**
     * 成本估算ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 成本估算号
     */
    @Column(name = "estimate_number", nullable = false, length = 12)
    private String estimateNumber;

    /**
     * 物料ID
     */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /**
     * 物料代码
     */
    @Column(name = "material_code", length = 18)
    private String materialCode;

    /**
     * 物料名称
     */
    @Column(name = "material_name", length = 100)
    private String materialName;

    /**
     * 工厂代码
     */
    @Column(name = "plant_code", length = 4)
    private String plantCode;

    /**
     * 估算类型 (01-标准成本估算 02-库存评估 03-修改估算)
     */
    @Column(name = "estimate_type", nullable = false, length = 2)
    private String estimateType;

    /**
     * 估算版本
     */
    @Column(name = "estimate_version", length = 3)
    private String estimateVersion;

    /**
     * 成本核算表
     */
    @Column(name = "costing_sheet", length = 6)
    private String costingSheet;

    /**
     * 成本控制范围
     */
    @Column(name = "controlling_area", length = 4)
    private String controllingArea;

    /**
     * 有效起始日期
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    /**
     * 有效结束日期
     */
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    /**
     * 估算日期
     */
    @Column(name = "estimate_date")
    private LocalDate estimateDate;

    /**
     * 数量
     */
    @Column(name = "quantity", precision = 13, scale = 3)
    private BigDecimal quantity;

    /**
     * 单位
     */
    @Column(name = "unit", length = 3)
    private String unit;

    /**
     * 物料成本
     */
    @Column(name = "material_cost", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal materialCost = BigDecimal.ZERO;

    /**
     * 人工成本
     */
    @Column(name = "labor_cost", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal laborCost = BigDecimal.ZERO;

    /**
     * 机器成本
     */
    @Column(name = "machine_cost", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal machineCost = BigDecimal.ZERO;

    /**
     * 制造费用
     */
    @Column(name = "overhead_cost", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal overheadCost = BigDecimal.ZERO;

    /**
     * 外协加工费
     */
    @Column(name = "subcontracting_cost", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal subcontractingCost = BigDecimal.ZERO;

    /**
     * 总成本
     */
    @Column(name = "total_cost", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    /**
     * 单位成本
     */
    @Column(name = "unit_cost", precision = 19, scale = 4)
    private BigDecimal unitCost;

    /**
     * 货币代码
     */
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    /**
     * 估算状态 (01-草稿 02-已下达 03-已标记 04-已发布)
     */
    @Column(name = "estimate_status", nullable = false, length = 2)
    @Builder.Default
    private String estimateStatus = "01";

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 发布时间
     */
    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    /**
     * 成本构成明细
     */
    @OneToMany(mappedBy = "costEstimate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoCostComponent> costComponents = new ArrayList<>();

    /**
     * 获取估算类型名称
     */
    public String getEstimateTypeName() {
        return switch (estimateType) {
            case "01" -> "标准成本估算";
            case "02" -> "库存评估";
            case "03" -> "修改估算";
            default -> "未知";
        };
    }

    /**
     * 获取估算状态名称
     */
    public String getEstimateStatusName() {
        return switch (estimateStatus) {
            case "01" -> "草稿";
            case "02" -> "已下达";
            case "03" -> "已标记";
            case "04" -> "已发布";
            default -> "未知";
        };
    }

    /**
     * 计算总成本
     */
    public void calculateTotalCost() {
        this.totalCost = (materialCost != null ? materialCost : BigDecimal.ZERO)
                .add(laborCost != null ? laborCost : BigDecimal.ZERO)
                .add(machineCost != null ? machineCost : BigDecimal.ZERO)
                .add(overheadCost != null ? overheadCost : BigDecimal.ZERO)
                .add(subcontractingCost != null ? subcontractingCost : BigDecimal.ZERO);

        if (quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0) {
            this.unitCost = totalCost.divide(quantity, 4, BigDecimal.ROUND_HALF_UP);
        }
    }
}
