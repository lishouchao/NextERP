package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt0008BasicPay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0008 - 基本工资 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt0008BasicPayRepository extends JpaRepository<HrIt0008BasicPay, Long>,
        JpaSpecificationExecutor<HrIt0008BasicPay> {

    /**
     * 根据员工ID查询
     */
    List<HrIt0008BasicPay> findByEmployeeIdAndIsDeletedFalse(Long employeeId);

    /**
     * 根据员工编号查询
     */
    List<HrIt0008BasicPay> findByEmployeeNoAndTenantIdAndIsDeletedFalse(String employeeNo, Long tenantId);

    /**
     * 查询指定日期有效的记录
     */
    @Query("SELECT p FROM HrIt0008BasicPay p WHERE p.employeeId = :employeeId " +
           "AND p.isDeleted = false " +
           "AND p.validFrom <= :keyDate AND p.validTo >= :keyDate")
    Optional<HrIt0008BasicPay> findValidOnDate(@Param("employeeId") Long employeeId,
                                                @Param("keyDate") LocalDate keyDate);

    /**
     * 查询当前薪资
     */
    @Query("SELECT p FROM HrIt0008BasicPay p WHERE p.employeeId = :employeeId " +
           "AND p.approvalStatus = '1' AND p.isDeleted = false " +
           "AND p.validFrom <= :keyDate AND p.validTo >= :keyDate")
    Optional<HrIt0008BasicPay> findCurrentPay(@Param("employeeId") Long employeeId,
                                               @Param("keyDate") LocalDate keyDate);

    /**
     * 根据薪资等级查询
     */
    List<HrIt0008BasicPay> findByPayGradeAndTenantIdAndIsDeletedFalse(
            String payGrade, Long tenantId);

    /**
     * 根据工资组查询
     */
    List<HrIt0008BasicPay> findByPayGroupAndTenantIdAndIsDeletedFalse(
            String payGroup, Long tenantId);

    /**
     * 查询待审批的调薪记录
     */
    @Query("SELECT p FROM HrIt0008BasicPay p WHERE p.tenantId = :tenantId " +
           "AND p.approvalStatus = '0' AND p.isDeleted = false")
    List<HrIt0008BasicPay> findPendingApproval(@Param("tenantId") Long tenantId);

    /**
     * 查询员工调薪历史
     */
    @Query("SELECT p FROM HrIt0008BasicPay p WHERE p.employeeId = :employeeId " +
           "AND p.isDeleted = false ORDER BY p.validFrom DESC")
    List<HrIt0008BasicPay> findHistoryByEmployee(@Param("employeeId") Long employeeId);
}
