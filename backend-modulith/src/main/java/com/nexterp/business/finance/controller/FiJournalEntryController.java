package com.nexterp.business.finance.controller;

import com.nexterp.business.finance.application.service.FiJournalEntryService;
import com.nexterp.business.finance.domain.model.FiJournalEntryHdr;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 凭证控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/finance/journal-entries")
@RequiredArgsConstructor
public class FiJournalEntryController {

    private final FiJournalEntryService journalEntryService;

    /**
     * 创建凭证
     */
    @PostMapping
    @PreAuthorize("hasAuthority('finance:journal:add')")
    public Result<Long> createJournalEntry(@Valid @RequestBody FiJournalEntryHdr hdr) {
        Long id = journalEntryService.createJournalEntry(hdr);
        return Result.success(id);
    }

    /**
     * 过账
     */
    @PostMapping("/{fiscalYear}/{id}/post")
    @PreAuthorize("hasAuthority('finance:journal:post')")
    public Result<FiJournalEntryHdr> postJournalEntry(
            @PathVariable Integer fiscalYear,
            @PathVariable Long id,
            @RequestParam(required = false) String postedBy) {
        FiJournalEntryHdr posted = journalEntryService.postJournalEntry(fiscalYear, id, postedBy);
        return Result.success(posted);
    }

    /**
     * 获取凭证详情
     */
    @GetMapping("/{fiscalYear}/{id}")
    @PreAuthorize("hasAuthority('finance:journal:view')")
    public Result<FiJournalEntryHdr> getJournalEntry(
            @PathVariable Integer fiscalYear,
            @PathVariable Long id) {
        FiJournalEntryHdr hdr = journalEntryService.getJournalEntry(fiscalYear, id);
        return Result.success(hdr);
    }

    /**
     * 根据凭证号获取
     */
    @GetMapping("/by-doc-number/{fiscalYear}/{documentNumber}")
    @PreAuthorize("hasAuthority('finance:journal:view')")
    public Result<FiJournalEntryHdr> getJournalEntryByDocNumber(
            @PathVariable Integer fiscalYear,
            @PathVariable String documentNumber,
            @RequestParam Long tenantId) {
        FiJournalEntryHdr hdr = journalEntryService.getJournalEntryByDocNumber(fiscalYear, documentNumber, tenantId);
        return Result.success(hdr);
    }

    /**
     * 分页查询
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('finance:journal:view')")
    public Result<PageResult<FiJournalEntryHdr>> listJournalEntries(
            @RequestParam Long tenantId,
            @RequestParam Integer fiscalYear,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FiJournalEntryHdr> page = journalEntryService.listJournalEntries(
                tenantId, fiscalYear, PageRequest.of(current - 1, size));

        PageResult<FiJournalEntryHdr> result = PageResult.<FiJournalEntryHdr>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
