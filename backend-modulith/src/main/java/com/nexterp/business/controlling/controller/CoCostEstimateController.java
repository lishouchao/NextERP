package com.nexterp.business.controlling.controller;

import com.nexterp.business.controlling.application.service.CoCostEstimateService;
import com.nexterp.business.controlling.domain.model.CoCostComponent;
import com.nexterp.business.controlling.domain.model.CoCostEstimate;
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
 * 成本估算控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/controlling/cost-estimates")
@RequiredArgsConstructor
public class CoCostEstimateController {

    private final CoCostEstimateService costEstimateService;

    /**
     * 创建成本估算
     */
    @PostMapping
    @PreAuthorize("hasAuthority('controlling:estimate:add')")
    public Result<Long> createCostEstimate(@Valid @RequestBody CoCostEstimate estimate) {
        Long id = costEstimateService.createCostEstimate(estimate);
        return Result.success(id);
    }

    /**
     * 更新成本估算
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:estimate:edit')")
    public Result<CoCostEstimate> updateCostEstimate(
            @PathVariable Long id,
            @Valid @RequestBody CoCostEstimate estimate) {
        CoCostEstimate updated = costEstimateService.updateCostEstimate(id, estimate);
        return Result.success(updated);
    }

    /**
     * 删除成本估算
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:estimate:delete')")
    public Result<Void> deleteCostEstimate(@PathVariable Long id) {
        costEstimateService.deleteCostEstimate(id);
        return Result.success();
    }

    /**
     * 获取成本估算详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:estimate:view')")
    public Result<CoCostEstimate> getCostEstimateById(@PathVariable Long id) {
        CoCostEstimate estimate = costEstimateService.getCostEstimateById(id);
        return Result.success(estimate);
    }

    /**
     * 根据估算号获取
     */
    @GetMapping("/number/{estimateNumber}")
    @PreAuthorize("hasAuthority('controlling:estimate:view')")
    public Result<CoCostEstimate> getCostEstimateByNumber(
            @PathVariable String estimateNumber,
            @RequestParam Long tenantId) {
        CoCostEstimate estimate = costEstimateService.getCostEstimateByNumber(estimateNumber, tenantId);
        return Result.success(estimate);
    }

    /**
     * 发布成本估算
     */
    @PostMapping("/{id}/release")
    @PreAuthorize("hasAuthority('controlling:estimate:edit')")
    public Result<CoCostEstimate> releaseCostEstimate(@PathVariable Long id) {
        CoCostEstimate estimate = costEstimateService.releaseCostEstimate(id);
        return Result.success(estimate);
    }

    /**
     * 标记成本估算
     */
    @PostMapping("/{id}/mark")
    @PreAuthorize("hasAuthority('controlling:estimate:edit')")
    public Result<CoCostEstimate> markCostEstimate(@PathVariable Long id) {
        CoCostEstimate estimate = costEstimateService.markCostEstimate(id);
        return Result.success(estimate);
    }

    /**
     * 获取物料的有效成本估算
     */
    @GetMapping("/material/{materialId}/valid")
    @PreAuthorize("hasAuthority('controlling:estimate:view')")
    public Result<CoCostEstimate> getValidCostEstimate(
            @PathVariable Long materialId,
            @RequestParam(defaultValue = "01") String estimateType,
            @RequestParam Long tenantId) {
        CoCostEstimate estimate = costEstimateService.getValidCostEstimate(materialId, estimateType, tenantId);
        return Result.success(estimate);
    }

    /**
     * 按物料查询
     */
    @GetMapping("/material/{materialId}")
    @PreAuthorize("hasAuthority('controlling:estimate:view')")
    public Result<List<CoCostEstimate>> listByMaterial(
            @PathVariable Long materialId,
            @RequestParam Long tenantId) {
        List<CoCostEstimate> list = costEstimateService.listByMaterial(materialId, tenantId);
        return Result.success(list);
    }

    /**
     * 分页查询成本估算
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('controlling:estimate:view')")
    public Result<PageResult<CoCostEstimate>> listCostEstimates(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<CoCostEstimate> page = costEstimateService.listCostEstimates(
                tenantId, PageRequest.of(current - 1, size));

        PageResult<CoCostEstimate> result = PageResult.<CoCostEstimate>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }

    /**
     * 添加成本构成
     */
    @PostMapping("/{id}/components")
    @PreAuthorize("hasAuthority('controlling:estimate:edit')")
    public Result<Long> addCostComponent(
            @PathVariable Long id,
            @Valid @RequestBody CoCostComponent component) {
        Long componentId = costEstimateService.addCostComponent(id, component);
        return Result.success(componentId);
    }

    /**
     * 获取成本构成明细
     */
    @GetMapping("/{id}/components")
    @PreAuthorize("hasAuthority('controlling:estimate:view')")
    public Result<List<CoCostComponent>> getCostComponents(
            @PathVariable Long id,
            @RequestParam Long tenantId) {
        List<CoCostComponent> components = costEstimateService.getCostComponents(id, tenantId);
        return Result.success(components);
    }
}
