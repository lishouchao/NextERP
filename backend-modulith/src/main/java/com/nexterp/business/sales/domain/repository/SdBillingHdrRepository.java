package com.nexterp.business.sales.domain.repository;

import com.nexterp.business.sales.domain.model.SdBillingHdr;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 开票凭证头仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SdBillingHdrRepository extends TenantAwareRepository<SdBillingHdr> {

    /**
     * 根据开票号查询
     *
     * @param billingNumber 开票号
     * @param tenantId      租户ID
     * @return 开票凭证头
     */
    Optional<SdBillingHdr> findByBillingNumberAndTenantId(String billingNumber, Long tenantId);

    /**
     * 根据租户ID和开票状态分页查询
     *
     * @param tenantId      租户ID
     * @param billingStatus 开票状态
     * @param pageable      分页
     * @return 开票凭证头分页
     */
    Page<SdBillingHdr> findByTenantIdAndBillingStatusAndIsDeletedFalse(Long tenantId, String billingStatus, Pageable pageable);

    /**
     * 根据来源交货ID查询未删除的开票凭证
     *
     * @param deliveryId 交货ID
     * @return 开票凭证头列表
     */
    List<SdBillingHdr> findByDeliveryIdAndIsDeletedFalse(Long deliveryId);

    /**
     * 根据租户ID分页查询未删除的开票凭证
     *
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 开票凭证头分页
     */
    Page<SdBillingHdr> findByTenantIdAndIsDeletedFalse(Long tenantId, Pageable pageable);
}
