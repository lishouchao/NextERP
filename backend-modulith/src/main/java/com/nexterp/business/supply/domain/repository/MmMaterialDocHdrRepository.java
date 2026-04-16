package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmMaterialDocHdr;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

/**
 * 物料凭证头仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmMaterialDocHdrRepository extends TenantAwareRepository<MmMaterialDocHdr> {
}
