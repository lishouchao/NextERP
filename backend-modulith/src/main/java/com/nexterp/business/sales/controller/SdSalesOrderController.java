package com.nexterp.business.sales.controller;

import com.nexterp.business.sales.application.service.SdSalesOrderService;
import com.nexterp.business.sales.dto.CreateSalesOrderRequest;
import com.nexterp.business.sales.dto.CreditCheckResult;
import com.nexterp.business.sales.dto.SalesOrderDTO;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 销售订单控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/sd/orders")
@RequiredArgsConstructor
public class SdSalesOrderController {

    private final SdSalesOrderService salesOrderService;

    /**
     * 分页查询销售订单
     *
     * @param tenantId    租户ID
     * @param orderStatus 订单状态
     * @param current     当前页
     * @param size        每页大小
     * @return 分页结果
     */
    @GetMapping
    @PreAuthorize("hasAuthority('sd:order:view')")
    public Result<PageResult<SalesOrderDTO>> listOrders(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        log.info("查询销售订单列表, tenantId={}, orderStatus={}, current={}, size={}", tenantId, orderStatus, current, size);
        return Result.success(salesOrderService.listOrders(tenantId, orderStatus, current, size));
    }

    /**
     * 创建销售订单
     *
     * @param request 创建销售订单请求
     * @return 订单ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sd:order:add')")
    public Result<Long> createOrder(@Valid @RequestBody CreateSalesOrderRequest request) {
        log.info("创建销售订单");
        return Result.success(salesOrderService.createOrder(request));
    }

    /**
     * 获取销售订单详情
     *
     * @param id 订单ID
     * @return 销售订单
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sd:order:view')")
    public Result<SalesOrderDTO> getOrderById(@PathVariable Long id) {
        log.info("获取销售订单详情, id={}", id);
        return Result.success(salesOrderService.getOrderById(id));
    }

    /**
     * 更新销售订单
     *
     * @param id      订单ID
     * @param request 创建销售订单请求
     * @return 更新后的销售订单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sd:order:edit')")
    public Result<Void> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody CreateSalesOrderRequest request) {
        log.info("更新销售订单, id={}", id);
        salesOrderService.updateOrder(id, request);
        return Result.success();
    }

    /**
     * 删除销售订单
     *
     * @param id 订单ID
     * @return 成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sd:order:delete')")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        log.info("删除销售订单, id={}", id);
        salesOrderService.deleteOrder(id);
        return Result.success();
    }

    /**
     * 提交销售订单
     *
     * @param id 订单ID
     * @return 成功
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('sd:order:edit')")
    public Result<Void> submitOrder(@PathVariable Long id) {
        log.info("提交销售订单, id={}", id);
        salesOrderService.submitOrder(id);
        return Result.success();
    }

    /**
     * 审批销售订单
     *
     * @param id         订单ID
     * @param approvedBy 审批人
     * @return 成功
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('sd:order:approve')")
    public Result<Void> approveOrder(
            @PathVariable Long id,
            @RequestParam String approvedBy) {
        log.info("审批销售订单, id={}, approvedBy={}", id, approvedBy);
        salesOrderService.approveOrder(id, approvedBy);
        return Result.success();
    }

    /**
     * 拒绝销售订单
     *
     * @param id         订单ID
     * @param rejectedBy 拒绝人
     * @param reason     拒绝原因
     * @return 成功
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('sd:order:approve')")
    public Result<Void> rejectOrder(
            @PathVariable Long id,
            @RequestParam String rejectedBy,
            @RequestParam String reason) {
        log.info("拒绝销售订单, id={}, rejectedBy={}, reason={}", id, rejectedBy, reason);
        salesOrderService.rejectOrder(id, rejectedBy, reason);
        return Result.success();
    }

    /**
     * 销售订单信用检查
     *
     * @param id 订单ID
     * @return 信用检查结果
     */
    @PostMapping("/{id}/credit-check")
    @PreAuthorize("hasAuthority('sd:order:view')")
    public Result<CreditCheckResult> creditCheck(@PathVariable Long id) {
        log.info("销售订单信用检查, id={}", id);
        return Result.success(salesOrderService.creditCheck(id));
    }

    /**
     * 销售订单可用性检查
     *
     * @param id 订单ID
     * @return 可用性检查结果
     */
    @GetMapping("/{id}/availability")
    @PreAuthorize("hasAuthority('sd:order:view')")
    public Result<Map<String, Object>> availabilityCheck(@PathVariable Long id) {
        log.info("销售订单可用性检查, id={}", id);
        return Result.success(salesOrderService.availabilityCheck(id));
    }
}
