package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmInvoiceHdr;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

/**
 * 发票头仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmInvoiceHdrRepository extends TenantAwareRepository<MmInvoiceHdr> {
}
