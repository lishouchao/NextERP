package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt2002Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 2002 - 考勤 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt2002AttendanceRepository extends JpaRepository<HrIt2002Attendance, Long>,
        JpaSpecificationExecutor<HrIt2002Attendance> {

    /**
     * 根据员工和日期查询
     */
    Optional<HrIt2002Attendance> findByEmployeeIdAndAttendanceDateAndIsDeletedFalse(
            Long employeeId, LocalDate attendanceDate);

    /**
     * 根据员工查询月度考勤
     */
    @Query("SELECT a FROM HrIt2002Attendance a WHERE a.employeeId = :employeeId " +
           "AND a.isDeleted = false " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.attendanceDate")
    List<HrIt2002Attendance> findByMonth(@Param("employeeId") Long employeeId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    /**
     * 根据考勤状态查询
     */
    List<HrIt2002Attendance> findByAttendanceStatusAndTenantIdAndIsDeletedFalse(
            String attendanceStatus, Long tenantId);

    /**
     * 查询异常考勤
     */
    @Query("SELECT a FROM HrIt2002Attendance a WHERE a.tenantId = :tenantId " +
           "AND a.attendanceStatus IN ('2', '3', '4') AND a.isDeleted = false " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<HrIt2002Attendance> findAbnormal(@Param("tenantId") Long tenantId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 统计月度迟到次数
     */
    @Query("SELECT COUNT(a) FROM HrIt2002Attendance a WHERE a.employeeId = :employeeId " +
           "AND a.attendanceStatus = '2' AND a.isDeleted = false " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Long countLateByMonth(@Param("employeeId") Long employeeId,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);

    /**
     * 统计月度早退次数
     */
    @Query("SELECT COUNT(a) FROM HrIt2002Attendance a WHERE a.employeeId = :employeeId " +
           "AND a.attendanceStatus = '3' AND a.isDeleted = false " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Long countEarlyLeaveByMonth(@Param("employeeId") Long employeeId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    /**
     * 统计月度加班时长
     */
    @Query("SELECT SUM(a.overtimeHours) FROM HrIt2002Attendance a WHERE a.employeeId = :employeeId " +
           "AND a.isDeleted = false " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumOvertimeByMonth(@Param("employeeId") Long employeeId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * 查询未打卡记录
     */
    @Query("SELECT a FROM HrIt2002Attendance a WHERE a.employeeId = :employeeId " +
           "AND a.isDeleted = false " +
           "AND (a.actualClockIn IS NULL OR a.actualClockOut IS NULL) " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<HrIt2002Attendance> findMissingClock(@Param("employeeId") Long employeeId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 查询待补签记录
     */
    @Query("SELECT a FROM HrIt2002Attendance a WHERE a.tenantId = :tenantId " +
           "AND a.manualReason IS NOT NULL AND a.manualApprovedBy IS NULL " +
           "AND a.isDeleted = false")
    List<HrIt2002Attendance> findPendingManualApproval(@Param("tenantId") Long tenantId);

    /**
     * 批量查询
     */
    @Query("SELECT a FROM HrIt2002Attendance a WHERE a.employeeId IN :employeeIds " +
           "AND a.attendanceDate IN :dates AND a.isDeleted = false")
    List<HrIt2002Attendance> findByEmployeeIdsAndDates(@Param("employeeIds") List<Long> employeeIds,
                                                        @Param("dates") List<LocalDate> dates);

    /**
     * 根据员工ID查询所有考勤
     */
    List<HrIt2002Attendance> findByEmployeeIdAndIsDeletedFalseOrderByAttendanceDateDesc(Long employeeId);

    /**
     * 根据员工和日期范围查询
     */
    List<HrIt2002Attendance> findByEmployeeIdAndAttendanceDateBetweenAndIsDeletedFalse(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据日期查询
     */
    List<HrIt2002Attendance> findByAttendanceDateAndTenantIdAndIsDeletedFalse(
            LocalDate attendanceDate, Long tenantId);

    /**
     * 根据日期范围查询
     */
    List<HrIt2002Attendance> findByAttendanceDateBetweenAndTenantIdAndIsDeletedFalse(
            LocalDate startDate, LocalDate endDate, Long tenantId);

    /**
     * 查询异常考勤 (带参数名)
     */
    @Query("SELECT a FROM HrIt2002Attendance a WHERE a.tenantId = :tenantId " +
           "AND a.attendanceStatus IN ('2', '3', '4') AND a.isDeleted = false " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<HrIt2002Attendance> findAbnormalByDateRange(@Param("tenantId") Long tenantId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * 搜索考勤记录
     */
    @Query("SELECT a FROM HrIt2002Attendance a WHERE a.tenantId = :tenantId " +
           "AND (:employeeId IS NULL OR a.employeeId = :employeeId) " +
           "AND (:startDate IS NULL OR a.attendanceDate >= :startDate) " +
           "AND (:endDate IS NULL OR a.attendanceDate <= :endDate) " +
           "AND (:status IS NULL OR a.attendanceStatus = :status) " +
           "AND a.isDeleted = false ORDER BY a.attendanceDate DESC")
    org.springframework.data.domain.Page<HrIt2002Attendance> search(
            @Param("tenantId") Long tenantId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status,
            org.springframework.data.domain.Pageable pageable);

    /**
     * 统计异常天数
     */
    @Query("SELECT COUNT(a) FROM HrIt2002Attendance a WHERE a.employeeId = :employeeId " +
           "AND a.attendanceStatus IN ('2', '3', '4') AND a.isDeleted = false " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Long countAbnormalDays(@Param("employeeId") Long employeeId,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate);

    /**
     * 统计加班时长
     */
    @Query("SELECT COALESCE(SUM(a.overtimeHours), 0) FROM HrIt2002Attendance a " +
           "WHERE a.employeeId = :employeeId AND a.isDeleted = false " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumOvertimeHours(@Param("employeeId") Long employeeId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}
