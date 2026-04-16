package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmTransferOrderItm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MmTransferOrderItmRepository extends JpaRepository<MmTransferOrderItm, Long> {
    List<MmTransferOrderItm> findByToHdrIdOrderByToItemAsc(Long toHdrId);
}
