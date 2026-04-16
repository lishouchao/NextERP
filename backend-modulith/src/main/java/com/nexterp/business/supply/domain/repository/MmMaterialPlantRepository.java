package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmMaterialPlant;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MmMaterialPlantRepository extends TenantAwareRepository<MmMaterialPlant> {
    Optional<MmMaterialPlant> findByMaterialIdAndPlantIdAndIsDeletedFalse(Long materialId, Long plantId);
    List<MmMaterialPlant> findByMaterialIdAndIsDeletedFalse(Long materialId);
    List<MmMaterialPlant> findByPlantIdAndIsDeletedFalse(Long plantId);
    boolean existsByMaterialIdAndPlantIdAndIsDeletedFalse(Long materialId, Long plantId);
}
