package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FiAccountGroup;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 科目组 Repository
 *
 * @author NextERP
 */
@Repository
public interface FiAccountGroupRepository extends TenantAwareRepository<FiAccountGroup> {

    /**
     * 根据代码查找
     */
    Optional<FiAccountGroup> findByGroupCodeAndTenantIdAndIsDeletedFalse(String groupCode, Long tenantId);

    /**
     * 检查代码是否存在
     */
    boolean existsByGroupCodeAndTenantIdAndIsDeletedFalse(String groupCode, Long tenantId);

    /**
     * 根据科目表ID查询
     */
    List<FiAccountGroup> findByCoaIdAndTenantIdAndIsDeletedFalseOrderBySortOrder(Long coaId, Long tenantId);

    /**
     * 根据科目类型查询
     */
    List<FiAccountGroup> findByAccountTypeAndTenantIdAndIsDeletedFalseOrderBySortOrder(String accountType, Long tenantId);

    /**
     * 根据父ID查询子科目组
     */
    List<FiAccountGroup> findByParentIdAndTenantIdAndIsDeletedFalseOrderBySortOrder(Long parentId, Long tenantId);

    /**
     * 查询顶级科目组
     */
    List<FiAccountGroup> findByParentIdIsNullAndTenantIdAndIsDeletedFalseOrderBySortOrder(Long tenantId);

    /**
     * 查询统驭科目组
     */
    List<FiAccountGroup> findByIsReconciliationTrueAndTenantIdAndIsDeletedFalseOrderBySortOrder(Long tenantId);

    /**
     * 查询所有启用的科目组
     */
    List<FiAccountGroup> findByStatusAndTenantIdAndIsDeletedFalseOrderBySortOrder(Integer status, Long tenantId);
}
