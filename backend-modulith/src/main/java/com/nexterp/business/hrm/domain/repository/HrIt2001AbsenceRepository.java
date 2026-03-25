package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt2001Absence;
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
 * InfoType 2001 - 请假 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt2001AbsenceRepository extends JpaRepository<HrIt2001Absence, Long>,
        JpaSpecificationExecutor<HrIt2001Absence> {

    /**
     * 根据员工ID查询
     */
    List<HrIt2001Absence> findByEmployeeIdAndIsDeletedFalseOrderByStartDateDesc(Long employeeId);

    /**
     * 根据审批状态查询
     */
    List<HrIt2001Absence> findByApprovalStatusAndTenantIdAndIsDeletedFalse(
            String approvalStatus, Long tenantId);

    /**
     * 查询待审批请假
     */
    @Query("SELECT a FROM HrIt2001Absence a WHERE a.tenantId = :tenantId " +
           "AND a.approvalStatus IN ('0', '1') AND a.isDeleted = false ORDER BY a.createdAt")
    List<HrIt2001Absence> findPendingApproval(@Param("tenantId") Long tenantId);

    /**
     * 查询指定日期范围内的请假
     */
    @Query("SELECT a FROM HrIt2001Absence a WHERE a.employeeId = :employeeId " +
           "AND a.approvalStatus = '2' AND a.isDeleted = false " +
           "AND a.startDate <= :endDate AND a.endDate >= :startDate")
    List<HrIt2001Absence> findByDateRange(@Param("employeeId") Long employeeId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 检查日期冲突
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM HrIt2001Absence a " +
           "WHERE a.employeeId = :employeeId AND a.approvalStatus IN ('0', '1', '2') " +
           "AND a.isDeleted = false " +
           "AND a.startDate <= :endDate AND a.endDate >= :startDate")
    boolean hasDateConflict(@Param("employeeId") Long employeeId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);

    /**
     * 根据假期类型查询
     */
    List<HrIt2001Absence> findByLeaveTypeIdAndTenantIdAndIsDeletedFalse(
            Long leaveTypeId, Long tenantId);

    /**
     * 统计员工年度请假天数
     */
    @Query("SELECT SUM(a.absenceDays) FROM HrIt2001Absence a " +
           "WHERE a.employeeId = :employeeId AND a.leaveTypeId = :leaveTypeId " +
           "AND a.approvalStatus = '2' AND a.isDeleted = false " +
           "AND YEAR(a.startDate) = :year")
    Optional<BigDecimal> sumApprovedDaysByYear(@Param("employeeId") Long employeeId,
                                                @Param("leaveTypeId") Long leaveTypeId,
                                                @Param("year") Integer year);

    /**
     * 查询员工在指定日期的请假记录
     */
    @Query("SELECT a FROM HrIt2001Absence a WHERE a.employeeId = :employeeId " +
           "AND a.approvalStatus = '2' AND a.isDeleted = false " +
           "AND a.startDate <= :date AND a.endDate >= :date")
    Optional<HrIt2001Absence> findByEmployeeAndDate(@Param("employeeId") Long employeeId,
                                                     @Param("date") LocalDate date);

    /**
     * 查询历史记录
     */
    @Query("SELECT a FROM HrIt2001Absence a WHERE a.employeeId = :employeeId " +
           "AND a.isDeleted = false ORDER BY a.startDate DESC")
    List<HrIt2001Absence> findHistoryByEmployee(@Param("employeeId") Long employeeId);

    /**
     * 根据请假单号查询
     */
    Optional<HrIt2001Absence> findByRequestNoAndTenantIdAndIsDeletedFalse(String requestNo, Long tenantId);

    /**
     * 根据日期范围查询员工请假
     */
    @Query("SELECT a FROM HrIt2001Absence a WHERE a.employeeId = :employeeId " +
           "AND a.isDeleted = false " +
           "AND a.startDate <= :endDate AND a.endDate >= :startDate " +
           "ORDER BY a.startDate DESC")
    List<HrIt2001Absence> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    /**
     * 查询待审批人处理的请假
     */
    List<HrIt2001Absence> findByCurrentApproverIdAndApprovalStatusAndIsDeletedFalse(
            Long currentApproverId, String approvalStatus);

    /**
     * 统计员工指定类型年度请假天数
     */
    @Query("SELECT COALESCE(SUM(a.absenceDays), 0) FROM HrIt2001Absence a " +
           "WHERE a.employeeId = :employeeId AND a.leaveTypeId = :leaveTypeId " +
           "AND a.approvalStatus = '2' AND a.isDeleted = false " +
           "AND YEAR(a.startDate) = :year")
    Optional<BigDecimal> sumAbsenceDaysByEmployeeAndTypeAndYear(
            @Param("employeeId") Long employeeId,
            @Param("leaveTypeId") Long leaveTypeId,
            @Param("year") Integer year);
}
