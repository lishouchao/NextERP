package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FiChartOfAccounts;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 科目表 Repository
 *
 * @author NextERP
 */
@Repository
public interface FiChartOfAccountsRepository extends TenantAwareRepository<FiChartOfAccounts> {

    /**
     * 根据代码查找
     */
    Optional<FiChartOfAccounts> findByCoaCodeAndTenantIdAndIsDeletedFalse(String coaCode, Long tenantId);

    /**
     * 检查代码是否存在
     */
    boolean existsByCoaCodeAndTenantIdAndIsDeletedFalse(String coaCode, Long tenantId);

    /**
     * 根据类型查询
     */
    List<FiChartOfAccounts> findByCoaTypeAndTenantIdAndIsDeletedFalseOrderByCoaCodeAsc(String coaType, Long tenantId);

    /**
     * 查询集团科目表
     */
    List<FiChartOfAccounts> findByIsGroupCoaTrueAndTenantIdAndIsDeletedFalseOrderByCoaCodeAsc(Long tenantId);

    /**
     * 查询所有启用的科目表
     */
    List<FiChartOfAccounts> findByStatusAndTenantIdAndIsDeletedFalseOrderByCoaCodeAsc(Integer status, Long tenantId);

    /**
     * 查询当前有效的科目表
     */
    @Query("SELECT c FROM FiChartOfAccounts c WHERE c.tenantId = :tenantId " +
           "AND c.isDeleted = false AND c.status = 1 " +
           "AND c.validFrom <= CURRENT_DATE AND c.validTo >= CURRENT_DATE " +
           "ORDER BY c.coaCode")
    List<FiChartOfAccounts> findValidCoaByTenantId(@Param("tenantId") Long tenantId);
}
