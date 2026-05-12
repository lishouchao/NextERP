package com.nexterp.business.finance.listener;

import com.nexterp.business.finance.application.service.FiJournalEntryService;
import com.nexterp.business.finance.domain.model.FiJournalEntryHdr;
import com.nexterp.business.finance.domain.model.FiJournalEntryItm;
import com.nexterp.business.finance.domain.repository.FiJournalEntryHdrRepository;
import com.nexterp.business.finance.domain.repository.FiJournalEntryItmRepository;
import com.nexterp.business.supply.event.InvoiceVerifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FiMmInvoiceEventListener {

    private final FiJournalEntryService journalEntryService;
    private final FiJournalEntryHdrRepository hdrRepository;
    private final FiJournalEntryItmRepository itmRepository;

    @ApplicationModuleListener
    @Transactional(rollbackFor = Exception.class)
    public void handleInvoiceVerified(InvoiceVerifiedEvent event) {
        log.info("收到发票校验事件: invoiceId={}, invoiceNumber={}, tenantId={}, vendorId={}, grossAmount={}",
                event.invoiceId(), event.invoiceNumber(), event.tenantId(),
                event.vendorId(), event.grossAmount());

        LocalDate now = LocalDate.now();
        int fiscalYear = now.getYear();

        FiJournalEntryHdr hdr = FiJournalEntryHdr.builder()
                .tenantId(event.tenantId())
                .companyCode("1000")
                .documentNumber(generateDocNumber(fiscalYear, event.tenantId()))
                .documentTypeCode("RE")
                .documentDate(now)
                .postingDate(now)
                .fiscalPeriod(now.getMonthValue())
                .currencyCode("CNY")
                .sourceType("MM")
                .sourceDocument(event.invoiceNumber())
                .headerText("MM发票校验自动生成 - " + event.invoiceNumber())
                .docStatus("POSTED")
                .isPosted(true)
                .postedBy("SYSTEM")
                .postedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .totalDebit(event.grossAmount())
                .totalCredit(event.grossAmount())
                .build();

        Long hdrId = journalEntryService.createJournalEntry(hdr);

        List<FiJournalEntryItm> items = List.of(
                FiJournalEntryItm.builder()
                        .fiscalYear(fiscalYear)
                        .tenantId(event.tenantId())
                        .headerId(hdrId)
                        .lineItem(1)
                        .accountId(1402L)
                        .accountCode("1402")
                        .accountName("材料采购/GR-IR清算")
                        .debitCredit("D")
                        .amount(event.grossAmount())
                        .currencyCode("CNY")
                        .itemText("MM发票校验 - 采购 " + event.invoiceNumber())
                        .createdAt(LocalDateTime.now())
                        .build(),
                FiJournalEntryItm.builder()
                        .fiscalYear(fiscalYear)
                        .tenantId(event.tenantId())
                        .headerId(hdrId)
                        .lineItem(2)
                        .accountId(2202L)
                        .accountCode("2202")
                        .accountName("应付账款")
                        .debitCredit("C")
                        .amount(event.grossAmount())
                        .currencyCode("CNY")
                        .partnerId(event.vendorId())
                        .partnerType("V")
                        .itemText("MM发票校验 - 应付 " + event.invoiceNumber())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        itmRepository.saveAll(items);
        log.info("MM发票校验凭证创建成功: fiscalYear={}, docNumber={}, invoiceNumber={}",
                fiscalYear, hdr.getDocumentNumber(), event.invoiceNumber());
    }

    private String generateDocNumber(int fiscalYear, Long tenantId) {
        String maxDoc = hdrRepository.findMaxDocumentNumber(fiscalYear, tenantId);
        if (maxDoc == null) {
            return "1000000001";
        }
        long next = Long.parseLong(maxDoc) + 1;
        return String.format("%010d", next);
    }
}
