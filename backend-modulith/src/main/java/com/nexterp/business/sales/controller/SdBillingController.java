package com.nexterp.business.sales.controller;

import com.nexterp.business.sales.application.service.SdBillingService;
import com.nexterp.business.sales.dto.BillingDTO;
import com.nexterp.business.sales.dto.CreateBillingRequest;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 开票控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/sd/billings")
@RequiredArgsConstructor
public class SdBillingController {

    private final SdBillingService billingService;

    /**
     * 分页查询开票单
     *
     * @param tenantId      租户ID
     * @param billingStatus 开票状态
     * @param current       当前页
     * @param size          每页大小
     * @return 分页结果
     */
    @GetMapping
    @PreAuthorize("hasAuthority('sd:billing:view')")
    public Result<PageResult<BillingDTO>> listBillings(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String billingStatus,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        log.info("查询开票单列表, tenantId={}, billingStatus={}, current={}, size={}", tenantId, billingStatus, current, size);
        return Result.success(billingService.listBillings(tenantId, billingStatus, current, size));
    }

    /**
     * 创建开票单
     *
     * @param request 创建开票单请求
     * @return 开票单ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sd:billing:add')")
    public Result<Long> createBilling(@Valid @RequestBody CreateBillingRequest request) {
        log.info("创建开票单");
        return Result.success(billingService.createBilling(request));
    }

    /**
     * 获取开票单详情
     *
     * @param id 开票单ID
     * @return 开票单
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sd:billing:view')")
    public Result<BillingDTO> getBillingById(@PathVariable Long id) {
        log.info("获取开票单详情, id={}", id);
        return Result.success(billingService.getBillingById(id));
    }

    /**
     * 更新开票单
     *
     * @param id      开票单ID
     * @param request 创建开票单请求
     * @return 更新后的开票单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sd:billing:edit')")
    public Result<Void> updateBilling(
            @PathVariable Long id,
            @Valid @RequestBody CreateBillingRequest request) {
        log.info("更新开票单, id={}", id);
        billingService.updateBilling(id, request);
        return Result.success();
    }

    /**
     * 开票单过账
     *
     * @param id 开票单ID
     * @return 成功
     */
    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('sd:billing:edit')")
    public Result<Void> postBilling(@PathVariable Long id) {
        log.info("开票单过账, id={}", id);
        billingService.postBilling(id);
        return Result.success();
    }

    /**
     * 取消开票单
     *
     * @param id 开票单ID
     * @return 成功
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('sd:billing:edit')")
    public Result<Void> cancelBilling(@PathVariable Long id) {
        log.info("取消开票单, id={}", id);
        billingService.cancelBilling(id);
        return Result.success();
    }

    /**
     * 开票预览
     *
     * @param deliveryId 交货单ID
     * @return 开票预览结果
     */
    @PostMapping("/preview")
    @PreAuthorize("hasAuthority('sd:billing:view')")
    public Result<BillingDTO> previewBilling(@RequestParam Long deliveryId) {
        log.info("开票预览, deliveryId={}", deliveryId);
        return Result.success(billingService.previewBilling(deliveryId));
    }
}
