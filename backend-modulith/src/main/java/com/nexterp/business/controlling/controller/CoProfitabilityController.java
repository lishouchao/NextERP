package com.nexterp.business.controlling.controller;

import com.nexterp.business.controlling.application.service.CoProfitabilityService;
import com.nexterp.business.controlling.domain.model.CoProfitabilitySegment;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * CO-PA 盈利分析控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/controlling/profitability")
@RequiredArgsConstructor
public class CoProfitabilityController {

    private final CoProfitabilityService profitabilityService;

    /**
     * 创建盈利段
     */
    @PostMapping("/segments")
    @PreAuthorize("hasAuthority('controlling:pa:add')")
    public Result<Long> createSegment(@Valid @RequestBody CoProfitabilitySegment segment) {
        Long id = profitabilityService.createSegment(segment);
        return Result.success(id);
    }

    /**
     * 批量创建盈利段
     */
    @PostMapping("/segments/batch")
    @PreAuthorize("hasAuthority('controlling:pa:add')")
    public Result<Void> createSegmentsBatch(@Valid @RequestBody List<CoProfitabilitySegment> segments) {
        profitabilityService.createSegmentsBatch(segments);
        return Result.success();
    }

    /**
     * 更新盈利段
     */
    @PutMapping("/segments/{id}")
    @PreAuthorize("hasAuthority('controlling:pa:edit')")
    public Result<CoProfitabilitySegment> updateSegment(
            @PathVariable Long id,
            @Valid @RequestBody CoProfitabilitySegment segment) {
        CoProfitabilitySegment updated = profitabilityService.updateSegment(id, segment);
        return Result.success(updated);
    }

    /**
     * 删除盈利段
     */
    @DeleteMapping("/segments/{id}")
    @PreAuthorize("hasAuthority('controlling:pa:delete')")
    public Result<Void> deleteSegment(@PathVariable Long id) {
        profitabilityService.deleteSegment(id);
        return Result.success();
    }

    /**
     * 获取盈利段详情
     */
    @GetMapping("/segments/{id}")
    @PreAuthorize("hasAuthority('controlling:pa:view')")
    public Result<CoProfitabilitySegment> getSegmentById(@PathVariable Long id) {
        CoProfitabilitySegment segment = profitabilityService.getSegmentById(id);
        return Result.success(segment);
    }

    /**
     * 按会计年度查询
     */
    @GetMapping("/segments/fiscal-year/{fiscalYear}")
    @PreAuthorize("hasAuthority('controlling:pa:view')")
    public Result<List<CoProfitabilitySegment>> listByFiscalYear(
            @PathVariable String fiscalYear,
            @RequestParam Long tenantId) {
        List<CoProfitabilitySegment> list = profitabilityService.listByFiscalYear(fiscalYear, tenantId);
        return Result.success(list);
    }

    /**
     * 按会计期间查询
     */
    @GetMapping("/segments/fiscal-period")
    @PreAuthorize("hasAuthority('controlling:pa:view')")
    public Result<List<CoProfitabilitySegment>> listByFiscalPeriod(
            @RequestParam String fiscalYear,
            @RequestParam String fiscalPeriod,
            @RequestParam Long tenantId) {
        List<CoProfitabilitySegment> list = profitabilityService.listByFiscalPeriod(fiscalYear, fiscalPeriod, tenantId);
        return Result.success(list);
    }

    /**
     * 按日期范围查询
     */
    @GetMapping("/segments/date-range")
    @PreAuthorize("hasAuthority('controlling:pa:view')")
    public Result<List<CoProfitabilitySegment>> listByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Long tenantId) {
        List<CoProfitabilitySegment> list = profitabilityService.listByDateRange(startDate, endDate, tenantId);
        return Result.success(list);
    }

    /**
     * 按利润中心查询
     */
    @GetMapping("/segments/profit-center/{profitCenter}")
    @PreAuthorize("hasAuthority('controlling:pa:view')")
    public Result<List<CoProfitabilitySegment>> listByProfitCenter(
            @PathVariable String profitCenter,
            @RequestParam Long tenantId) {
        List<CoProfitabilitySegment> list = profitabilityService.listByProfitCenter(profitCenter, tenantId);
        return Result.success(list);
    }

    /**
     * 按客户查询
     */
    @GetMapping("/segments/customer/{customerId}")
    @PreAuthorize("hasAuthority('controlling:pa:view')")
    public Result<List<CoProfitabilitySegment>> listByCustomer(
            @PathVariable Long customerId,
            @RequestParam Long tenantId) {
        List<CoProfitabilitySegment> list = profitabilityService.listByCustomer(customerId, tenantId);
        return Result.success(list);
    }

    /**
     * 按物料查询
     */
    @GetMapping("/segments/material/{materialId}")
    @PreAuthorize("hasAuthority('controlling:pa:view')")
    public Result<List<CoProfitabilitySegment>> listByMaterial(
            @PathVariable Long materialId,
            @RequestParam Long tenantId) {
        List<CoProfitabilitySegment> list = profitabilityService.listByMaterial(materialId, tenantId);
        return Result.success(list);
    }

    /**
     * 分页查询盈利段
     */
    @PostMapping("/segments/page")
    @PreAuthorize("hasAuthority('controlling:pa:view')")
    public Result<PageResult<CoProfitabilitySegment>> listSegments(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<CoProfitabilitySegment> page = profitabilityService.listSegments(
                tenantId, PageRequest.of(current - 1, size));

        PageResult<CoProfitabilitySegment> result = PageResult.<CoProfitabilitySegment>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }

    /**
     * 获取期间汇总
     */
    @GetMapping("/summary/period")
    @PreAuthorize("hasAuthority('controlling:pa:view')")
    public Result<CoProfitabilityService.PeriodSummary> getPeriodSummary(
            @RequestParam String fiscalYear,
            @RequestParam String fiscalPeriod,
            @RequestParam Long tenantId) {
        CoProfitabilityService.PeriodSummary summary = profitabilityService.calculatePeriodSummary(fiscalYear, fiscalPeriod, tenantId);
        return Result.success(summary);
    }
}
