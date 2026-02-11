package com.nexterp.business.finance.controller;

import com.nexterp.business.finance.application.service.FinAccountingPeriodService;
import com.nexterp.business.finance.domain.model.FinAccountingPeriod;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会计期间控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/finance/periods")
@RequiredArgsConstructor
public class FinAccountingPeriodController {

    private final FinAccountingPeriodService periodService;

    /**
     * 初始化年度期间
     *
     * @param year     会计年度
     * @param tenantId 租户ID
     * @return 成功
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasAuthority('finance:period:initialize')")
    public Result<Void> initializeYearPeriods(
            @RequestParam Integer year,
            @RequestParam Long tenantId) {
        periodService.initializeYearPeriods(year, tenantId);
        return Result.success();
    }

    /**
     * 开启期间
     *
     * @param periodId 期间ID
     * @return 成功
     */
    @PostMapping("/{id}/open")
    @PreAuthorize("hasAuthority('finance:period:open')")
    public Result<Void> openPeriod(@PathVariable Long id) {
        periodService.openPeriod(id);
        return Result.success();
    }

    /**
     * 结账
     *
     * @param periodId 期间ID
     * @return 成功
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('finance:period:close')")
    public Result<Void> closePeriod(@PathVariable Long id) {
        periodService.closePeriod(id);
        return Result.success();
    }

    /**
     * 反结账
     *
     * @param periodId 期间ID
     * @return 成功
     */
    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAuthority('finance:period:reopen')")
    public Result<Void> reopenPeriod(@PathVariable Long id) {
        periodService.reopenPeriod(id);
        return Result.success();
    }

    /**
     * 获取期间详情
     *
     * @param id 期间ID
     * @return 期间
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:period:view')")
    public Result<FinAccountingPeriod> getPeriodById(@PathVariable Long id) {
        FinAccountingPeriod period = periodService.getPeriodById(id);
        return Result.success(period);
    }

    /**
     * 获取当前期间
     *
     * @param tenantId 租户ID
     * @return 当前期间
     */
    @GetMapping("/current")
    @PreAuthorize("hasAuthority('finance:period:view')")
    public Result<FinAccountingPeriod> getCurrentPeriod(@RequestParam Long tenantId) {
        FinAccountingPeriod period = periodService.getCurrentPeriod(tenantId);
        return Result.success(period);
    }

    /**
     * 根据日期获取期间
     *
     * @param date     日期 (格式: yyyy-MM-dd)
     * @param tenantId 租户ID
     * @return 期间
     */
    @GetMapping("/by-date")
    @PreAuthorize("hasAuthority('finance:period:view')")
    public Result<FinAccountingPeriod> getPeriodByDate(
            @RequestParam String date,
            @RequestParam Long tenantId) {
        java.time.LocalDate localDate = java.time.LocalDate.parse(date);
        FinAccountingPeriod period = periodService.getPeriodByDate(localDate, tenantId);
        return Result.success(period);
    }

    /**
     * 获取年度期间列表
     *
     * @param year     会计年度
     * @param tenantId 租户ID
     * @return 期间列表
     */
    @GetMapping("/year/{year}")
    @PreAuthorize("hasAuthority('finance:period:view')")
    public Result<List<FinAccountingPeriod>> listYearPeriods(
            @PathVariable Integer year,
            @RequestParam Long tenantId) {
        List<FinAccountingPeriod> periods = periodService.listYearPeriods(year, tenantId);
        return Result.success(periods);
    }

    /**
     * 获取已开启的期间列表
     *
     * @param tenantId 租户ID
     * @return 期间列表
     */
    @GetMapping("/opened")
    @PreAuthorize("hasAuthority('finance:period:view')")
    public Result<List<FinAccountingPeriod>> listOpenedPeriods(@RequestParam Long tenantId) {
        List<FinAccountingPeriod> periods = periodService.listOpenedPeriods(tenantId);
        return Result.success(periods);
    }

    /**
     * 获取未结账的期间列表
     *
     * @param tenantId 租户ID
     * @return 期间列表
     */
    @GetMapping("/unclosed")
    @PreAuthorize("hasAuthority('finance:period:view')")
    public Result<List<FinAccountingPeriod>> listUnclosedPeriods(@RequestParam Long tenantId) {
        List<FinAccountingPeriod> periods = periodService.listUnclosedPeriods(tenantId);
        return Result.success(periods);
    }
}
