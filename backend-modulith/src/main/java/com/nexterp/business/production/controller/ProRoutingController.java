package com.nexterp.business.production.controller;

import com.nexterp.business.production.application.service.ProRoutingService;
import com.nexterp.business.production.dto.CreateRoutingRequest;
import com.nexterp.business.production.dto.ProRoutingDTO;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 工艺路线控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/production/routings")
@RequiredArgsConstructor
public class ProRoutingController {

    private final ProRoutingService routingService;

    /**
     * 创建工艺路线
     */
    @PostMapping
    @PreAuthorize("hasAuthority('pp:routing:add')")
    public Result<Long> createRouting(@Valid @RequestBody CreateRoutingRequest request) {
        Long id = routingService.createRouting(request);
        return Result.success(id);
    }

    /**
     * 更新工艺路线
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:routing:edit')")
    public Result<Void> updateRouting(@PathVariable Long id, @Valid @RequestBody CreateRoutingRequest request) {
        routingService.updateRouting(id, request);
        return Result.success();
    }

    /**
     * 删除工艺路线
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:routing:delete')")
    public Result<Void> deleteRouting(@PathVariable Long id) {
        routingService.deleteRouting(id);
        return Result.success();
    }

    /**
     * 根据ID获取工艺路线
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:routing:view')")
    public Result<ProRoutingDTO> getRoutingById(@PathVariable Long id) {
        return Result.success(routingService.getRoutingById(id));
    }

    /**
     * 分页查询工艺路线
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('pp:routing:view')")
    public Result<PageResult<ProRoutingDTO>> listRoutings(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Integer routingType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(routingService.listRoutings(tenantId, routingType, status, current, size));
    }

    /**
     * 启用工艺路线
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('pp:routing:edit')")
    public Result<Void> activateRouting(@PathVariable Long id) {
        routingService.activateRouting(id);
        return Result.success();
    }

    /**
     * 停用工艺路线
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('pp:routing:edit')")
    public Result<Void> deactivateRouting(@PathVariable Long id) {
        routingService.deactivateRouting(id);
        return Result.success();
    }
}
