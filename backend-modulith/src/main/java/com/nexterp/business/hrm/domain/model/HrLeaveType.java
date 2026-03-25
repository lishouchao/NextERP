package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * 假期类型 (Leave Type)
 *
 * 定义企业可用的假期类型及其规则
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_leave_type", uniqueConstraints = {
    @UniqueConstraint(name = "uk_leave_type_code", columnNames = {"tenant_id", "leave_type_code"})
})
public class HrLeaveType extends TenantAwareEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 假期类型代码
     */
    @Column(name = "leave_type_code", nullable = false, length = 4)
    private String leaveTypeCode;

    /**
     * 假期类型名称
     */
    @Column(name = "leave_type_name", nullable = false, length = 50)
    private String leaveTypeName;

    /**
     * 假期分类 (1-年假 2-病假 3-事假 4-婚假 5-产假 6-陪产假 7-丧假 8-调休 9-其他)
     */
    @Column(name = "leave_category", nullable = false, length = 1)
    private String leaveCategory;

    /**
     * 是否带薪
     */
    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private Boolean isPaid = true;

    /**
     * 薪资支付比例 (0-1)
     */
    @Column(name = "pay_ratio")
    @Builder.Default
    private java.math.BigDecimal payRatio = java.math.BigDecimal.ONE;

    /**
     * 是否需要审批
     */
    @Column(name = "require_approval", nullable = false)
    @Builder.Default
    private Boolean requireApproval = true;

    /**
     * 审批级别 (1-一级 2-二级 3-三级)
     */
    @Column(name = "approval_level")
    @Builder.Default
    private Integer approvalLevel = 1;

    /**
     * 是否需要附件
     */
    @Column(name = "require_attachment")
    @Builder.Default
    private Boolean requireAttachment = false;

    /**
     * 最小请假单位 (1-半天 2-按天 3-按小时)
     */
    @Column(name = "min_unit", length = 1)
    @Builder.Default
    private String minUnit = "2";

    /**
     * 每年限额 (天数, null表示不限制)
     */
    @Column(name = "annual_limit")
    private Integer annualLimit;

    /**
     * 是否结转
     */
    @Column(name = "can_carry_over")
    @Builder.Default
    private Boolean canCarryOver = false;

    /**
     * 结转最大天数
     */
    @Column(name = "max_carry_over_days")
    private Integer maxCarryOverDays;

    /**
     * 结转有效期 (月)
     */
    @Column(name = "carry_over_expire_months")
    private Integer carryOverExpireMonths;

    /**
     * 适用性别 (A-全部 M-男 F-女)
     */
    @Column(name = "apply_gender", length = 1)
    @Builder.Default
    private String applyGender = "A";

    /**
     * 适用员工组 (JSON数组)
     */
    @Column(name = "apply_employee_groups", length = 100)
    private String applyEmployeeGroups;

    /**
     * 工龄要求 (年)
     */
    @Column(name = "min_tenure_years")
    private Integer minTenureYears;

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
     * 获取假期分类名称
     */
    public String getLeaveCategoryName() {
        return switch (leaveCategory) {
            case "1" -> "年假";
            case "2" -> "病假";
            case "3" -> "事假";
            case "4" -> "婚假";
            case "5" -> "产假";
            case "6" -> "陪产假";
            case "7" -> "丧假";
            case "8" -> "调休";
            case "9" -> "其他";
            default -> "未知";
        };
    }
}
