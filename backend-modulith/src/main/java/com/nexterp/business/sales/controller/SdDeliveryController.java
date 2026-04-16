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
 * 交货控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/sd/deliveries")
@RequiredArgsConstructor
public class SdDeliveryController {

    /**
     * 分页查询交货单
     *
     * @param tenantId      租户ID
     * @param deliveryStatus 交货状态
     * @param current       当前页
     * @param size          每页大小
     * @return 分页结果
     */
    @GetMapping
    @PreAuthorize("hasAuthority('sd:delivery:view')")
    public Result<PageResult<Map<String, Object>>> listDeliveries(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String deliveryStatus,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        log.info("查询交货单列表, tenantId={}, deliveryStatus={}, current={}, size={}", tenantId, deliveryStatus, current, size);
        // TODO: 调用交货服务
        PageResult<Map<String, Object>> pageResult = PageResult.<Map<String, Object>>builder()
                .records(Collections.emptyList())
                .total(0L)
                .current(current)
                .size(size)
                .build();
        return Result.success(pageResult);
    }

    /**
     * 创建交货单
     *
     * @param request 创建交货单请求
     * @return 交货单ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sd:delivery:add')")
    public Result<Long> createDelivery(@Valid @RequestBody Map<String, Object> request) {
        log.info("创建交货单");
        // TODO: 调用交货服务
        return Result.success(1L);
    }

    /**
     * 获取交货单详情
     *
     * @param id 交货单ID
     * @return 交货单
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sd:delivery:view')")
    public Result<Map<String, Object>> getDeliveryById(@PathVariable Long id) {
        log.info("获取交货单详情, id={}", id);
        // TODO: 调用交货服务
        return Result.success(Map.of("id", id));
    }

    /**
     * 更新交货单
     *
     * @param id      交货单ID
     * @param request 创建交货单请求
     * @return 更新后的交货单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sd:delivery:edit')")
    public Result<Map<String, Object>> updateDelivery(
            @PathVariable Long id,
            @Valid @RequestBody Map<String, Object> request) {
        log.info("更新交货单, id={}", id);
        // TODO: 调用交货服务
        return Result.success(Map.of("id", id));
    }

    /**
     * 交货单拣货
     *
     * @param id        交货单ID
     * @param pickItems 拣货明细
     * @return 成功
     */
    @PostMapping("/{id}/pick")
    @PreAuthorize("hasAuthority('sd:delivery:edit')")
    public Result<Void> pickDelivery(
            @PathVariable Long id,
            @RequestBody List<Map<String, Object>> pickItems) {
        log.info("交货单拣货, id={}, 拣货明细数={}", id, pickItems.size());
        // TODO: 调用交货服务
        return Result.success();
    }

    /**
     * 交货单发货过账
     *
     * @param id          交货单ID
     * @param actualGiDate 实际发货日期
     * @return 成功
     */
    @PostMapping("/{id}/post-gi")
    @PreAuthorize("hasAuthority('sd:delivery:edit')")
    public Result<Void> postGoodsIssue(
            @PathVariable Long id,
            @RequestParam String actualGiDate) {
        log.info("交货单发货过账, id={}, actualGiDate={}", id, actualGiDate);
        // TODO: 调用交货服务
        return Result.success();
    }

    /**
     * 取消交货单
     *
     * @param id 交货单ID
     * @return 成功
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('sd:delivery:edit')")
    public Result<Void> cancelDelivery(@PathVariable Long id) {
        log.info("取消交货单, id={}", id);
        // TODO: 调用交货服务
        return Result.success();
    }
}
