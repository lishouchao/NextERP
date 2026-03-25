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
import java.time.LocalDateTime;

/**
 * InfoType 2001 - 请假/缺勤 (Absence)
 * 对标 SAP IT2001
 *
 * 记录员工的请假申请和缺勤情况
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it2001_absence", indexes = {
    @Index(name = "idx_it2001_employee", columnList = "tenant_id, employee_id, start_date"),
    @Index(name = "idx_it2001_type", columnList = "tenant_id, leave_type_id"),
    @Index(name = "idx_it2001_status", columnList = "tenant_id, approval_status")
})
public class HrIt2001Absence extends TimeValidEntity {

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
     * 请假单号
     */
    @Column(name = "request_no", length = 20)
    private String requestNo;

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
     * 开始日期
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * 开始时间 (半天请假时使用)
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 请假天数
     */
    @Column(name = "absence_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal absenceDays;

    /**
     * 请假小时数
     */
    @Column(name = "absence_hours", precision = 5, scale = 1)
    private BigDecimal absenceHours;

    /**
     * 请假原因
     */
    @Column(name = "reason", length = 500)
    private String reason;

    /**
     * 请假地址/联系方式
     */
    @Column(name = "contact_info", length = 100)
    private String contactInfo;

    /**
     * 附件 (JSON数组)
     */
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    /**
     * 审批状态 (0-待提交 1-待审批 2-已通过 3-已拒绝 4-已撤销)
     */
    @Column(name = "approval_status", nullable = false, length = 1)
    @Builder.Default
    private String approvalStatus = "0";

    /**
     * 当前审批节点
     */
    @Column(name = "current_approver_id")
    private Long currentApproverId;

    /**
     * 当前审批人姓名
     */
    @Column(name = "current_approver_name", length = 50)
    private String currentApproverName;

    /**
     * 审批时间
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 审批意见
     */
    @Column(name = "approval_comment", length = 500)
    private String approvalComment;

    /**
     * 销假状态 (0-未销假 1-已销假)
     */
    @Column(name = "cancel_status", length = 1)
    @Builder.Default
    private String cancelStatus = "0";

    /**
     * 销假时间
     */
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    /**
     * 是否带薪
     */
    @Column(name = "is_paid")
    @Builder.Default
    private Boolean isPaid = true;

    /**
     * 薪资扣除比例
     */
    @Column(name = "deduction_ratio", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal deductionRatio = BigDecimal.ZERO;

    /**
     * 实际扣款金额
     */
    @Column(name = "deduction_amount", precision = 19, scale = 2)
    private BigDecimal deductionAmount;

    /**
     * 关联额度ID
     */
    @Column(name = "quota_id")
    private Long quotaId;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取审批状态名称
     */
    public String getApprovalStatusName() {
        return switch (approvalStatus) {
            case "0" -> "待提交";
            case "1" -> "待审批";
            case "2" -> "已通过";
            case "3" -> "已拒绝";
            case "4" -> "已撤销";
            default -> "未知";
        };
    }

    /**
     * 是否可编辑
     */
    public boolean isEditable() {
        return "0".equals(approvalStatus) || "3".equals(approvalStatus);
    }

    /**
     * 是否可撤销
     */
    public boolean isCancelable() {
        return "1".equals(approvalStatus) || "2".equals(approvalStatus);
    }

    /**
     * 获取请假时长 (天)
     */
    public long getDurationDays() {
        if (startDate == null || endDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
