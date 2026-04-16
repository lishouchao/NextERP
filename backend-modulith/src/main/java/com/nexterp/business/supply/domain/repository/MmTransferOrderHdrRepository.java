package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmTransferOrderHdr;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

/**
 * 转运订单头仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmTransferOrderHdrRepository extends TenantAwareRepository<MmTransferOrderHdr> {
}
