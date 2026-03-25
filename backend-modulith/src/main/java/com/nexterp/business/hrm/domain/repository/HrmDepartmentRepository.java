package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmDepartment;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 部门 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrmDepartmentRepository extends TenantAwareRepository<HrmDepartment> {

    /**
     * 根据部门编码查询
     */
    Optional<HrmDepartment> findByDeptCodeAndTenantIdAndIsDeletedFalse(String deptCode, Long tenantId);

    /**
     * 检查部门编码是否存在
     */
    boolean existsByDeptCodeAndTenantIdAndIsDeletedFalse(String deptCode, Long tenantId);

    /**
     * 根据父部门ID查询子部门
     */
    List<HrmDepartment> findByParentIdAndTenantIdAndIsDeletedFalseOrderBySortOrderAsc(Long parentId, Long tenantId);

    /**
     * 查询根部门
     */
    List<HrmDepartment> findByParentIdIsNullAndTenantIdAndIsDeletedFalseOrderBySortOrderAsc(Long tenantId);

    /**
     * 查询所有启用的部门
     */
    List<HrmDepartment> findByTenantIdAndIsDeletedFalseAndStatusOrderBySortOrderAsc(Long tenantId, Integer status);

    /**
     * 根据负责人ID查询部门
     */
    List<HrmDepartment> findByLeaderIdAndTenantIdAndIsDeletedFalse(Long leaderId, Long tenantId);

    /**
     * 查询有效部门
     */
    List<HrmDepartment> findByTenantIdAndIsDeletedFalseAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByDeptCodeAsc(
            Long tenantId, LocalDate validFrom, LocalDate validTo);

    /**
     * 按成本中心查询
     */
    List<HrmDepartment> findByCostCenterIdAndTenantIdAndIsDeletedFalse(Long costCenterId, Long tenantId);

    /**
     * 查询租户所有部门（按排序号排序）
     */
    List<HrmDepartment> findByTenantIdAndIsDeletedFalseOrderBySortOrderAsc(Long tenantId);
}
