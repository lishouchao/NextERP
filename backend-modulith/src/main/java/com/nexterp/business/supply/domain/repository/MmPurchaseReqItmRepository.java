package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmPurchaseReqItm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MmPurchaseReqItmRepository extends JpaRepository<MmPurchaseReqItm, Long> {
    List<MmPurchaseReqItm> findByReqHdrIdOrderByPrItemAsc(Long reqHdrId);
}
