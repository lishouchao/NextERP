package com.nexterp.business.hrm.domain.model;

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
 * 工资类型 (Wage Type)
 *
 * 定义薪酬计算的各类工资项目
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_wage_type", uniqueConstraints = {
    @UniqueConstraint(name = "uk_wage_type_code", columnNames = {"tenant_id", "wage_type_code"})
})
public class HrWageType extends TenantAwareEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 工资类型代码
     */
    @Column(name = "wage_type_code", nullable = false, length = 4)
    private String wageTypeCode;

    /**
     * 工资类型名称
     */
    @Column(name = "wage_type_name", nullable = false, length = 50)
    private String wageTypeName;

    /**
     * 工资类型分类 (1-基本工资 2-津贴 3-奖金 4-扣款 5-社保 6-公积金 7-个税 8-其他)
     */
    @Column(name = "wage_category", nullable = false, length = 1)
    private String wageCategory;

    /**
     * 计算类型 (1-固定金额 2-按比例 3-公式计算 4-累进税率)
     */
    @Column(name = "calc_type", nullable = false, length = 1)
    private String calcType;

    /**
     * 固定金额
     */
    @Column(name = "fixed_amount", precision = 19, scale = 2)
    private BigDecimal fixedAmount;

    /**
     * 比例 (用于按比例计算)
     */
    @Column(name = "ratio", precision = 5, scale = 2)
    private BigDecimal ratio;

    /**
     * 计算公式
     */
    @Column(name = "calc_formula", length = 500)
    private String calcFormula;

    /**
     * 基数上限
     */
    @Column(name = "base_max", precision = 19, scale = 2)
    private BigDecimal baseMax;

    /**
     * 基数下限
     */
    @Column(name = "base_min", precision = 19, scale = 2)
    private BigDecimal baseMin;

    /**
     * 是否应税
     */
    @Column(name = "is_taxable", nullable = false)
    @Builder.Default
    private Boolean isTaxable = true;

    /**
     * 是否计入社保基数
     */
    @Column(name = "is_social_base", nullable = false)
    @Builder.Default
    private Boolean isSocialBase = true;

    /**
     * 是否计入公积金基数
     */
    @Column(name = "is_fund_base", nullable = false)
    @Builder.Default
    private Boolean isFundBase = true;

    /**
     * 借贷方向 (D-借方/加项 C-贷方/减项)
     */
    @Column(name = "dc_indicator", nullable = false, length = 1)
    @Builder.Default
    private String dcIndicator = "D";

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 状态 (0-禁用 1-启用)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取工资分类名称
     */
    public String getWageCategoryName() {
        return switch (wageCategory) {
            case "1" -> "基本工资";
            case "2" -> "津贴";
            case "3" -> "奖金";
            case "4" -> "扣款";
            case "5" -> "社保";
            case "6" -> "公积金";
            case "7" -> "个税";
            case "8" -> "其他";
            default -> "未知";
        };
    }

    /**
     * 是否加项
     */
    public boolean isAddition() {
        return "D".equals(dcIndicator);
    }

    /**
     * 是否减项
     */
    public boolean isDeduction() {
        return "C".equals(dcIndicator);
    }
}
