package com.nexterp.business.sales.domain.repository;

import com.nexterp.business.sales.domain.model.SdCreditMaster;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户信用主数据仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SdCreditMasterRepository extends TenantAwareRepository<SdCreditMaster> {

    /**
     * 根据客户ID和公司代码查询信用主数据
     *
     * @param customerId 客户ID
     * @param companyId  公司代码
     * @return 信用主数据
     */
    Optional<SdCreditMaster> findByCustomerIdAndCompanyIdAndIsDeletedFalse(Long customerId, Long companyId);

    /**
     * 根据租户ID和信用状态查询信用主数据
     *
     * @param tenantId     租户ID
     * @param creditStatus 信用状态
     * @return 信用主数据列表
     */
    List<SdCreditMaster> findByTenantIdAndCreditStatusAndIsDeletedFalse(Long tenantId, String creditStatus);
}
