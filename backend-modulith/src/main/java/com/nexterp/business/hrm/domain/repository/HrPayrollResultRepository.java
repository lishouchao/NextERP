package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrPayrollResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 薪酬结果 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrPayrollResultRepository extends JpaRepository<HrPayrollResult, Long>,
        JpaSpecificationExecutor<HrPayrollResult> {

    /**
     * 根据员工和薪酬期间查询
     */
    Optional<HrPayrollResult> findByEmployeeIdAndPayrollPeriodAndIsDeletedFalse(
            Long employeeId, String payrollPeriod);

    /**
     * 根据薪酬期间查询所有结果
     */
    List<HrPayrollResult> findByPayrollPeriodAndTenantIdAndIsDeletedFalse(
            String payrollPeriod, Long tenantId);

    /**
     * 根据批次号查询
     */
    List<HrPayrollResult> findByBatchNoAndTenantIdAndIsDeletedFalse(
            String batchNo, Long tenantId);

    /**
     * 根据薪酬状态查询
     */
    List<HrPayrollResult> findByPayrollStatusAndTenantIdAndIsDeletedFalse(
            String payrollStatus, Long tenantId);

    /**
     * 根据员工查询历史记录
     */
    List<HrPayrollResult> findByEmployeeIdAndIsDeletedFalseOrderByPayrollPeriodDesc(
            Long employeeId);

    /**
     * 根据年度查询
     */
    List<HrPayrollResult> findByPayrollYearAndTenantIdAndIsDeletedFalse(
            Integer payrollYear, Long tenantId);

    /**
     * 根据组织和期间查询
     */
    List<HrPayrollResult> findByOrgUnitIdAndPayrollPeriodAndIsDeletedFalse(
            Long orgUnitId, String payrollPeriod);

    /**
     * 检查员工期间是否已有薪酬记录
     */
    boolean existsByEmployeeIdAndPayrollPeriodAndIsDeletedFalse(
            Long employeeId, String payrollPeriod);

    /**
     * 统计期间薪酬总额
     */
    @Query("SELECT SUM(pr.grossPay), SUM(pr.netPay) FROM HrPayrollResult pr " +
           "WHERE pr.payrollPeriod = :payrollPeriod AND pr.tenantId = :tenantId " +
           "AND pr.isDeleted = false")
    Optional<Object[]> sumByPeriod(@Param("tenantId") Long tenantId,
                                    @Param("payrollPeriod") String payrollPeriod);

    /**
     * 更新薪酬状态
     */
    @Modifying
    @Query("UPDATE HrPayrollResult pr SET pr.payrollStatus = :status " +
           "WHERE pr.payrollPeriod = :payrollPeriod AND pr.tenantId = :tenantId")
    int updateStatusByPeriod(@Param("tenantId") Long tenantId,
                              @Param("payrollPeriod") String payrollPeriod,
                              @Param("status") String status);

    /**
     * 审批通过
     */
    @Modifying
    @Query("UPDATE HrPayrollResult pr SET pr.payrollStatus = '2', " +
           "pr.approvedBy = :approvedBy, pr.approvedAt = CURRENT_DATE " +
           "WHERE pr.id = :id")
    int approve(@Param("id") Long id, @Param("approvedBy") String approvedBy);

    /**
     * 标记发放
     */
    @Modifying
    @Query("UPDATE HrPayrollResult pr SET pr.payrollStatus = '3', " +
           "pr.paidAt = CURRENT_DATE WHERE pr.id = :id")
    int markPaid(@Param("id") Long id);

    /**
     * 撤销
     */
    @Modifying
    @Query("UPDATE HrPayrollResult pr SET pr.payrollStatus = '4' WHERE pr.id = :id")
    int revoke(@Param("id") Long id);

    /**
     * 按组织统计薪酬
     */
    @Query("SELECT pr.orgUnitId, pr.orgUnitName, COUNT(pr), SUM(pr.grossPay), SUM(pr.netPay) " +
           "FROM HrPayrollResult pr WHERE pr.payrollPeriod = :payrollPeriod " +
           "AND pr.tenantId = :tenantId AND pr.isDeleted = false " +
           "GROUP BY pr.orgUnitId, pr.orgUnitName")
    List<Object[]> statsByOrg(@Param("tenantId") Long tenantId,
                               @Param("payrollPeriod") String payrollPeriod);

    /**
     * 搜索薪酬记录
     */
    @Query("SELECT pr FROM HrPayrollResult pr WHERE pr.tenantId = :tenantId " +
           "AND (:payrollPeriod IS NULL OR pr.payrollPeriod = :payrollPeriod) " +
           "AND (:orgUnitId IS NULL OR pr.orgUnitId = :orgUnitId) " +
           "AND (:status IS NULL OR pr.payrollStatus = :status) " +
           "AND pr.isDeleted = false ORDER BY pr.payrollPeriod DESC, pr.employeeNo")
    org.springframework.data.domain.Page<HrPayrollResult> search(
            @Param("tenantId") Long tenantId,
            @Param("payrollPeriod") String payrollPeriod,
            @Param("orgUnitId") Long orgUnitId,
            @Param("status") String status,
            org.springframework.data.domain.Pageable pageable);
}
