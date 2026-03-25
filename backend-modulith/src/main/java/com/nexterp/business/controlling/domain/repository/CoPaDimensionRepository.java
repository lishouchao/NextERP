package com.nexterp.business.controlling.domain.repository;

import com.nexterp.business.controlling.domain.model.CoPaDimension;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PA维度仓储接口
 *
 * @author NextERP
 */
@Repository
public interface CoPaDimensionRepository extends TenantAwareRepository<CoPaDimension> {

    /**
     * 根据维度代码查询
     */
    Optional<CoPaDimension> findByDimensionCodeAndTenantIdAndIsDeletedFalse(String dimensionCode, Long tenantId);

    /**
     * 检查维度代码是否存在
     */
    boolean existsByDimensionCodeAndTenantIdAndIsDeletedFalse(String dimensionCode, Long tenantId);

    /**
     * 按维度类型查询
     */
    List<CoPaDimension> findByDimensionTypeAndTenantIdAndIsDeletedFalseOrderBySortOrderAsc(String dimensionType, Long tenantId);

    /**
     * 按经营范围查询
     */
    List<CoPaDimension> findByOperatingConcernAndTenantIdAndIsDeletedFalseOrderBySortOrderAsc(String operatingConcern, Long tenantId);

    /**
     * 查询启用的维度
     */
    List<CoPaDimension> findByTenantIdAndIsDeletedFalseAndStatusOrderBySortOrderAsc(Long tenantId, Integer status);

    /**
     * 查询所有维度（按排序号排序）
     */
    List<CoPaDimension> findByTenantIdAndIsDeletedFalseOrderBySortOrderAsc(Long tenantId);
}
