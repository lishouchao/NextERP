package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmMaterialDocHdr;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * 物料凭证头仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmMaterialDocHdrRepository extends TenantAwareRepository<MmMaterialDocHdr> {

    /**
     * 分页查询物料凭证 (按租户)
     *
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 物料凭证分页
     */
    Page<MmMaterialDocHdr> findByTenantIdAndIsDeletedFalse(Long tenantId, Pageable pageable);
}
