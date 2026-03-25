package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmEmployee;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 员工仓储接口
 *
 * @author NextERP
 */
@Repository
public interface HrmEmployeeRepository extends TenantAwareRepository<HrmEmployee> {

    /**
     * 根据员工编号查询
     */
    Optional<HrmEmployee> findByEmployeeNoAndTenantIdAndIsDeletedFalse(String employeeNo, Long tenantId);

    /**
     * 检查员工编号是否存在
     */
    boolean existsByEmployeeNoAndTenantIdAndIsDeletedFalse(String employeeNo, Long tenantId);

    /**
     * 按部门查询员工
     */
    List<HrmEmployee> findByDepartmentIdAndTenantIdAndIsDeletedFalseOrderByEmployeeNoAsc(Long departmentId, Long tenantId);

    /**
     * 按工作状态查询员工
     */
    List<HrmEmployee> findByWorkStatusAndTenantIdAndIsDeletedFalseOrderByEmployeeNoAsc(Integer workStatus, Long tenantId);

    /**
     * 按直属上级查询
     */
    List<HrmEmployee> findBySupervisorIdAndTenantIdAndIsDeletedFalse(Long supervisorId, Long tenantId);

    /**
     * 按岗位查询员工
     */
    List<HrmEmployee> findByPositionIdAndTenantIdAndIsDeletedFalse(Long positionId, Long tenantId);

    /**
     * 按姓名模糊查询
     */
    List<HrmEmployee> findByEmployeeNameContainingAndTenantIdAndIsDeletedFalse(String employeeName, Long tenantId);

    /**
     * 查询在职员工
     */
    List<HrmEmployee> findByWorkStatusNotAndTenantIdAndIsDeletedFalseOrderByEmployeeNoAsc(Integer workStatus, Long tenantId);
}
