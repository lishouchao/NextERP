package com.nexterp.business.controlling.domain.repository;

import com.nexterp.business.controlling.domain.model.CoCostCenterGroup;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 成本中心组仓储接口
 *
 * @author NextERP
 */
@Repository
public interface CoCostCenterGroupRepository extends TenantAwareRepository<CoCostCenterGroup> {

    /**
     * 根据组代码查询
     */
    Optional<CoCostCenterGroup> findByGroupCodeAndTenantIdAndIsDeletedFalse(String groupCode, Long tenantId);

    /**
     * 检查组代码是否存在
     */
    boolean existsByGroupCodeAndTenantIdAndIsDeletedFalse(String groupCode, Long tenantId);

    /**
     * 查询根节点组
     */
    List<CoCostCenterGroup> findByParentGroupIdIsNullAndTenantIdAndIsDeletedFalseOrderByGroupCodeAsc(Long tenantId);

    /**
     * 查询子组
     */
    List<CoCostCenterGroup> findByParentGroupIdAndTenantIdAndIsDeletedFalseOrderByGroupCodeAsc(Long parentGroupId, Long tenantId);

    /**
     * 按成本控制范围查询
     */
    List<CoCostCenterGroup> findByControllingAreaAndTenantIdAndIsDeletedFalseOrderByGroupCodeAsc(String controllingArea, Long tenantId);

    /**
     * 查询叶子节点组
     */
    List<CoCostCenterGroup> findByIsLeafTrueAndTenantIdAndIsDeletedFalseOrderByGroupCodeAsc(Long tenantId);

    /**
     * 查询所有组（按层级排序）
     */
    List<CoCostCenterGroup> findByTenantIdAndIsDeletedFalseOrderByHierarchyLevelAscGroupCodeAsc(Long tenantId);
}
