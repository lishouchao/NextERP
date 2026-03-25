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
 * 请假记录 (对标 SAP IT2001 Absence)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hrm_leave")
public class HrmLeave extends TenantAwareEntity {

    /**
     * 请假ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 请假单号
     */
    @Column(name = "leave_no", nullable = false, length = 20)
    private String leaveNo;

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
     * 假期类型 (01-年假 02-事假 03-病假 04-婚假 05-产假 06-陪产假 07-丧假 08-调休)
     */
    @Column(name = "leave_type", nullable = false, length = 2)
    private String leaveType;

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
     * 开始时间 (上午/下午)
     */
    @Column(name = "start_period", length = 2)
    private String startPeriod;

    /**
     * 结束时间 (上午/下午)
     */
    @Column(name = "end_period", length = 2)
    private String endPeriod;

    /**
     * 请假天数
     */
    @Column(name = "leave_days", precision = 4, scale = 1)
    private BigDecimal leaveDays;

    /**
     * 请假小时数
     */
    @Column(name = "leave_hours", precision = 5, scale = 1)
    private BigDecimal leaveHours;

    /**
     * 请假原因
     */
    @Column(name = "leave_reason", columnDefinition = "TEXT")
    private String leaveReason;

    /**
     * 请假期间联系电话
     */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * 附件 (JSON格式)
     */
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    /**
     * 审批状态 (01-待提交 02-审批中 03-已通过 04-已拒绝 05-已撤回 06-已取消)
     */
    @Column(name = "approval_status", nullable = false, length = 2)
    @Builder.Default
    private String approvalStatus = "01";

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
     * 提交时间
     */
    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    /**
     * 最终审批时间
     */
    @Column(name = "final_approval_time")
    private LocalDateTime finalApprovalTime;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取假期类型名称
     */
    public String getLeaveTypeName() {
        return switch (leaveType) {
            case "01" -> "年假";
            case "02" -> "事假";
            case "03" -> "病假";
            case "04" -> "婚假";
            case "05" -> "产假";
            case "06" -> "陪产假";
            case "07" -> "丧假";
            case "08" -> "调休";
            default -> "未知";
        };
    }

    /**
     * 获取审批状态名称
     */
    public String getApprovalStatusName() {
        return switch (approvalStatus) {
            case "01" -> "待提交";
            case "02" -> "审批中";
            case "03" -> "已通过";
            case "04" -> "已拒绝";
            case "05" -> "已撤回";
            case "06" -> "已取消";
            default -> "未知";
        };
    }

    /**
     * 判断是否可以编辑
     */
    public boolean canEdit() {
        return "01".equals(approvalStatus) || "04".equals(approvalStatus);
    }

    /**
     * 判断是否可以撤回
     */
    public boolean canWithdraw() {
        return "02".equals(approvalStatus);
    }

    /**
     * 计算请假天数
     */
    public void calculateLeaveDays() {
        if (startDate != null && endDate != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            this.leaveDays = BigDecimal.valueOf(days);
        }
    }
}
