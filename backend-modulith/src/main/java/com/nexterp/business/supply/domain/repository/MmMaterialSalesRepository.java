package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmMaterialSales;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MmMaterialSalesRepository extends TenantAwareRepository<MmMaterialSales> {
    Optional<MmMaterialSales> findByMaterialIdAndSalesOrgIdAndDistrChannelAndIsDeletedFalse(Long materialId, Long salesOrgId, String distrChannel);
    List<MmMaterialSales> findByMaterialIdAndIsDeletedFalse(Long materialId);
}
