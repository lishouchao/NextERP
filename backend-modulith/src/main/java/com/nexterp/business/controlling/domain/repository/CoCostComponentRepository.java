package com.nexterp.business.controlling.domain.repository;

import com.nexterp.business.controlling.domain.model.CoCostComponent;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 成本构成仓储接口
 *
 * @author NextERP
 */
@Repository
public interface CoCostComponentRepository extends TenantAwareRepository<CoCostComponent> {

    /**
     * 按成本估算查询
     */
    List<CoCostComponent> findByCostEstimateIdAndTenantIdAndIsDeletedFalseOrderByLineNoAsc(Long costEstimateId, Long tenantId);

    /**
     * 按成本类别查询
     */
    List<CoCostComponent> findByCostCategoryAndTenantIdAndIsDeletedFalse(String costCategory, Long tenantId);

    /**
     * 按成本要素查询
     */
    List<CoCostComponent> findByCostElementIdAndTenantIdAndIsDeletedFalse(Long costElementId, Long tenantId);

    /**
     * 按成本中心查询
     */
    List<CoCostComponent> findByCostCenterIdAndTenantIdAndIsDeletedFalse(Long costCenterId, Long tenantId);
}
