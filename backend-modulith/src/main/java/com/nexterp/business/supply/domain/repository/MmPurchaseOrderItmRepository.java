package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmPurchaseOrderItm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 采购订单项仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmPurchaseOrderItmRepository extends JpaRepository<MmPurchaseOrderItm, Long> {
}
