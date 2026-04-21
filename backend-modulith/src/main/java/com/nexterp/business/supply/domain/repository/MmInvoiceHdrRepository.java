package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmInvoiceHdr;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * 发票头仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmInvoiceHdrRepository extends TenantAwareRepository<MmInvoiceHdr> {

    /**
     * 分页查询发票 (按租户和状态)
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param pageable 分页
     * @return 发票分页
     */
    Page<MmInvoiceHdr> findByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, String status, Pageable pageable);

    /**
     * 分页查询发票 (按租户)
     *
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 发票分页
     */
    Page<MmInvoiceHdr> findByTenantIdAndIsDeletedFalse(Long tenantId, Pageable pageable);
}
