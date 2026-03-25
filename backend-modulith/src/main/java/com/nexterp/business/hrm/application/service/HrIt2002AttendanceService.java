package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt2002Attendance;
import com.nexterp.business.hrm.domain.repository.HrIt2002AttendanceRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 2002 - 考勤/出勤 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt2002AttendanceService {

    private final HrIt2002AttendanceRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrIt2002Attendance entity) {
        // 检查是否已存在当日考勤
        if (repository.findByEmployeeIdAndAttendanceDateAndIsDeletedFalse(
                entity.getEmployeeId(), entity.getAttendanceDate()).isPresent()) {
            throw new BusinessException("当日考勤记录已存在");
        }
        // 计算工作时长
        if (entity.getActualClockIn() != null && entity.getActualClockOut() != null) {
            entity.setWorkHours(entity.calculateWorkHours());
        }
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(entity.getAttendanceDate());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(entity.getAttendanceDate());
        }
        HrIt2002Attendance saved = repository.save(entity);
        log.info("创建考勤记录: employeeId={}, date={}", saved.getEmployeeId(), saved.getAttendanceDate());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt2002Attendance update(Long id, HrIt2002Attendance entity) {
        HrIt2002Attendance existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("考勤记录不存在"));
        existing.setShiftId(entity.getShiftId());
        existing.setShiftName(entity.getShiftName());
        existing.setScheduledClockIn(entity.getScheduledClockIn());
        existing.setScheduledClockOut(entity.getScheduledClockOut());
        existing.setActualClockIn(entity.getActualClockIn());
        existing.setActualClockOut(entity.getActualClockOut());
        existing.setClockInMethod(entity.getClockInMethod());
        existing.setClockOutMethod(entity.getClockOutMethod());
        existing.setClockInLocation(entity.getClockInLocation());
        existing.setClockOutLocation(entity.getClockOutLocation());
        existing.setClockInIp(entity.getClockInIp());
        existing.setClockOutIp(entity.getClockOutIp());
        existing.setAttendanceStatus(entity.getAttendanceStatus());
        existing.setLateMinutes(entity.getLateMinutes());
        existing.setEarlyLeaveMinutes(entity.getEarlyLeaveMinutes());
        existing.setOvertimeHours(entity.getOvertimeHours());
        existing.setOvertimeType(entity.getOvertimeType());
        existing.setExceptionNote(entity.getExceptionNote());
        existing.setRemark(entity.getRemark());
        // 重新计算工作时长
        if (existing.getActualClockIn() != null && existing.getActualClockOut() != null) {
            existing.setWorkHours(existing.calculateWorkHours());
        }
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt2002Attendance entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("考勤记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt2002Attendance getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("考勤记录不存在"));
    }

    public Optional<HrIt2002Attendance> getByEmployeeAndDate(Long employeeId, LocalDate date) {
        return repository.findByEmployeeIdAndAttendanceDateAndIsDeletedFalse(employeeId, date);
    }

    public List<HrIt2002Attendance> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalseOrderByAttendanceDateDesc(employeeId);
    }

    public List<HrIt2002Attendance> getByEmployeeAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return repository.findByEmployeeIdAndAttendanceDateBetweenAndIsDeletedFalse(employeeId, startDate, endDate);
    }

    public List<HrIt2002Attendance> getByDate(LocalDate date, Long tenantId) {
        return repository.findByAttendanceDateAndTenantIdAndIsDeletedFalse(date, tenantId);
    }

    public List<HrIt2002Attendance> getByDateRange(LocalDate startDate, LocalDate endDate, Long tenantId) {
        return repository.findByAttendanceDateBetweenAndTenantIdAndIsDeletedFalse(startDate, endDate, tenantId);
    }

    public List<HrIt2002Attendance> getAbnormalByDateRange(LocalDate startDate, LocalDate endDate, Long tenantId) {
        return repository.findAbnormalByDateRange(tenantId, startDate, endDate);
    }

    public Page<HrIt2002Attendance> search(Long tenantId, Long employeeId, LocalDate startDate,
                                            LocalDate endDate, String status, Pageable pageable) {
        return repository.search(tenantId, employeeId, startDate, endDate, status, pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public void clockIn(Long employeeId, String employeeNo, LocalDateTime clockTime,
                        String method, String location, String ip) {
        LocalDate date = clockTime.toLocalDate();
        HrIt2002Attendance attendance = repository
                .findByEmployeeIdAndAttendanceDateAndIsDeletedFalse(employeeId, date)
                .orElse(null);
        if (attendance == null) {
            attendance = HrIt2002Attendance.builder()
                    .employeeId(employeeId)
                    .employeeNo(employeeNo)
                    .attendanceDate(date)
                    .actualClockIn(clockTime)
                    .clockInMethod(method)
                    .clockInLocation(location)
                    .clockInIp(ip)
                    .attendanceStatus("1")
                    .validFrom(date)
                    .validTo(date)
                    .build();
        } else {
            attendance.setActualClockIn(clockTime);
            attendance.setClockInMethod(method);
            attendance.setClockInLocation(location);
            attendance.setClockInIp(ip);
        }
        repository.save(attendance);
        log.info("打卡上班: employeeId={}, time={}", employeeId, clockTime);
    }

    @Transactional(rollbackFor = Exception.class)
    public void clockOut(Long employeeId, LocalDateTime clockTime,
                         String method, String location, String ip) {
        LocalDate date = clockTime.toLocalDate();
        HrIt2002Attendance attendance = repository
                .findByEmployeeIdAndAttendanceDateAndIsDeletedFalse(employeeId, date)
                .orElseThrow(() -> new BusinessException("未找到当日上班打卡记录"));
        attendance.setActualClockOut(clockTime);
        attendance.setClockOutMethod(method);
        attendance.setClockOutLocation(location);
        attendance.setClockOutIp(ip);
        attendance.setWorkHours(attendance.calculateWorkHours());
        repository.save(attendance);
        log.info("打卡下班: employeeId={}, time={}", employeeId, clockTime);
    }

    @Transactional(rollbackFor = Exception.class)
    public void manualClockIn(Long id, String reason, String approvedBy) {
        HrIt2002Attendance attendance = repository.findById(id)
                .orElseThrow(() -> new BusinessException("考勤记录不存在"));
        attendance.setClockInMethod("5"); // 手动补签
        attendance.setManualReason(reason);
        attendance.setManualApprovedBy(approvedBy);
        attendance.setManualApprovedAt(LocalDateTime.now());
        repository.save(attendance);
        log.info("补签审批通过: id={}", id);
    }

    public Long countAbnormalDays(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return repository.countAbnormalDays(employeeId, startDate, endDate);
    }

    public BigDecimal sumOvertimeHours(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return repository.sumOvertimeHours(employeeId, startDate, endDate)
                .orElse(BigDecimal.ZERO);
    }
}
