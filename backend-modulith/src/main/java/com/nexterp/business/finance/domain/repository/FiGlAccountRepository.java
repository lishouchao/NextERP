package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FiGlAccount;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 总账科目 Repository
 *
 * @author NextERP
 */
@Repository
public interface FiGlAccountRepository extends TenantAwareRepository<FiGlAccount> {

    /**
     * 根据科目代码查找
     */
    Optional<FiGlAccount> findByAccountCodeAndCoaIdAndTenantIdAndIsDeletedFalse(
            String accountCode, Long coaId, Long tenantId);

    /**
     * 检查科目代码是否存在
     */
    boolean existsByAccountCodeAndCoaIdAndTenantIdAndIsDeletedFalse(
            String accountCode, Long coaId, Long tenantId);

    /**
     * 根据科目表ID查询所有科目
     */
    List<FiGlAccount> findByCoaIdAndTenantIdAndIsDeletedFalseOrderBySortOrder(Long coaId, Long tenantId);

    /**
     * 根据科目组ID查询
     */
    List<FiGlAccount> findByAccountGroupIdAndTenantIdAndIsDeletedFalseOrderByAccountCode(
            Long accountGroupId, Long tenantId);

    /**
     * 根据科目类型查询
     */
    List<FiGlAccount> findByAccountTypeAndTenantIdAndIsDeletedFalseOrderByAccountCode(
            String accountType, Long tenantId);

    /**
     * 根据父ID查询子科目
     */
    List<FiGlAccount> findByParentIdAndTenantIdAndIsDeletedFalseOrderByAccountCode(Long parentId, Long tenantId);

    /**
     * 查询顶级科目
     */
    List<FiGlAccount> findByParentIdIsNullAndTenantIdAndIsDeletedFalseOrderByAccountCode(Long tenantId);

    /**
     * 查询叶子科目
     */
    List<FiGlAccount> findByIsLeafTrueAndTenantIdAndIsDeletedFalseOrderByAccountCode(Long tenantId);

    /**
     * 查询可记账科目
     */
    List<FiGlAccount> findByIsPostableTrueAndTenantIdAndIsDeletedFalseOrderByAccountCode(Long tenantId);

    /**
     * 查询统驭科目
     */
    List<FiGlAccount> findByIsReconciliationTrueAndTenantIdAndIsDeletedFalseOrderByAccountCode(Long tenantId);

    /**
     * 查询现金科目
     */
    List<FiGlAccount> findByIsCashTrueAndTenantIdAndIsDeletedFalseOrderByAccountCode(Long tenantId);

    /**
     * 查询银行科目
     */
    List<FiGlAccount> findByIsBankTrueAndTenantIdAndIsDeletedFalseOrderByAccountCode(Long tenantId);

    /**
     * 查询当前有效的科目
     */
    @Query("SELECT a FROM FiGlAccount a WHERE a.tenantId = :tenantId " +
           "AND a.isDeleted = false AND a.status = 1 AND a.isPostable = true " +
           "AND a.validFrom <= CURRENT_DATE AND a.validTo >= CURRENT_DATE " +
           "ORDER BY a.accountCode")
    List<FiGlAccount> findValidPostableAccountsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据科目分类查询
     */
    List<FiGlAccount> findByAccountClassAndTenantIdAndIsDeletedFalseOrderByAccountCode(
            String accountClass, Long tenantId);

    /**
     * 查询资产负债表科目
     */
    List<FiGlAccount> findByIsBalanceSheetTrueAndTenantIdAndIsDeletedFalseOrderByAccountCode(Long tenantId);

    /**
     * 查询损益科目
     */
    List<FiGlAccount> findByIsProfitAndLossTrueAndTenantIdAndIsDeletedFalseOrderByAccountCode(Long tenantId);
}
