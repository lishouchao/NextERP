package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmMaterialDocItm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 物料凭证项仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmMaterialDocItmRepository extends JpaRepository<MmMaterialDocItm, Long> {
}
