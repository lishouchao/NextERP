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
 * 薪酬项目明细 (Payroll Item)
 *
 * 存储每次薪酬计算的明细项
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_payroll_item", indexes = {
    @Index(name = "idx_payroll_item_result", columnList = "tenant_id, payroll_result_id"),
    @Index(name = "idx_payroll_item_type", columnList = "tenant_id, wage_type_id")
})
public class HrPayrollItem extends TenantAwareEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 薪酬结果ID
     */
    @Column(name = "payroll_result_id", nullable = false)
    private Long payrollResultId;

    /**
     * 员工内码
     */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /**
     * 员工编号
     */
    @Column(name = "employee_no", nullable = false, length = 8)
    private String employeeNo;

    /**
     * 薪酬期间 (YYYYMM)
     */
    @Column(name = "payroll_period", nullable = false, length = 6)
    private String payrollPeriod;

    /**
     * 工资类型ID
     */
    @Column(name = "wage_type_id", nullable = false)
    private Long wageTypeId;

    /**
     * 工资类型代码
     */
    @Column(name = "wage_type_code", length = 4)
    private String wageTypeCode;

    /**
     * 工资类型名称 (冗余)
     */
    @Column(name = "wage_type_name", length = 50)
    private String wageTypeName;

    /**
     * 工资分类
     */
    @Column(name = "wage_category", length = 1)
    private String wageCategory;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 借贷方向 (D-借方 C-贷方)
     */
    @Column(name = "dc_indicator", length = 1)
    private String dcIndicator;

    /**
     * 计算基数
     */
    @Column(name = "calc_base", precision = 19, scale = 2)
    private BigDecimal calcBase;

    /**
     * 计算比例
     */
    @Column(name = "calc_ratio", precision = 5, scale = 2)
    private BigDecimal calcRatio;

    /**
     * 金额
     */
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * 计算公式
     */
    @Column(name = "calc_formula", length = 500)
    private String calcFormula;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

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
