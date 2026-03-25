package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录 (对标 SAP IT2002 Attendance)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hrm_attendance")
public class HrmAttendance extends TenantAwareEntity {

    /**
     * 考勤ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
     * 考勤日期
     */
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    /**
     * 班次ID
     */
    @Column(name = "shift_id")
    private Long shiftId;

    /**
     * 班次名称
     */
    @Column(name = "shift_name", length = 50)
    private String shiftName;

    /**
     * 上班打卡时间
     */
    @Column(name = "clock_in_time")
    private LocalDateTime clockInTime;

    /**
     * 下班打卡时间
     */
    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    /**
     * 规定上班时间
     */
    @Column(name = "scheduled_in_time")
    private LocalDateTime scheduledInTime;

    /**
     * 规定下班时间
     */
    @Column(name = "scheduled_out_time")
    private LocalDateTime scheduledOutTime;

    /**
     * 工作时长(分钟)
     */
    @Column(name = "work_minutes")
    private Integer workMinutes;

    /**
     * 加班时长(分钟)
     */
    @Column(name = "overtime_minutes")
    @Builder.Default
    private Integer overtimeMinutes = 0;

    /**
     * 迟到时长(分钟)
     */
    @Column(name = "late_minutes")
    @Builder.Default
    private Integer lateMinutes = 0;

    /**
     * 早退时长(分钟)
     */
    @Column(name = "early_leave_minutes")
    @Builder.Default
    private Integer earlyLeaveMinutes = 0;

    /**
     * 考勤状态 (01-正常 02-迟到 03-早退 04-缺勤 05-请假 06-出差)
     */
    @Column(name = "attendance_status", length = 2)
    private String attendanceStatus;

    /**
     * 异常原因
     */
    @Column(name = "exception_reason", length = 500)
    private String exceptionReason;

    /**
     * 审批状态 (01-待审批 02-已通过 03-已拒绝)
     */
    @Column(name = "approval_status", length = 2)
    private String approvalStatus;

    /**
     * 审批人ID
     */
    @Column(name = "approver_id")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @Column(name = "approver_name", length = 50)
    private String approverName;

    /**
     * 审批时间
     */
    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取考勤状态名称
     */
    public String getAttendanceStatusName() {
        return switch (attendanceStatus) {
            case "01" -> "正常";
            case "02" -> "迟到";
            case "03" -> "早退";
            case "04" -> "缺勤";
            case "05" -> "请假";
            case "06" -> "出差";
            default -> "未知";
        };
    }

    /**
     * 判断是否正常出勤
     */
    public boolean isNormalAttendance() {
        return "01".equals(attendanceStatus);
    }

    /**
     * 计算工作时长
     */
    public void calculateWorkMinutes() {
        if (clockInTime != null && clockOutTime != null) {
            long minutes = java.time.Duration.between(clockInTime, clockOutTime).toMinutes();
            this.workMinutes = (int) minutes;
        }
    }
}
