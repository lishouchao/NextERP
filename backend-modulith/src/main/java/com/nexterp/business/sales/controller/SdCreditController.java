package com.nexterp.business.sales.controller;

import com.nexterp.business.sales.application.service.SdCreditService;
import com.nexterp.business.sales.domain.model.SdCreditCheckLog;
import com.nexterp.business.sales.dto.CreditCheckRequest;
import com.nexterp.business.sales.dto.CreditCheckResult;
import com.nexterp.business.sales.dto.CreditMasterDTO;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 信用管理控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/sd/credit")
@RequiredArgsConstructor
public class SdCreditController {

    private final SdCreditService creditService;

    /**
     * 获取客户信用主数据
     *
     * @param customerId 客户ID
     * @param companyId  公司ID
     * @return 信用主数据
     */
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('sd:credit:view')")
    public Result<CreditMasterDTO> getCreditMaster(
            @PathVariable Long customerId,
            @RequestParam Long companyId) {
        log.info("获取客户信用主数据, customerId={}, companyId={}", customerId, companyId);
        return Result.success(creditService.getCreditMaster(customerId, companyId));
    }

    /**
     * 更新客户信用主数据
     *
     * @param customerId  客户ID
     * @param companyId   公司ID
     * @param creditLimit 信用额度
     * @param riskClass   风险类别
     * @return 成功
     */
    @PutMapping("/{customerId}")
    @PreAuthorize("hasAuthority('sd:credit:edit')")
    public Result<Void> updateCreditMaster(
            @PathVariable Long customerId,
            @RequestParam Long companyId,
            @RequestParam(required = false) BigDecimal creditLimit,
            @RequestParam(required = false) String riskClass) {
        log.info("更新客户信用主数据, customerId={}, companyId={}, creditLimit={}, riskClass={}", customerId, companyId, creditLimit, riskClass);
        creditService.updateCreditMaster(customerId, companyId, creditLimit, riskClass);
        return Result.success();
    }

    /**
     * 执行信用检查
     *
     * @param request 信用检查请求
     * @return 信用检查结果
     */
    @PostMapping("/check")
    @PreAuthorize("hasAuthority('sd:credit:check')")
    public Result<CreditCheckResult> performCreditCheck(@Valid @RequestBody CreditCheckRequest request) {
        log.info("执行信用检查");
        return Result.success(creditService.performCreditCheck(request));
    }

    /**
     * 查询信用检查日志
     *
     * @param tenantId   租户ID
     * @param customerId 客户ID
     * @param current    当前页
     * @param size       每页大小
     * @return 分页结果
     */
    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('sd:credit:view')")
    public Result<PageResult<SdCreditCheckLog>> getCreditLogs(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        log.info("查询信用检查日志, tenantId={}, customerId={}, current={}, size={}", tenantId, customerId, current, size);
        return Result.success(creditService.getCreditLogs(tenantId, customerId, current, size));
    }

    /**
     * 查询被冻结的订单
     *
     * @param tenantId 租户ID
     * @return 被冻结的订单列表
     */
    @GetMapping("/blocked-orders")
    @PreAuthorize("hasAuthority('sd:credit:view')")
    public Result<List<Map<String, Object>>> getBlockedOrders(@RequestParam Long tenantId) {
        log.info("查询被冻结的订单, tenantId={}", tenantId);
        return Result.success(creditService.getBlockedOrders(tenantId));
    }

    /**
     * 释放被冻结的订单
     *
     * @param orderId    订单ID
     * @param releasedBy 释放人
     * @return 成功
     */
    @PostMapping("/release/{orderId}")
    @PreAuthorize("hasAuthority('sd:credit:edit')")
    public Result<Void> releaseBlockedOrder(
            @PathVariable Long orderId,
            @RequestParam String releasedBy) {
        log.info("释放被冻结的订单, orderId={}, releasedBy={}", orderId, releasedBy);
        creditService.releaseBlockedOrder(orderId, releasedBy);
        return Result.success();
    }
}
