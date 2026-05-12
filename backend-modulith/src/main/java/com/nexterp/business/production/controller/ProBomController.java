package com.nexterp.business.production.controller;

import com.nexterp.business.production.application.service.ProBomService;
import com.nexterp.business.production.dto.CreateBomRequest;
import com.nexterp.business.production.dto.ProBomDTO;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 物料清单(BOM)控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/production/boms")
@RequiredArgsConstructor
public class ProBomController {

    private final ProBomService bomService;

    /**
     * 获取BOM详情
     *
     * @param id BOM ID
     * @return BOM详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:bom:view')")
    public Result<ProBomDTO> getBomById(@PathVariable Long id) {
        ProBomDTO bom = bomService.getBomById(id);
        return Result.success(bom);
    }

    /**
     * 分页查询BOM
     *
     * @param tenantId 租户ID
     * @param bomType  BOM类型
     * @param status   状态
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('pp:bom:view')")
    public Result<PageResult<ProBomDTO>> listBoms(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Integer bomType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ProBomDTO> result = bomService.listBoms(tenantId, bomType, status, current, size);
        return Result.success(result);
    }

    /**
     * 创建BOM
     *
     * @param request 创建请求
     * @return BOM ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('pp:bom:add')")
    public Result<Long> createBom(@Valid @RequestBody CreateBomRequest request) {
        Long id = bomService.createBom(request);
        return Result.success(id);
    }

    /**
     * 更新BOM
     *
     * @param id      BOM ID
     * @param request 更新请求
     * @return 成功
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:bom:edit')")
    public Result<Void> updateBom(@PathVariable Long id, @Valid @RequestBody CreateBomRequest request) {
        bomService.updateBom(id, request);
        return Result.success();
    }

    /**
     * 删除BOM
     *
     * @param id BOM ID
     * @return 成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:bom:delete')")
    public Result<Void> deleteBom(@PathVariable Long id) {
        bomService.deleteBom(id);
        return Result.success();
    }

    /**
     * 启用BOM
     *
     * @param id BOM ID
     * @return 成功
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('pp:bom:edit')")
    public Result<Void> activateBom(@PathVariable Long id) {
        bomService.activateBom(id);
        return Result.success();
    }

    /**
     * 停用BOM
     *
     * @param id BOM ID
     * @return 成功
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('pp:bom:edit')")
    public Result<Void> deactivateBom(@PathVariable Long id) {
        bomService.deactivateBom(id);
        return Result.success();
    }
}
