package com.nexterp.business.finance.listener;

import com.nexterp.business.finance.application.service.FiJournalEntryService;
import com.nexterp.business.finance.domain.model.FiJournalEntryHdr;
import com.nexterp.business.finance.domain.model.FiJournalEntryItm;
import com.nexterp.business.finance.domain.repository.FiJournalEntryHdrRepository;
import com.nexterp.business.finance.domain.repository.FiJournalEntryItmRepository;
import com.nexterp.business.sales.event.GoodsIssuePostedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FiSdGoodsIssueEventListener {

    private final FiJournalEntryService journalEntryService;
    private final FiJournalEntryHdrRepository hdrRepository;
    private final FiJournalEntryItmRepository itmRepository;

    @ApplicationModuleListener
    @Transactional(rollbackFor = Exception.class)
    public void handleGoodsIssuePosted(GoodsIssuePostedEvent event) {
        log.info("收到发货过账事件: deliveryId={}, deliveryNumber={}, tenantId={}, giDate={}",
                event.deliveryId(), event.deliveryNumber(), event.tenantId(), event.actualGiDate());

        int fiscalYear = event.actualGiDate().getYear();

        FiJournalEntryHdr hdr = FiJournalEntryHdr.builder()
                .tenantId(event.tenantId())
                .companyCode("1000")
                .documentNumber(generateDocNumber(fiscalYear, event.tenantId()))
                .documentTypeCode("WE")
                .documentDate(event.actualGiDate())
                .postingDate(event.actualGiDate())
                .fiscalPeriod(event.actualGiDate().getMonthValue())
                .currencyCode("CNY")
                .sourceType("SD")
                .sourceDocument(event.deliveryNumber())
                .headerText("SD发货自动生成 - " + event.deliveryNumber())
                .docStatus("DRAFT")
                .createdAt(LocalDateTime.now())
                .build();

        Long hdrId = journalEntryService.createJournalEntry(hdr);

        List<FiJournalEntryItm> items = List.of(
                FiJournalEntryItm.builder()
                        .fiscalYear(fiscalYear)
                        .tenantId(event.tenantId())
                        .headerId(hdrId)
                        .lineItem(1)
                        .accountId(6401L)
                        .accountCode("6401")
                        .accountName("主营业务成本")
                        .debitCredit("D")
                        .amount(java.math.BigDecimal.ZERO)
                        .currencyCode("CNY")
                        .itemText("SD发货 - COGS " + event.deliveryNumber())
                        .createdAt(LocalDateTime.now())
                        .build(),
                FiJournalEntryItm.builder()
                        .fiscalYear(fiscalYear)
                        .tenantId(event.tenantId())
                        .headerId(hdrId)
                        .lineItem(2)
                        .accountId(1405L)
                        .accountCode("1405")
                        .accountName("库存商品")
                        .debitCredit("C")
                        .amount(java.math.BigDecimal.ZERO)
                        .currencyCode("CNY")
                        .itemText("SD发货 - 库存减少 " + event.deliveryNumber())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        itmRepository.saveAll(items);
        log.info("SD发货凭证创建成功(待成本补充): fiscalYear={}, docNumber={}, deliveryNumber={}",
                fiscalYear, hdr.getDocumentNumber(), event.deliveryNumber());
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
