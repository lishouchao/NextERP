package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrPayrollItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 薪酬项目明细 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrPayrollItemRepository extends JpaRepository<HrPayrollItem, Long>,
        JpaSpecificationExecutor<HrPayrollItem> {

    /**
     * 根据薪酬结果ID查询所有明细
     */
    List<HrPayrollItem> findByPayrollResultIdAndIsDeletedFalseOrderByWageCategoryAscSortOrderAsc(
            Long payrollResultId);

    /**
     * 根据员工和期间查询
     */
    List<HrPayrollItem> findByEmployeeIdAndPayrollPeriodAndIsDeletedFalse(
            Long employeeId, String payrollPeriod);

    /**
     * 根据薪酬结果和工资类型查询
     */
    Optional<HrPayrollItem> findByPayrollResultIdAndWageTypeIdAndIsDeletedFalse(
            Long payrollResultId, Long wageTypeId);

    /**
     * 根据工资分类查询
     */
    List<HrPayrollItem> findByPayrollResultIdAndWageCategoryAndIsDeletedFalse(
            Long payrollResultId, String wageCategory);

    /**
     * 查询加项明细
     */
    @Query("SELECT pi FROM HrPayrollItem pi WHERE pi.payrollResultId = :resultId " +
           "AND pi.dcIndicator = 'D' AND pi.isDeleted = false ORDER BY pi.sortOrder")
    List<HrPayrollItem> findAdditions(@Param("resultId") Long resultId);

    /**
     * 查询减项明细
     */
    @Query("SELECT pi FROM HrPayrollItem pi WHERE pi.payrollResultId = :resultId " +
           "AND pi.dcIndicator = 'C' AND pi.isDeleted = false ORDER BY pi.sortOrder")
    List<HrPayrollItem> findDeductions(@Param("resultId") Long resultId);

    /**
     * 统计薪酬结果的加项合计
     */
    @Query("SELECT SUM(pi.amount) FROM HrPayrollItem pi " +
           "WHERE pi.payrollResultId = :resultId AND pi.dcIndicator = 'D' " +
           "AND pi.isDeleted = false")
    Optional<BigDecimal> sumAdditions(@Param("resultId") Long resultId);

    /**
     * 统计薪酬结果的减项合计
     */
    @Query("SELECT SUM(pi.amount) FROM HrPayrollItem pi " +
           "WHERE pi.payrollResultId = :resultId AND pi.dcIndicator = 'C' " +
           "AND pi.isDeleted = false")
    Optional<BigDecimal> sumDeductions(@Param("resultId") Long resultId);

    /**
     * 删除薪酬结果的所有明细 (软删除)
     */
    @Query("UPDATE HrPayrollItem pi SET pi.isDeleted = true " +
           "WHERE pi.payrollResultId = :resultId")
    void softDeleteByResultId(@Param("resultId") Long resultId);

    /**
     * 按工资类型统计
     */
    @Query("SELECT pi.wageTypeId, pi.wageTypeCode, pi.wageTypeName, SUM(pi.amount) " +
           "FROM HrPayrollItem pi WHERE pi.payrollPeriod = :payrollPeriod " +
           "AND pi.tenantId = :tenantId AND pi.isDeleted = false " +
           "GROUP BY pi.wageTypeId, pi.wageTypeCode, pi.wageTypeName")
    List<Object[]> sumByWageType(@Param("tenantId") Long tenantId,
                                  @Param("payrollPeriod") String payrollPeriod);
}
