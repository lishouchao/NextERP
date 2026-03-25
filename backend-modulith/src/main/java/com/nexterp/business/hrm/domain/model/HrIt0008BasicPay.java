package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
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
 * InfoType 0008 - 基本工资 (Basic Pay)
 * 对标 SAP IT0008
 *
 * 存储员工的基本薪资信息
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it0008_basic_pay", indexes = {
    @Index(name = "idx_it0008_employee", columnList = "tenant_id, employee_id, valid_from")
})
public class HrIt0008BasicPay extends TimeValidEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
     * 薪资类型 (1-月薪 2-日薪 3-时薪 4-年薪)
     */
    @Column(name = "pay_type", nullable = false, length = 1)
    @Builder.Default
    private String payType = "1";

    /**
     * 薪资等级
     */
    @Column(name = "pay_grade", length = 10)
    private String payGrade;

    /**
     * 薪资区域
     */
    @Column(name = "pay_area", length = 10)
    private String payArea;

    /**
     * 工资组
     */
    @Column(name = "pay_group", length = 10)
    private String payGroup;

    /**
     * 工资范围
     */
    @Column(name = "pay_scale", length = 10)
    private String payScale;

    /**
     * 货币类型
     */
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "CNY";

    /**
     * 基本工资金额
     */
    @Column(name = "basic_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal basicSalary;

    /**
     * 岗位工资
     */
    @Column(name = "position_salary", precision = 19, scale = 2)
    private BigDecimal positionSalary;

    /**
     * 绩效工资基数
     */
    @Column(name = "performance_base", precision = 19, scale = 2)
    private BigDecimal performanceBase;

    /**
     * 绩效系数
     */
    @Column(name = "performance_ratio", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal performanceRatio = BigDecimal.ONE;

    /**
     * 工龄工资
     */
    @Column(name = "seniority_pay", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal seniorityPay = BigDecimal.ZERO;

    /**
     * 职务津贴
     */
    @Column(name = "job_allowance", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal jobAllowance = BigDecimal.ZERO;

    /**
     * 交通补贴
     */
    @Column(name = "transport_allowance", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal transportAllowance = BigDecimal.ZERO;

    /**
     * 餐饮补贴
     */
    @Column(name = "meal_allowance", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal mealAllowance = BigDecimal.ZERO;

    /**
     * 通讯补贴
     */
    @Column(name = "communication_allowance", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal communicationAllowance = BigDecimal.ZERO;

    /**
     * 住房补贴
     */
    @Column(name = "housing_allowance", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal housingAllowance = BigDecimal.ZERO;

    /**
     * 其他补贴
     */
    @Column(name = "other_allowance", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal otherAllowance = BigDecimal.ZERO;

    /**
     * 月薪合计 (基本工资 + 岗位工资 + 各项补贴)
     */
    @Column(name = "monthly_total", precision = 19, scale = 2)
    private BigDecimal monthlyTotal;

    /**
     * 年薪合计
     */
    @Column(name = "annual_total", precision = 19, scale = 2)
    private BigDecimal annualTotal;

    /**
     * 生效原因 (1-新入职 2-调薪 3-晋升 4-调岗 5-转正 6-其他)
     */
    @Column(name = "reason", length = 1)
    private String reason;

    /**
     * 审批状态 (0-待审批 1-已审批 2-已拒绝)
     */
    @Column(name = "approval_status", length = 1)
    @Builder.Default
    private String approvalStatus = "1";

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
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 计算月薪合计
     */
    public BigDecimal calculateMonthlyTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (basicSalary != null) total = total.add(basicSalary);
        if (positionSalary != null) total = total.add(positionSalary);
        if (performanceBase != null && performanceRatio != null) {
            total = total.add(performanceBase.multiply(performanceRatio));
        }
        if (seniorityPay != null) total = total.add(seniorityPay);
        if (jobAllowance != null) total = total.add(jobAllowance);
        if (transportAllowance != null) total = total.add(transportAllowance);
        if (mealAllowance != null) total = total.add(mealAllowance);
        if (communicationAllowance != null) total = total.add(communicationAllowance);
        if (housingAllowance != null) total = total.add(housingAllowance);
        if (otherAllowance != null) total = total.add(otherAllowance);
        return total;
    }
}
