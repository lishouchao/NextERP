package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmPurchaseReqHdr;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * 采购申请头仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmPurchaseReqHdrRepository extends TenantAwareRepository<MmPurchaseReqHdr> {

    /**
     * 分页查询采购申请 (按状态)
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param pageable 分页
     * @return 采购申请分页
     */
    Page<MmPurchaseReqHdr> findByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, String status, Pageable pageable);

    /**
     * 分页查询采购申请 (按租户)
     *
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 采购申请分页
     */
    Page<MmPurchaseReqHdr> findByTenantIdAndIsDeletedFalse(Long tenantId, Pageable pageable);
}
