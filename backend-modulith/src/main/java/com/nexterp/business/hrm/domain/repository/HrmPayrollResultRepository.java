package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmPayrollResult;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 薪酬结果仓储接口
 *
 * @author NextERP
 */
@Repository
public interface HrmPayrollResultRepository extends TenantAwareRepository<HrmPayrollResult> {

    /**
     * 根据薪酬期间查询
     */
    List<HrmPayrollResult> findByPayrollPeriodAndTenantIdAndIsDeletedFalse(String payrollPeriod, Long tenantId);

    /**
     * 按员工ID和薪酬期间查询
     */
    Optional<HrmPayrollResult> findByEmployeeIdAndPayrollPeriodAndTenantIdAndIsDeletedFalse(
            Long employeeId, String payrollPeriod, Long tenantId);

    /**
     * 按员工查询所有薪酬结果
     */
    List<HrmPayrollResult> findByEmployeeIdAndTenantIdAndIsDeletedFalseOrderByPayrollPeriodDesc(
            Long employeeId, Long tenantId);

    /**
     * 按部门查询
     */
    List<HrmPayrollResult> findByDepartmentIdAndPayrollPeriodAndTenantIdAndIsDeletedFalse(
            Long departmentId, String payrollPeriod, Long tenantId);

    /**
     * 按状态查询
     */
    List<HrmPayrollResult> findByPayrollStatusAndTenantIdAndIsDeletedFalse(String payrollStatus, Long tenantId);
}
