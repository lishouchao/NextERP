package com.nexterp.business.controlling.controller;

import com.nexterp.business.controlling.application.service.CoInternalOrderService;
import com.nexterp.business.controlling.domain.model.CoInternalOrder;
import com.nexterp.business.controlling.domain.model.CoSettlementRule;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内部订单控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/controlling/internal-orders")
@RequiredArgsConstructor
public class CoInternalOrderController {

    private final CoInternalOrderService internalOrderService;

    /**
     * 创建内部订单
     */
    @PostMapping
    @PreAuthorize("hasAuthority('controlling:order:add')")
    public Result<Long> createInternalOrder(@Valid @RequestBody CoInternalOrder order) {
        Long id = internalOrderService.createInternalOrder(order);
        return Result.success(id);
    }

    /**
     * 更新内部订单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:order:edit')")
    public Result<CoInternalOrder> updateInternalOrder(
            @PathVariable Long id,
            @Valid @RequestBody CoInternalOrder order) {
        CoInternalOrder updated = internalOrderService.updateInternalOrder(id, order);
        return Result.success(updated);
    }

    /**
     * 删除内部订单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:order:delete')")
    public Result<Void> deleteInternalOrder(@PathVariable Long id) {
        internalOrderService.deleteInternalOrder(id);
        return Result.success();
    }

    /**
     * 获取内部订单详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:order:view')")
    public Result<CoInternalOrder> getInternalOrderById(@PathVariable Long id) {
        CoInternalOrder order = internalOrderService.getInternalOrderById(id);
        return Result.success(order);
    }

    /**
     * 根据订单号获取
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasAuthority('controlling:order:view')")
    public Result<CoInternalOrder> getInternalOrderByNumber(
            @PathVariable String orderNumber,
            @RequestParam Long tenantId) {
        CoInternalOrder order = internalOrderService.getInternalOrderByNumber(orderNumber, tenantId);
        return Result.success(order);
    }

    /**
     * 下达订单
     */
    @PostMapping("/{id}/release")
    @PreAuthorize("hasAuthority('controlling:order:edit')")
    public Result<CoInternalOrder> releaseOrder(@PathVariable Long id) {
        CoInternalOrder order = internalOrderService.releaseOrder(id);
        return Result.success(order);
    }

    /**
     * 完成订单
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('controlling:order:edit')")
    public Result<CoInternalOrder> completeOrder(@PathVariable Long id) {
        CoInternalOrder order = internalOrderService.completeOrder(id);
        return Result.success(order);
    }

    /**
     * 关闭订单
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('controlling:order:edit')")
    public Result<CoInternalOrder> closeOrder(@PathVariable Long id) {
        CoInternalOrder order = internalOrderService.closeOrder(id);
        return Result.success(order);
    }

    /**
     * 按类型查询订单
     */
    @GetMapping("/type/{orderType}")
    @PreAuthorize("hasAuthority('controlling:order:view')")
    public Result<List<CoInternalOrder>> listByOrderType(
            @PathVariable String orderType,
            @RequestParam Long tenantId) {
        List<CoInternalOrder> list = internalOrderService.listByOrderType(orderType, tenantId);
        return Result.success(list);
    }

    /**
     * 按状态查询订单
     */
    @GetMapping("/status/{orderStatus}")
    @PreAuthorize("hasAuthority('controlling:order:view')")
    public Result<List<CoInternalOrder>> listByOrderStatus(
            @PathVariable String orderStatus,
            @RequestParam Long tenantId) {
        List<CoInternalOrder> list = internalOrderService.listByOrderStatus(orderStatus, tenantId);
        return Result.success(list);
    }

    /**
     * 分页查询订单
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('controlling:order:view')")
    public Result<PageResult<CoInternalOrder>> listInternalOrders(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<CoInternalOrder> page = internalOrderService.listInternalOrders(
                tenantId, PageRequest.of(current - 1, size));

        PageResult<CoInternalOrder> result = PageResult.<CoInternalOrder>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }

    /**
     * 添加结算规则
     */
    @PostMapping("/{id}/settlement-rules")
    @PreAuthorize("hasAuthority('controlling:order:edit')")
    public Result<Long> addSettlementRule(
            @PathVariable Long id,
            @Valid @RequestBody CoSettlementRule rule) {
        Long ruleId = internalOrderService.addSettlementRule(id, rule);
        return Result.success(ruleId);
    }

    /**
     * 获取结算规则
     */
    @GetMapping("/{id}/settlement-rules")
    @PreAuthorize("hasAuthority('controlling:order:view')")
    public Result<List<CoSettlementRule>> getSettlementRules(
            @PathVariable Long id,
            @RequestParam Long tenantId) {
        List<CoSettlementRule> rules = internalOrderService.getSettlementRules(id, tenantId);
        return Result.success(rules);
    }
}
