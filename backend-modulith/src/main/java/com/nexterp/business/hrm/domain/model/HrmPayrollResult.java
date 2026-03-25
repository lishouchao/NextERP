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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 薪酬结果 (对标 SAP HRPY_RT)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hrm_payroll_result")
public class HrmPayrollResult extends TenantAwareEntity {

    /**
     * 薪酬结果ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 薪酬期间 (格式: YYYY-MM)
     */
    @Column(name = "payroll_period", nullable = false, length = 7)
    private String payrollPeriod;

    /**
     * 员工ID
     */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /**
     * 员工编号
     */
    @Column(name = "employee_no", length = 50)
    private String employeeNo;

    /**
     * 员工姓名
     */
    @Column(name = "employee_name", length = 50)
    private String employeeName;

    /**
     * 部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Column(name = "department_name", length = 100)
    private String departmentName;

    /**
     * 基本工资
     */
    @Column(name = "base_salary", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal baseSalary = BigDecimal.ZERO;

    /**
     * 岗位工资
     */
    @Column(name = "position_salary", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal positionSalary = BigDecimal.ZERO;

    /**
     * 绩效工资
     */
    @Column(name = "performance_salary", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal performanceSalary = BigDecimal.ZERO;

    /**
     * 加班工资
     */
    @Column(name = "overtime_pay", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal overtimePay = BigDecimal.ZERO;

    /**
     * 津贴合计
     */
    @Column(name = "allowance_total", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal allowanceTotal = BigDecimal.ZERO;

    /**
     * 奖金
     */
    @Column(name = "bonus", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal bonus = BigDecimal.ZERO;

    /**
     * 应发合计
     */
    @Column(name = "gross_pay", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal grossPay = BigDecimal.ZERO;

    /**
     * 养老保险(个人)
     */
    @Column(name = "pension_insurance", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal pensionInsurance = BigDecimal.ZERO;

    /**
     * 医疗保险(个人)
     */
    @Column(name = "medical_insurance", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal medicalInsurance = BigDecimal.ZERO;

    /**
     * 失业保险(个人)
     */
    @Column(name = "unemployment_insurance", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal unemploymentInsurance = BigDecimal.ZERO;

    /**
     * 住房公积金(个人)
     */
    @Column(name = "housing_fund", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal housingFund = BigDecimal.ZERO;

    /**
     * 个人所得税
     */
    @Column(name = "income_tax", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal incomeTax = BigDecimal.ZERO;

    /**
     * 其他扣款
     */
    @Column(name = "other_deduction", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal otherDeduction = BigDecimal.ZERO;

    /**
     * 扣款合计
     */
    @Column(name = "deduction_total", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal deductionTotal = BigDecimal.ZERO;

    /**
     * 实发合计
     */
    @Column(name = "net_pay", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal netPay = BigDecimal.ZERO;

    /**
     * 工作天数
     */
    @Column(name = "work_days", precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal workDays = BigDecimal.ZERO;

    /**
     * 货币代码
     */
    @Column(name = "currency_code", length = 3)
    @Builder.Default
    private String currencyCode = "CNY";

    /**
     * 状态 (01-待确认 02-已确认 03-已发放 04-已撤销)
     */
    @Column(name = "pay_status", nullable = false, length = 2)
    @Builder.Default
    private String payStatus = "01";

    /**
     * 计算时间
     */
    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    /**
     * 确认时间
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * 发放时间
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 计算应发合计
     */
    public void calculateGrossPay() {
        this.grossPay = baseSalary
                .add(positionSalary)
                .add(performanceSalary)
                .add(overtimePay)
                .add(allowanceTotal)
                .add(bonus);
    }

    /**
     * 计算扣款合计
     */
    public void calculateDeductionTotal() {
        this.deductionTotal = pensionInsurance
                .add(medicalInsurance)
                .add(unemploymentInsurance)
                .add(housingFund)
                .add(incomeTax)
                .add(otherDeduction);
    }

    /**
     * 计算实发合计
     */
    public void calculateNetPay() {
        calculateGrossPay();
        calculateDeductionTotal();
        this.netPay = grossPay.subtract(deductionTotal);
    }

    /**
     * 获取状态名称
     */
    public String getPayStatusName() {
        return switch (payStatus) {
            case "01" -> "待确认";
            case "02" -> "已确认";
            case "03" -> "已发放";
            case "04" -> "已撤销";
            default -> "未知";
        };
    }
}
