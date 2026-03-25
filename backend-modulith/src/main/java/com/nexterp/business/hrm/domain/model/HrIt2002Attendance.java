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
 * InfoType 2002 - 考勤/出勤 (Attendance)
 * 对标 SAP IT2002
 *
 * 记录员工的出勤打卡数据
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it2002_attendance", indexes = {
    @Index(name = "idx_it2002_employee_date", columnList = "tenant_id, employee_id, attendance_date"),
    @Index(name = "idx_it2002_date", columnList = "tenant_id, attendance_date")
})
public class HrIt2002Attendance extends TimeValidEntity {

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
     * 计划上班时间
     */
    @Column(name = "scheduled_clock_in")
    private LocalDateTime scheduledClockIn;

    /**
     * 计划下班时间
     */
    @Column(name = "scheduled_clock_out")
    private LocalDateTime scheduledClockOut;

    /**
     * 实际上班打卡时间
     */
    @Column(name = "actual_clock_in")
    private LocalDateTime actualClockIn;

    /**
     * 实际下班打卡时间
     */
    @Column(name = "actual_clock_out")
    private LocalDateTime actualClockOut;

    /**
     * 上班打卡方式 (1-指纹 2-人脸 3-刷卡 4-手机定位 5-手动补签)
     */
    @Column(name = "clock_in_method", length = 1)
    private String clockInMethod;

    /**
     * 下班打卡方式
     */
    @Column(name = "clock_out_method", length = 1)
    private String clockOutMethod;

    /**
     * 上班打卡地点
     */
    @Column(name = "clock_in_location", length = 200)
    private String clockInLocation;

    /**
     * 下班打卡地点
     */
    @Column(name = "clock_out_location", length = 200)
    private String clockOutLocation;

    /**
     * 上班打卡IP
     */
    @Column(name = "clock_in_ip", length = 50)
    private String clockInIp;

    /**
     * 下班打卡IP
     */
    @Column(name = "clock_out_ip", length = 50)
    private String clockOutIp;

    /**
     * 考勤状态 (1-正常 2-迟到 3-早退 4-旷工 5-请假 6-出差 7-外勤 8-休息)
     */
    @Column(name = "attendance_status", nullable = false, length = 1)
    @Builder.Default
    private String attendanceStatus = "1";

    /**
     * 迟到分钟数
     */
    @Column(name = "late_minutes")
    @Builder.Default
    private Integer lateMinutes = 0;

    /**
     * 早退分钟数
     */
    @Column(name = "early_leave_minutes")
    @Builder.Default
    private Integer earlyLeaveMinutes = 0;

    /**
     * 加班时长 (小时)
     */
    @Column(name = "overtime_hours", precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    /**
     * 加班类型 (1-工作日加班 2-周末加班 3-节假日加班)
     */
    @Column(name = "overtime_type", length = 1)
    private String overtimeType;

    /**
     * 工作时长 (小时)
     */
    @Column(name = "work_hours", precision = 5, scale = 1)
    private BigDecimal workHours;

    /**
     * 关联请假ID
     */
    @Column(name = "absence_id")
    private Long absenceId;

    /**
     * 异常说明
     */
    @Column(name = "exception_note", length = 500)
    private String exceptionNote;

    /**
     * 补签原因
     */
    @Column(name = "manual_reason", length = 500)
    private String manualReason;

    /**
     * 补签审批人
     */
    @Column(name = "manual_approved_by", length = 50)
    private String manualApprovedBy;

    /**
     * 补签审批时间
     */
    @Column(name = "manual_approved_at")
    private LocalDateTime manualApprovedAt;

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
            case "1" -> "正常";
            case "2" -> "迟到";
            case "3" -> "早退";
            case "4" -> "旷工";
            case "5" -> "请假";
            case "6" -> "出差";
            case "7" -> "外勤";
            case "8" -> "休息";
            default -> "未知";
        };
    }

    /**
     * 是否异常考勤
     */
    public boolean isAbnormal() {
        return "2".equals(attendanceStatus) || "3".equals(attendanceStatus) || "4".equals(attendanceStatus);
    }

    /**
     * 是否已打卡
     */
    public boolean hasClockIn() {
        return actualClockIn != null;
    }

    /**
     * 是否已签退
     */
    public boolean hasClockOut() {
        return actualClockOut != null;
    }

    /**
     * 计算工作时长 (小时)
     */
    public BigDecimal calculateWorkHours() {
        if (actualClockIn == null || actualClockOut == null) {
            return BigDecimal.ZERO;
        }
        long minutes = java.time.Duration.between(actualClockIn, actualClockOut).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 1, java.math.RoundingMode.HALF_UP);
    }
}
