package com.nexterp.business.controlling.domain.repository;

import com.nexterp.business.controlling.domain.model.CoCostElement;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 成本要素仓储接口
 *
 * @author NextERP
 */
@Repository
public interface CoCostElementRepository extends TenantAwareRepository<CoCostElement> {

    /**
     * 根据成本要素代码查询
     */
    Optional<CoCostElement> findByElementCodeAndTenantIdAndIsDeletedFalse(String elementCode, Long tenantId);

    /**
     * 检查成本要素代码是否存在
     */
    boolean existsByElementCodeAndTenantIdAndIsDeletedFalse(String elementCode, Long tenantId);

    /**
     * 按成本要素类型查询
     */
    List<CoCostElement> findByElementTypeAndTenantIdAndIsDeletedFalseOrderByElementCodeAsc(String elementType, Long tenantId);

    /**
     * 按成本要素类别查询
     */
    List<CoCostElement> findByElementCategoryAndTenantIdAndIsDeletedFalseOrderByElementCodeAsc(String elementCategory, Long tenantId);

    /**
     * 查询有效成本要素
     */
    List<CoCostElement> findByTenantIdAndIsDeletedFalseAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByElementCodeAsc(
            Long tenantId, LocalDate validFrom, LocalDate validTo);

    /**
     * 按GL科目查询
     */
    Optional<CoCostElement> findByGlAccountCodeAndTenantIdAndIsDeletedFalse(String glAccountCode, Long tenantId);

    /**
     * 查询启用的成本要素
     */
    List<CoCostElement> findByTenantIdAndIsDeletedFalseAndStatusOrderByElementCodeAsc(Long tenantId, Integer status);
}
