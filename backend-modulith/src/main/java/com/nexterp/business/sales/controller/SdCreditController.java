package com.nexterp.business.sales.controller;

import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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

    /**
     * 获取客户信用主数据
     *
     * @param customerId 客户ID
     * @param companyId  公司ID
     * @return 信用主数据
     */
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('sd:credit:view')")
    public Result<Map<String, Object>> getCreditMaster(
            @PathVariable Long customerId,
            @RequestParam Long companyId) {
        log.info("获取客户信用主数据, customerId={}, companyId={}", customerId, companyId);
        // TODO: 调用信用管理服务
        return Result.success(Map.of("customerId", customerId, "companyId", companyId));
    }

    /**
     * 更新客户信用主数据
     *
     * @param customerId 客户ID
     * @param companyId  公司ID
     * @param params     信用主数据参数
     * @return 更新后的信用主数据
     */
    @PutMapping("/{customerId}")
    @PreAuthorize("hasAuthority('sd:credit:edit')")
    public Result<Map<String, Object>> updateCreditMaster(
            @PathVariable Long customerId,
            @RequestParam Long companyId,
            @RequestBody Map<String, Object> params) {
        log.info("更新客户信用主数据, customerId={}, companyId={}", customerId, companyId);
        // TODO: 调用信用管理服务
        return Result.success(Map.of("customerId", customerId, "companyId", companyId));
    }

    /**
     * 执行信用检查
     *
     * @param request 信用检查请求
     * @return 信用检查结果
     */
    @PostMapping("/check")
    @PreAuthorize("hasAuthority('sd:credit:check')")
    public Result<Map<String, Object>> performCreditCheck(@Valid @RequestBody Map<String, Object> request) {
        log.info("执行信用检查");
        // TODO: 调用信用检查服务
        return Result.success(Map.of("creditStatus", "PASSED"));
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
    public Result<PageResult<Map<String, Object>>> getCreditLogs(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        log.info("查询信用检查日志, tenantId={}, customerId={}, current={}, size={}", tenantId, customerId, current, size);
        // TODO: 调用信用管理服务
        PageResult<Map<String, Object>> pageResult = PageResult.<Map<String, Object>>builder()
                .records(Collections.emptyList())
                .total(0L)
                .current(current)
                .size(size)
                .build();
        return Result.success(pageResult);
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
        // TODO: 调用信用管理服务
        return Result.success(List.of());
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
        // TODO: 调用信用管理服务
        return Result.success();
    }
}
