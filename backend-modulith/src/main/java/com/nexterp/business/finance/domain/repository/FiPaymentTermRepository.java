package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FiPaymentTerm;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 付款条件 Repository
 *
 * @author NextERP
 */
@Repository
public interface FiPaymentTermRepository extends TenantAwareRepository<FiPaymentTerm> {

    /**
     * 根据代码查找
     */
    Optional<FiPaymentTerm> findByPaymentTermCodeAndTenantIdAndIsDeletedFalse(String paymentTermCode, Long tenantId);

    /**
     * 检查代码是否存在
     */
    boolean existsByPaymentTermCodeAndTenantIdAndIsDeletedFalse(String paymentTermCode, Long tenantId);

    /**
     * 根据类型查询
     */
    List<FiPaymentTerm> findByTermTypeAndTenantIdAndIsDeletedFalseOrderBySortOrder(String termType, Long tenantId);

    /**
     * 根据适用范围查询
     */
    List<FiPaymentTerm> findByApplyScopeAndTenantIdAndIsDeletedFalseOrderBySortOrder(String applyScope, Long tenantId);

    /**
     * 查询默认付款条件
     */
    Optional<FiPaymentTerm> findByIsDefaultTrueAndTenantIdAndIsDeletedFalse(Long tenantId);

    /**
     * 查询所有启用的付款条件
     */
    List<FiPaymentTerm> findByStatusAndTenantIdAndIsDeletedFalseOrderBySortOrder(Integer status, Long tenantId);
}
