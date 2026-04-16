package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmInvoiceItm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MmInvoiceItmRepository extends JpaRepository<MmInvoiceItm, Long> {
    List<MmInvoiceItm> findByInvoiceHdrIdOrderByLineItemAsc(Long invoiceHdrId);
}
