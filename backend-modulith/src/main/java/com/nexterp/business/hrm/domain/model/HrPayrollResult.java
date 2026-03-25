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

/**
 * 薪酬结果 (Payroll Result)
 *
 * 存储每次薪酬计算的结果汇总
 * 建议按 payroll_year + payroll_month 分区
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_payroll_result", indexes = {
    @Index(name = "idx_payroll_employee", columnList = "tenant_id, employee_id, payroll_year, payroll_month"),
    @Index(name = "idx_payroll_period", columnList = "tenant_id, payroll_year, payroll_month"),
    @Index(name = "idx_payroll_status", columnList = "tenant_id, payroll_status")
})
public class HrPayrollResult extends TenantAwareEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 薪酬计算批次号
     */
    @Column(name = "batch_no", length = 20)
    private String batchNo;

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
     * 员工姓名 (冗余)
     */
    @Column(name = "employee_name", length = 50)
    private String employeeName;

    /**
     * 薪酬年度
     */
    @Column(name = "payroll_year", nullable = false)
    private Integer payrollYear;

    /**
     * 薪酬月份
     */
    @Column(name = "payroll_month", nullable = false)
    private Integer payrollMonth;

    /**
     * 薪酬期间 (YYYYMM)
     */
    @Column(name = "payroll_period", nullable = false, length = 6)
    private String payrollPeriod;

    /**
     * 公司代码
     */
    @Column(name = "company_code", length = 4)
    private String companyCode;

    /**
     * 组织单元内码
     */
    @Column(name = "org_unit_id")
    private Long orgUnitId;

    /**
     * 组织单元名称 (冗余)
     */
    @Column(name = "org_unit_name", length = 100)
    private String orgUnitName;

    /**
     * 应发合计
     */
    @Column(name = "gross_pay", nullable = false, precision = 19, scale = 2)
    private BigDecimal grossPay;

    /**
     * 扣款合计
     */
    @Column(name = "total_deduction", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalDeduction = BigDecimal.ZERO;

    /**
     * 社保个人合计
     */
    @Column(name = "social_personal", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal socialPersonal = BigDecimal.ZERO;

    /**
     * 公积金个人合计
     */
    @Column(name = "fund_personal", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal fundPersonal = BigDecimal.ZERO;

    /**
     * 个税
     */
    @Column(name = "income_tax", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal incomeTax = BigDecimal.ZERO;

    /**
     * 实发合计
     */
    @Column(name = "net_pay", nullable = false, precision = 19, scale = 2)
    private BigDecimal netPay;

    /**
     * 薪酬状态 (1-已计算 2-已审批 3-已发放 4-已撤销)
     */
    @Column(name = "payroll_status", nullable = false, length = 1)
    @Builder.Default
    private String payrollStatus = "1";

    /**
     * 审批人
     */
    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    /**
     * 审批时间
     */
    @Column(name = "approved_at")
    private LocalDate approvedAt;

    /**
     * 发放时间
     */
    @Column(name = "paid_at")
    private LocalDate paidAt;

    /**
     * 发放方式 (1-银行转账 2-现金 3-支票)
     */
    @Column(name = "payment_method", length = 1)
    @Builder.Default
    private String paymentMethod = "1";

    /**
     * 银行账号
     */
    @Column(name = "bank_account", length = 30)
    private String bankAccount;

    /**
     * 计算时间
     */
    @Column(name = "calculated_at")
    private java.time.LocalDateTime calculatedAt;

    /**
     * 计算人
     */
    @Column(name = "calculated_by", length = 50)
    private String calculatedBy;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取薪酬状态名称
     */
    public String getPayrollStatusName() {
        return switch (payrollStatus) {
            case "1" -> "已计算";
            case "2" -> "已审批";
            case "3" -> "已发放";
            case "4" -> "已撤销";
            default -> "未知";
        };
    }

    /**
     * 是否可修改
     */
    public boolean isModifiable() {
        return "1".equals(payrollStatus);
    }

    /**
     * 计算实发 (= 应发 - 扣款)
     */
    public BigDecimal calculateNetPay() {
        BigDecimal net = grossPay != null ? grossPay : BigDecimal.ZERO;
        if (totalDeduction != null) net = net.subtract(totalDeduction);
        return net.max(BigDecimal.ZERO);
    }
}
