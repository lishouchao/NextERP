package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmPurchaseOrderHdr;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * 采购订单头仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmPurchaseOrderHdrRepository extends TenantAwareRepository<MmPurchaseOrderHdr> {

    /**
     * 分页查询采购订单 (按状态)
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param pageable 分页
     * @return 采购订单分页
     */
    Page<MmPurchaseOrderHdr> findByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, String status, Pageable pageable);
}
