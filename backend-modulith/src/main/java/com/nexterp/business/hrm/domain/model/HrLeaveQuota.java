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
 * 假期额度 (Leave Quota)
 *
 * 员工的各类假期额度管理
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_leave_quota", indexes = {
    @Index(name = "idx_leave_quota_employee", columnList = "tenant_id, employee_id, quota_year"),
    @Index(name = "idx_leave_quota_type", columnList = "tenant_id, leave_type_id, quota_year")
})
public class HrLeaveQuota extends TenantAwareEntity {

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
     * 假期类型ID
     */
    @Column(name = "leave_type_id", nullable = false)
    private Long leaveTypeId;

    /**
     * 假期类型代码
     */
    @Column(name = "leave_type_code", length = 4)
    private String leaveTypeCode;

    /**
     * 假期类型名称 (冗余)
     */
    @Column(name = "leave_type_name", length = 50)
    private String leaveTypeName;

    /**
     * 额度年度
     */
    @Column(name = "quota_year", nullable = false)
    private Integer quotaYear;

    /**
     * 年初额度 (天)
     */
    @Column(name = "entitled_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal entitledDays;

    /**
     * 结转额度 (天)
     */
    @Column(name = "carried_over_days", precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal carriedOverDays = BigDecimal.ZERO;

    /**
     * 调整额度 (天)
     */
    @Column(name = "adjusted_days", precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal adjustedDays = BigDecimal.ZERO;

    /**
     * 已使用额度 (天)
     */
    @Column(name = "used_days", precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal usedDays = BigDecimal.ZERO;

    /**
     * 待审批额度 (天)
     */
    @Column(name = "pending_days", precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal pendingDays = BigDecimal.ZERO;

    /**
     * 总额度 (= 年初 + 结转 + 调整)
     */
    @Column(name = "total_days", precision = 5, scale = 1)
    private BigDecimal totalDays;

    /**
     * 剩余额度 (= 总额度 - 已使用 - 待审批)
     */
    @Column(name = "remaining_days", precision = 5, scale = 1)
    private BigDecimal remainingDays;

    /**
     * 结转过期日期
     */
    @Column(name = "carry_over_expire_date")
    private LocalDate carryOverExpireDate;

    /**
     * 最后计算时间
     */
    @Column(name = "last_calculated_at")
    private java.time.LocalDateTime lastCalculatedAt;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 计算总额度
     */
    public BigDecimal calculateTotalDays() {
        BigDecimal total = entitledDays != null ? entitledDays : BigDecimal.ZERO;
        if (carriedOverDays != null) total = total.add(carriedOverDays);
        if (adjustedDays != null) total = total.add(adjustedDays);
        return total;
    }

    /**
     * 计算剩余额度
     */
    public BigDecimal calculateRemainingDays() {
        BigDecimal total = calculateTotalDays();
        if (usedDays != null) total = total.subtract(usedDays);
        if (pendingDays != null) total = total.subtract(pendingDays);
        return total.max(BigDecimal.ZERO);
    }

    /**
     * 刷新计算
     */
    public void refresh() {
        this.totalDays = calculateTotalDays();
        this.remainingDays = calculateRemainingDays();
        this.lastCalculatedAt = java.time.LocalDateTime.now();
    }

    /**
     * 是否有足够额度
     */
    public boolean hasEnoughQuota(BigDecimal days) {
        return remainingDays != null && remainingDays.compareTo(days) >= 0;
    }
}
