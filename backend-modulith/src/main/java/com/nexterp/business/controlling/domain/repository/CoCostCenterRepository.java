package com.nexterp.business.controlling.domain.repository;

import com.nexterp.business.controlling.domain.model.CoCostCenter;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 成本中心仓储接口
 *
 * @author NextERP
 */
@Repository
public interface CoCostCenterRepository extends TenantAwareRepository<CoCostCenter> {

    /**
     * 根据成本中心代码查询
     */
    Optional<CoCostCenter> findByCostCenterCodeAndTenantIdAndIsDeletedFalse(String costCenterCode, Long tenantId);

    /**
     * 检查成本中心代码是否存在
     */
    boolean existsByCostCenterCodeAndTenantIdAndIsDeletedFalse(String costCenterCode, Long tenantId);

    /**
     * 按成本中心组查询
     */
    List<CoCostCenter> findByCostCenterGroupIdAndTenantIdAndIsDeletedFalseOrderByCostCenterCodeAsc(Long groupId, Long tenantId);

    /**
     * 按成本中心类型查询
     */
    List<CoCostCenter> findByCostCenterTypeAndTenantIdAndIsDeletedFalseOrderByCostCenterCodeAsc(String costCenterType, Long tenantId);

    /**
     * 按公司代码查询
     */
    List<CoCostCenter> findByCompanyCodeAndTenantIdAndIsDeletedFalseOrderByCostCenterCodeAsc(String companyCode, Long tenantId);

    /**
     * 按成本控制范围查询
     */
    List<CoCostCenter> findByControllingAreaAndTenantIdAndIsDeletedFalseOrderByCostCenterCodeAsc(String controllingArea, Long tenantId);

    /**
     * 按部门查询
     */
    List<CoCostCenter> findByDepartmentIdAndTenantIdAndIsDeletedFalseOrderByCostCenterCodeAsc(Long departmentId, Long tenantId);

    /**
     * 查询有效成本中心
     */
    List<CoCostCenter> findByTenantIdAndIsDeletedFalseAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByCostCenterCodeAsc(
            Long tenantId, LocalDate validFrom, LocalDate validTo);

    /**
     * 查询启用的成本中心
     */
    List<CoCostCenter> findByTenantIdAndIsDeletedFalseAndStatusOrderByCostCenterCodeAsc(Long tenantId, Integer status);
}
