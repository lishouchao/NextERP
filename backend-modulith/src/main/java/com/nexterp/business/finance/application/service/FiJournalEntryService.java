package com.nexterp.business.finance.application.service;

import com.nexterp.business.finance.domain.model.FiJournalEntryHdr;
import com.nexterp.business.finance.domain.model.JournalEntryId;
import com.nexterp.business.finance.domain.repository.FiJournalEntryHdrRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 凭证服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FiJournalEntryService {

    private final FiJournalEntryHdrRepository hdrRepository;

    /**
     * 创建凭证
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createJournalEntry(FiJournalEntryHdr hdr) {
        Integer fiscalYear = hdr.getFiscalYear();
        if (fiscalYear == null) {
            fiscalYear = hdr.getPostingDate() != null ? hdr.getPostingDate().getYear() : LocalDate.now().getYear();
            hdr.setFiscalYear(fiscalYear);
        }

        // 检查凭证号是否已存在
        if (hdr.getDocumentNumber() != null &&
            hdrRepository.existsByFiscalYearAndDocumentNumberAndTenantIdAndIsDeletedFalse(
                    fiscalYear, hdr.getDocumentNumber(), hdr.getTenantId())) {
            throw new BusinessException("凭证号已存在: " + hdr.getDocumentNumber());
        }

        // 设置默认值
        if (hdr.getDocStatus() == null) {
            hdr.setDocStatus("DRAFT");
        }
        if (hdr.getIsPosted() == null) {
            hdr.setIsPosted(false);
        }
        if (hdr.getIsReversed() == null) {
            hdr.setIsReversed(false);
        }
        if (hdr.getCreatedAt() == null) {
            hdr.setCreatedAt(LocalDateTime.now());
        }

        FiJournalEntryHdr saved = hdrRepository.save(hdr);
        log.info("创建凭证成功: fiscalYear={}, docNo={}", fiscalYear, saved.getDocumentNumber());
        return saved.getId();
    }

    /**
     * 过账
     */
    @Transactional(rollbackFor = Exception.class)
    public FiJournalEntryHdr postJournalEntry(Integer fiscalYear, Long id, String postedBy) {
        FiJournalEntryHdr hdr = hdrRepository.findByFiscalYearAndIdAndIsDeletedFalse(fiscalYear, id)
                .orElseThrow(() -> new BusinessException("凭证不存在"));

        if (!"DRAFT".equals(hdr.getDocStatus())) {
            throw new BusinessException("只有草稿状态的凭证可以过账");
        }

        // 检查借贷平衡
        if (hdr.getTotalDebit() == null || hdr.getTotalCredit() == null ||
            hdr.getTotalDebit().compareTo(hdr.getTotalCredit()) != 0) {
            throw new BusinessException("凭证借贷不平衡，无法过账");
        }

        hdr.setDocStatus("POSTED");
        hdr.setIsPosted(true);
        hdr.setPostedBy(postedBy);
        hdr.setPostedAt(LocalDateTime.now());

        return hdrRepository.save(hdr);
    }

    /**
     * 获取凭证详情
     */
    public FiJournalEntryHdr getJournalEntry(Integer fiscalYear, Long id) {
        return hdrRepository.findByFiscalYearAndIdAndIsDeletedFalse(fiscalYear, id)
                .orElseThrow(() -> new BusinessException("凭证不存在"));
    }

    /**
     * 根据凭证号获取
     */
    public FiJournalEntryHdr getJournalEntryByDocNumber(Integer fiscalYear, String documentNumber, Long tenantId) {
        return hdrRepository.findByFiscalYearAndDocumentNumberAndTenantIdAndIsDeletedFalse(fiscalYear, documentNumber, tenantId)
                .orElseThrow(() -> new BusinessException("凭证不存在: " + documentNumber));
    }

    /**
     * 分页查询
     */
    public Page<FiJournalEntryHdr> listJournalEntries(Long tenantId, Integer fiscalYear, Pageable pageable) {
                return hdrRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("fiscalYear"), fiscalYear),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }
}
