package com.nexterp.business.production.controller;

import com.nexterp.business.production.application.service.ProProductionOrderService;
import com.nexterp.business.production.dto.CreateProductionOrderRequest;
import com.nexterp.business.production.dto.ProProductionOrderDTO;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 生产订单控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/production/orders")
@RequiredArgsConstructor
public class ProProductionOrderController {

    private final ProProductionOrderService productionOrderService;

    /**
     * 分页查询生产订单
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('pp:order:view')")
    public Result<PageResult<ProProductionOrderDTO>> listOrders(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long workshopId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(productionOrderService.listOrders(tenantId, status, workshopId, current, size));
    }

    /**
     * 创建生产订单
     */
    @PostMapping
    @PreAuthorize("hasAuthority('pp:order:add')")
    public Result<Long> createOrder(@Valid @RequestBody CreateProductionOrderRequest request) {
        Long id = productionOrderService.createOrder(request);
        return Result.success(id);
    }

    /**
     * 根据ID获取生产订单
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:order:view')")
    public Result<ProProductionOrderDTO> getOrderById(@PathVariable Long id) {
        return Result.success(productionOrderService.getOrderById(id));
    }

    /**
     * 更新生产订单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:order:edit')")
    public Result<Void> updateOrder(@PathVariable Long id, @Valid @RequestBody CreateProductionOrderRequest request) {
        productionOrderService.updateOrder(id, request);
        return Result.success();
    }

    /**
     * 删除生产订单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:order:delete')")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        productionOrderService.deleteOrder(id);
        return Result.success();
    }

    /**
     * 审核生产订单
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('pp:order:approve')")
    public Result<Void> approveOrder(@PathVariable Long id, @RequestParam String approvedBy) {
        productionOrderService.approveOrder(id, approvedBy);
        return Result.success();
    }

    /**
     * 生产订单开工
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('pp:order:edit')")
    public Result<Void> startProduction(@PathVariable Long id) {
        productionOrderService.startProduction(id);
        return Result.success();
    }

    /**
     * 生产订单完工报工
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('pp:order:edit')")
    public Result<Void> completeOrder(@PathVariable Long id,
                                      @RequestParam BigDecimal completedQty,
                                      @RequestParam BigDecimal scrappedQty) {
        productionOrderService.completeOrder(id, completedQty, scrappedQty);
        return Result.success();
    }

    /**
     * 关闭生产订单
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('pp:order:edit')")
    public Result<Void> closeOrder(@PathVariable Long id) {
        productionOrderService.closeOrder(id);
        return Result.success();
    }

    /**
     * 取消生产订单
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('pp:order:edit')")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        productionOrderService.cancelOrder(id);
        return Result.success();
    }
}
