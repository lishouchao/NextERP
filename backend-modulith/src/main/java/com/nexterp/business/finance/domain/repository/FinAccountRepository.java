package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FinAccount;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 财务科目仓储接口
 *
 * @author NextERP
 */
@Repository
public interface FinAccountRepository extends TenantAwareRepository<FinAccount> {

    /**
     * 根据科目编码查询
     *
     * @param accountCode 科目编码
     * @param tenantId    租户ID
     * @return 科目
     */
    Optional<FinAccount> findByAccountCodeAndTenantIdAndIsDeletedFalse(String accountCode, Long tenantId);

    /**
     * 检查科目编码是否存在
     *
     * @param accountCode 科目编码
     * @param tenantId    租户ID
     * @return 是否存在
     */
    boolean existsByAccountCodeAndTenantIdAndIsDeletedFalse(String accountCode, Long tenantId);

    /**
     * 查询租户所有科目（按排序号排序）
     *
     * @param tenantId 租户ID
     * @return 科目列表
     */
    List<FinAccount> findByTenantIdAndIsDeletedFalseOrderBySortOrderAscIdAsc(Long tenantId);

    /**
     * 查询指定类型的科目
     *
     * @param accountType 科目类型
     * @param tenantId    租户ID
     * @return 科目列表
     */
    List<FinAccount> findByAccountTypeAndTenantIdAndIsDeletedFalseOrderByAccountCodeAsc(Integer accountType, Long tenantId);

    /**
     * 查询叶子科目
     *
     * @param tenantId 租户ID
     * @return 科目列表
     */
    List<FinAccount> findByIsLeafTrueAndTenantIdAndIsDeletedFalseOrderByAccountCodeAsc(Long tenantId);

    /**
     * 查询启用状态的科目
     *
     * @param tenantId 租户ID
     * @return 科目列表
     */
    @Query("SELECT a FROM FinAccount a WHERE a.tenantId = :tenantId AND a.isDeleted = false AND a.status = 1 ORDER BY a.accountCode")
    List<FinAccount> findActiveAccounts(@Param("tenantId") Long tenantId);

    /**
     * 查询指定层级的科目
     *
     * @param level    层级
     * @param tenantId 租户ID
     * @return 科目列表
     */
    List<FinAccount> findByAccountLevelAndTenantIdAndIsDeletedFalseOrderByAccountCodeAsc(Integer level, Long tenantId);

    /**
     * 查询子科目
     *
     * @param parentId 父科目ID
     * @param tenantId 租户ID
     * @return 科目列表
     */
    List<FinAccount> findByParentIdAndTenantIdAndIsDeletedFalseOrderByAccountCodeAsc(Long parentId, Long tenantId);

    /**
     * 检查是否存在子科目
     *
     * @param parentId 父科目ID
     * @param tenantId 租户ID
     * @return 是否存在
     */
    boolean existsByParentIdAndTenantIdAndIsDeletedFalse(Long parentId, Long tenantId);

    /**
     * 查询现金科目
     *
     * @param tenantId 租户ID
     * @return 科目列表
     */
    List<FinAccount> findByIsCashTrueAndTenantIdAndIsDeletedFalse(Long tenantId);

    /**
     * 查询银行科目
     *
     * @param tenantId 租户ID
     * @return 科目列表
     */
    List<FinAccount> findByIsBankTrueAndTenantIdAndIsDeletedFalse(Long tenantId);
}
