package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmMaterialValuation;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MmMaterialValuationRepository extends TenantAwareRepository<MmMaterialValuation> {
    Optional<MmMaterialValuation> findByMaterialIdAndValuationAreaAndIsDeletedFalse(Long materialId, String valuationArea);
}
