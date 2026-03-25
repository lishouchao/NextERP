package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrLeaveQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 假期额度 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrLeaveQuotaRepository extends JpaRepository<HrLeaveQuota, Long>,
        JpaSpecificationExecutor<HrLeaveQuota> {

    /**
     * 根据员工和年度查询
     */
    List<HrLeaveQuota> findByEmployeeIdAndQuotaYearAndIsDeletedFalse(
            Long employeeId, Integer quotaYear);

    /**
     * 根据员工、假期类型和年度查询
     */
    Optional<HrLeaveQuota> findByEmployeeIdAndLeaveTypeIdAndQuotaYearAndIsDeletedFalse(
            Long employeeId, Long leaveTypeId, Integer quotaYear);

    /**
     * 根据员工和假期类型查询所有年度
     */
    List<HrLeaveQuota> findByEmployeeIdAndLeaveTypeIdAndIsDeletedFalseOrderByQuotaYearDesc(
            Long employeeId, Long leaveTypeId);

    /**
     * 查询员工所有额度
     */
    List<HrLeaveQuota> findByEmployeeIdAndIsDeletedFalse(Long employeeId);

    /**
     * 增加已用额度
     */
    @Modifying
    @Query("UPDATE HrLeaveQuota q SET q.usedDays = q.usedDays + :days " +
           "WHERE q.id = :id AND q.usedDays + :days <= q.totalDays")
    int addUsedDays(@Param("id") Long id, @Param("days") BigDecimal days);

    /**
     * 减少已用额度 (撤销请假时)
     */
    @Modifying
    @Query("UPDATE HrLeaveQuota q SET q.usedDays = q.usedDays - :days " +
           "WHERE q.id = :id AND q.usedDays >= :days")
    int subtractUsedDays(@Param("id") Long id, @Param("days") BigDecimal days);

    /**
     * 增加待审批额度
     */
    @Modifying
    @Query("UPDATE HrLeaveQuota q SET q.pendingDays = q.pendingDays + :days " +
           "WHERE q.id = :id")
    int addPendingDays(@Param("id") Long id, @Param("days") BigDecimal days);

    /**
     * 减少待审批额度
     */
    @Modifying
    @Query("UPDATE HrLeaveQuota q SET q.pendingDays = q.pendingDays - :days " +
           "WHERE q.id = :id AND q.pendingDays >= :days")
    int subtractPendingDays(@Param("id") Long id, @Param("days") BigDecimal days);

    /**
     * 查询有剩余额度的记录
     */
    @Query("SELECT q FROM HrLeaveQuota q WHERE q.employeeId = :employeeId " +
           "AND q.leaveTypeId = :leaveTypeId AND q.isDeleted = false " +
           "AND q.totalDays > q.usedDays ORDER BY q.quotaYear ASC")
    List<HrLeaveQuota> findWithRemaining(@Param("employeeId") Long employeeId,
                                          @Param("leaveTypeId") Long leaveTypeId);

    /**
     * 查询即将过期的结转额度
     */
    @Query("SELECT q FROM HrLeaveQuota q WHERE q.tenantId = :tenantId " +
           "AND q.carriedOverDays > 0 AND q.carryOverExpireDate IS NOT NULL " +
           "AND q.carryOverExpireDate BETWEEN :startDate AND :endDate")
    List<HrLeaveQuota> findExpiringCarryOver(@Param("tenantId") Long tenantId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 根据租户和年度查询
     */
    List<HrLeaveQuota> findByTenantIdAndQuotaYearAndIsDeletedFalse(Long tenantId, Integer quotaYear);
}
