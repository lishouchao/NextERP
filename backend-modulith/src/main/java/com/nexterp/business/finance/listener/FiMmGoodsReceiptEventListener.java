package com.nexterp.business.finance.listener;

import com.nexterp.business.finance.application.service.FiJournalEntryService;
import com.nexterp.business.finance.domain.model.FiJournalEntryHdr;
import com.nexterp.business.finance.domain.model.FiJournalEntryItm;
import com.nexterp.business.finance.domain.repository.FiJournalEntryHdrRepository;
import com.nexterp.business.finance.domain.repository.FiJournalEntryItmRepository;
import com.nexterp.business.supply.event.GoodsReceiptPostedEvent;
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
public class FiMmGoodsReceiptEventListener {

    private final FiJournalEntryService journalEntryService;
    private final FiJournalEntryHdrRepository hdrRepository;
    private final FiJournalEntryItmRepository itmRepository;

    @ApplicationModuleListener
    @Transactional(rollbackFor = Exception.class)
    public void handleGoodsReceiptPosted(GoodsReceiptPostedEvent event) {
        log.info("收到收货过账事件: materialDocId={}, materialDocument={}, movementType={}, tenantId={}, totalAmount={}",
                event.materialDocId(), event.materialDocument(), event.movementType(),
                event.tenantId(), event.totalAmount());

        LocalDate now = LocalDate.now();
        int fiscalYear = now.getYear();

        FiJournalEntryHdr hdr = FiJournalEntryHdr.builder()
                .tenantId(event.tenantId())
                .companyCode("1000")
                .documentNumber(generateDocNumber(fiscalYear, event.tenantId()))
                .documentTypeCode("WE")
                .documentDate(now)
                .postingDate(now)
                .fiscalPeriod(now.getMonthValue())
                .currencyCode("CNY")
                .sourceType("MM")
                .sourceDocument(event.materialDocument())
                .referenceNumber(event.purchaseOrder())
                .headerText("MM收货自动生成 - " + event.materialDocument())
                .docStatus("POSTED")
                .isPosted(true)
                .postedBy("SYSTEM")
                .postedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .totalDebit(event.totalAmount())
                .totalCredit(event.totalAmount())
                .build();

        Long hdrId = journalEntryService.createJournalEntry(hdr);

        List<FiJournalEntryItm> items = List.of(
                FiJournalEntryItm.builder()
                        .fiscalYear(fiscalYear)
                        .tenantId(event.tenantId())
                        .headerId(hdrId)
                        .lineItem(1)
                        .accountId(1401L)
                        .accountCode("1401")
                        .accountName("原材料")
                        .debitCredit("D")
                        .amount(event.totalAmount())
                        .currencyCode("CNY")
                        .itemText("MM收货 - 库存增加 " + event.materialDocument())
                        .createdAt(LocalDateTime.now())
                        .build(),
                FiJournalEntryItm.builder()
                        .fiscalYear(fiscalYear)
                        .tenantId(event.tenantId())
                        .headerId(hdrId)
                        .lineItem(2)
                        .accountId(1402L)
                        .accountCode("1402")
                        .accountName("材料采购/GR-IR清算")
                        .debitCredit("C")
                        .amount(event.totalAmount())
                        .currencyCode("CNY")
                        .itemText("MM收货 - GR/IR " + event.materialDocument())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        itmRepository.saveAll(items);
        log.info("MM收货凭证创建成功: fiscalYear={}, docNumber={}, materialDocument={}",
                fiscalYear, hdr.getDocumentNumber(), event.materialDocument());
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
