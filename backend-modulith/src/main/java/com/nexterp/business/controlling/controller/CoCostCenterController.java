package com.nexterp.business.controlling.controller;

import com.nexterp.business.controlling.application.service.CoCostCenterService;
import com.nexterp.business.controlling.domain.model.CoCostCenter;
import com.nexterp.business.controlling.domain.model.CoCostCenterGroup;
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
 * 成本中心控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/controlling/cost-centers")
@RequiredArgsConstructor
public class CoCostCenterController {

    private final CoCostCenterService costCenterService;

    /**
     * 创建成本中心
     */
    @PostMapping
    @PreAuthorize("hasAuthority('controlling:costcenter:add')")
    public Result<Long> createCostCenter(@Valid @RequestBody CoCostCenter costCenter) {
        Long id = costCenterService.createCostCenter(costCenter);
        return Result.success(id);
    }

    /**
     * 更新成本中心
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:costcenter:edit')")
    public Result<CoCostCenter> updateCostCenter(
            @PathVariable Long id,
            @Valid @RequestBody CoCostCenter costCenter) {
        CoCostCenter updated = costCenterService.updateCostCenter(id, costCenter);
        return Result.success(updated);
    }

    /**
     * 删除成本中心
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:costcenter:delete')")
    public Result<Void> deleteCostCenter(@PathVariable Long id) {
        costCenterService.deleteCostCenter(id);
        return Result.success();
    }

    /**
     * 获取成本中心详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('controlling:costcenter:view')")
    public Result<CoCostCenter> getCostCenterById(@PathVariable Long id) {
        CoCostCenter costCenter = costCenterService.getCostCenterById(id);
        return Result.success(costCenter);
    }

    /**
     * 根据代码获取成本中心
     */
    @GetMapping("/code/{costCenterCode}")
    @PreAuthorize("hasAuthority('controlling:costcenter:view')")
    public Result<CoCostCenter> getCostCenterByCode(
            @PathVariable String costCenterCode,
            @RequestParam Long tenantId) {
        CoCostCenter costCenter = costCenterService.getCostCenterByCode(costCenterCode, tenantId);
        return Result.success(costCenter);
    }

    /**
     * 按类型查询成本中心
     */
    @GetMapping("/type/{costCenterType}")
    @PreAuthorize("hasAuthority('controlling:costcenter:view')")
    public Result<List<CoCostCenter>> listByType(
            @PathVariable String costCenterType,
            @RequestParam Long tenantId) {
        List<CoCostCenter> list = costCenterService.listByType(costCenterType, tenantId);
        return Result.success(list);
    }

    /**
     * 按组查询成本中心
     */
    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAuthority('controlling:costcenter:view')")
    public Result<List<CoCostCenter>> listByGroup(
            @PathVariable Long groupId,
            @RequestParam Long tenantId) {
        List<CoCostCenter> list = costCenterService.listByGroup(groupId, tenantId);
        return Result.success(list);
    }

    /**
     * 查询有效成本中心
     */
    @GetMapping("/valid")
    @PreAuthorize("hasAuthority('controlling:costcenter:view')")
    public Result<List<CoCostCenter>> listValidCostCenters(@RequestParam Long tenantId) {
        List<CoCostCenter> list = costCenterService.listValidCostCenters(tenantId);
        return Result.success(list);
    }

    /**
     * 分页查询成本中心
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('controlling:costcenter:view')")
    public Result<PageResult<CoCostCenter>> listCostCenters(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<CoCostCenter> page = costCenterService.listCostCenters(
                tenantId, PageRequest.of(current - 1, size));

        PageResult<CoCostCenter> result = PageResult.<CoCostCenter>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }

    /**
     * 获取成本中心组树
     */
    @GetMapping("/groups/tree")
    @PreAuthorize("hasAuthority('controlling:costcenter:view')")
    public Result<List<CoCostCenterGroup>> getCostCenterGroupTree(@RequestParam Long tenantId) {
        List<CoCostCenterGroup> tree = costCenterService.getCostCenterGroupTree(tenantId);
        return Result.success(tree);
    }

    /**
     * 创建成本中心组
     */
    @PostMapping("/groups")
    @PreAuthorize("hasAuthority('controlling:costcenter:add')")
    public Result<Long> createCostCenterGroup(@Valid @RequestBody CoCostCenterGroup group) {
        Long id = costCenterService.createCostCenterGroup(group);
        return Result.success(id);
    }
}
