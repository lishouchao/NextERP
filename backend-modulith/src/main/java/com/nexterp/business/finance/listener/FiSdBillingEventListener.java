package com.nexterp.business.finance.listener;

import com.nexterp.business.finance.application.service.FiJournalEntryService;
import com.nexterp.business.finance.domain.model.FiJournalEntryHdr;
import com.nexterp.business.finance.domain.model.FiJournalEntryItm;
import com.nexterp.business.finance.domain.repository.FiJournalEntryHdrRepository;
import com.nexterp.business.finance.domain.repository.FiJournalEntryItmRepository;
import com.nexterp.business.sales.event.BillingPostedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FiSdBillingEventListener {

    private final FiJournalEntryService journalEntryService;
    private final FiJournalEntryHdrRepository hdrRepository;
    private final FiJournalEntryItmRepository itmRepository;

    @ApplicationModuleListener
    @Transactional(rollbackFor = Exception.class)
    public void handleBillingPosted(BillingPostedEvent event) {
        log.info("收到开票过账事件: billingId={}, billingNumber={}, tenantId={}, netValue={}, taxAmount={}",
                event.billingId(), event.billingNumber(), event.tenantId(),
                event.netValue(), event.taxAmount());

        LocalDate now = LocalDate.now();
        BigDecimal grossAmount = event.netValue().add(event.taxAmount());
        int fiscalYear = now.getYear();

        FiJournalEntryHdr hdr = FiJournalEntryHdr.builder()
                .tenantId(event.tenantId())
                .companyCode("1000")
                .documentNumber(generateDocNumber(fiscalYear, event.tenantId()))
                .documentTypeCode("RV")
                .documentDate(now)
                .postingDate(now)
                .fiscalPeriod(now.getMonthValue())
                .currencyCode("CNY")
                .sourceType("SD")
                .sourceDocument(event.billingNumber())
                .headerText("SD开票自动生成 - " + event.billingNumber())
                .docStatus("POSTED")
                .isPosted(true)
                .postedBy("SYSTEM")
                .postedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .totalDebit(grossAmount)
                .totalCredit(grossAmount)
                .build();

        Long hdrId = journalEntryService.createJournalEntry(hdr);

        List<FiJournalEntryItm> items = List.of(
                FiJournalEntryItm.builder()
                        .fiscalYear(fiscalYear)
                        .tenantId(event.tenantId())
                        .headerId(hdrId)
                        .lineItem(1)
                        .accountId(1001L)
                        .accountCode("1122")
                        .accountName("应收账款")
                        .debitCredit("D")
                        .amount(grossAmount)
                        .currencyCode("CNY")
                        .itemText("SD开票 - 应收 " + event.billingNumber())
                        .createdAt(LocalDateTime.now())
                        .build(),
                FiJournalEntryItm.builder()
                        .fiscalYear(fiscalYear)
                        .tenantId(event.tenantId())
                        .headerId(hdrId)
                        .lineItem(2)
                        .accountId(6001L)
                        .accountCode("6001")
                        .accountName("主营业务收入")
                        .debitCredit("C")
                        .amount(event.netValue())
                        .currencyCode("CNY")
                        .itemText("SD开票 - 收入 " + event.billingNumber())
                        .createdAt(LocalDateTime.now())
                        .build(),
                FiJournalEntryItm.builder()
                        .fiscalYear(fiscalYear)
                        .tenantId(event.tenantId())
                        .headerId(hdrId)
                        .lineItem(3)
                        .accountId(2221L)
                        .accountCode("2221")
                        .accountName("应交税费-销项税")
                        .debitCredit("C")
                        .amount(event.taxAmount())
                        .currencyCode("CNY")
                        .itemText("SD开票 - 销项税 " + event.billingNumber())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        itmRepository.saveAll(items);
        log.info("SD开票凭证创建成功: fiscalYear={}, docNumber={}, billingNumber={}",
                fiscalYear, hdr.getDocumentNumber(), event.billingNumber());
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
