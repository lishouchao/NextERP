package com.nexterp.business.controlling.controller;

import com.nexterp.business.controlling.application.service.CoCostElementService;
import com.nexterp.business.controlling.domain.model.CoCostElement;
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
 * 成本要素控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/controlling/cost-elements")
@RequiredArgsConstructor
public class CoCostElementController {

    private final CoCostElementService costElementService;

    /**
     * 创建成本要素
     */
    @PostMapping
    @PreAuthorize("hasAuthority('controlling:element:add')")
    public Result<Long> createCostElement(@Valid @RequestBody CoCostElement costElement) {
        Long id = costElementService.createCostElement(costElement);
        return Result.success(id);
    }

    /**
     * 更新成本要素
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:element:edit')")
    public Result<CoCostElement> updateCostElement(
            @PathVariable Long id,
            @Valid @RequestBody CoCostElement costElement) {
        CoCostElement updated = costElementService.updateCostElement(id, costElement);
        return Result.success(updated);
    }

    /**
     * 删除成本要素
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:element:delete')")
    public Result<Void> deleteCostElement(@PathVariable Long id) {
        costElementService.deleteCostElement(id);
        return Result.success();
    }

    /**
     * 获取成本要素详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:element:view')")
    public Result<CoCostElement> getCostElementById(@PathVariable Long id) {
        CoCostElement costElement = costElementService.getCostElementById(id);
        return Result.success(costElement);
    }

    /**
     * 根据代码获取成本要素
     */
    @GetMapping("/code/{elementCode}")
    @PreAuthorize("hasAuthority('controlling:element:view')")
    public Result<CoCostElement> getCostElementByCode(
            @PathVariable String elementCode,
            @RequestParam Long tenantId) {
        CoCostElement costElement = costElementService.getCostElementByCode(elementCode, tenantId);
        return Result.success(costElement);
    }

    /**
     * 按类型查询成本要素
     */
    @GetMapping("/type/{elementType}")
    @PreAuthorize("hasAuthority('controlling:element:view')")
    public Result<List<CoCostElement>> listByElementType(
            @PathVariable String elementType,
            @RequestParam Long tenantId) {
        List<CoCostElement> list = costElementService.listByElementType(elementType, tenantId);
        return Result.success(list);
    }

    /**
     * 按类别查询成本要素
     */
    @GetMapping("/category/{elementCategory}")
    @PreAuthorize("hasAuthority('controlling:element:view')")
    public Result<List<CoCostElement>> listByElementCategory(
            @PathVariable String elementCategory,
            @RequestParam Long tenantId) {
        List<CoCostElement> list = costElementService.listByElementCategory(elementCategory, tenantId);
        return Result.success(list);
    }

    /**
     * 查询有效成本要素
     */
    @GetMapping("/valid")
    @PreAuthorize("hasAuthority('controlling:element:view')")
    public Result<List<CoCostElement>> listValidCostElements(@RequestParam Long tenantId) {
        List<CoCostElement> list = costElementService.listValidCostElements(tenantId);
        return Result.success(list);
    }

    /**
     * 分页查询成本要素
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('controlling:element:view')")
    public Result<PageResult<CoCostElement>> listCostElements(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<CoCostElement> page = costElementService.listCostElements(
                tenantId, PageRequest.of(current - 1, size));

        PageResult<CoCostElement> result = PageResult.<CoCostElement>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
