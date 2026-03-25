package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FiDocumentType;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 凭证类型 Repository
 *
 * @author NextERP
 */
@Repository
public interface FiDocumentTypeRepository extends TenantAwareRepository<FiDocumentType> {

    /**
     * 根据代码查找
     */
    Optional<FiDocumentType> findByDocTypeCodeAndTenantIdAndIsDeletedFalse(String docTypeCode, Long tenantId);

    /**
     * 检查代码是否存在
     */
    boolean existsByDocTypeCodeAndTenantIdAndIsDeletedFalse(String docTypeCode, Long tenantId);

    /**
     * 根据分类查询
     */
    List<FiDocumentType> findByDocTypeClassAndTenantIdAndIsDeletedFalseOrderBySortOrder(
            String docTypeClass, Long tenantId);

    /**
     * 查询所有启用的凭证类型
     */
    List<FiDocumentType> findByStatusAndTenantIdAndIsDeletedFalseOrderBySortOrder(Integer status, Long tenantId);

    /**
     * 查询可冲销的凭证类型
     */
    List<FiDocumentType> findByIsReversibleTrueAndTenantIdAndIsDeletedFalseOrderBySortOrder(Long tenantId);

    /**
     * 根据编号范围查询
     */
    List<FiDocumentType> findByNumberRangeCodeAndTenantIdAndIsDeletedFalseOrderBySortOrder(
            String numberRangeCode, Long tenantId);
}
