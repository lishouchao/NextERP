package com.nexterp.business.supply.controller;

import com.nexterp.business.supply.application.service.SupSupplierService;
import com.nexterp.business.supply.domain.model.SupSupplier;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/supply/suppliers")
@RequiredArgsConstructor
public class SupSupplierController {

    private final SupSupplierService supplierService;

    /**
     * 创建供应商
     *
     * @param supplier 供应商
     * @return 供应商ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('supply:supplier:add')")
    public Result<Long> createSupplier(@Valid @RequestBody SupSupplier supplier) {
        Long id = supplierService.createSupplier(supplier);
        return Result.success(id);
    }

    /**
     * 更新供应商
     *
     * @param id       供应商ID
     * @param supplier 供应商
     * @return 更新后的供应商
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supply:supplier:edit')")
    public Result<SupSupplier> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupSupplier supplier) {
        SupSupplier updated = supplierService.updateSupplier(id, supplier);
        return Result.success(updated);
    }

    /**
     * 删除供应商
     *
     * @param id 供应商ID
     * @return 成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supply:supplier:delete')")
    public Result<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return Result.success();
    }

    /**
     * 启用/禁用供应商
     *
     * @param id     供应商ID
     * @param status 状态
     * @return 成功
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('supply:supplier:edit')")
    public Result<Void> updateSupplierStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        supplierService.updateSupplierStatus(id, status);
        return Result.success();
    }

    /**
     * 获取供应商详情
     *
     * @param id 供应商ID
     * @return 供应商
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('supply:supplier:view')")
    public Result<SupSupplier> getSupplierById(@PathVariable Long id) {
        SupSupplier supplier = supplierService.getSupplierById(id);
        return Result.success(supplier);
    }

    /**
     * 分页查询供应商
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('supply:supplier:view')")
    public Result<PageResult<SupSupplier>> listSuppliers(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(current - 1, size);
        PageResult<SupSupplier> result = supplierService.listSuppliers(tenantId, status, pageable);
        return Result.success(result);
    }

    /**
     * 查询启用状态的供应商
     *
     * @param tenantId 租户ID
     * @return 供应商列表
     */
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('supply:supplier:view')")
    public Result<List<SupSupplier>> listActiveSuppliers(@RequestParam Long tenantId) {
        List<SupSupplier> suppliers = supplierService.listActiveSuppliers(tenantId);
        return Result.success(suppliers);
    }

    /**
     * 根据分类查询供应商
     *
     * @param categoryId 分类ID
     * @param tenantId   租户ID
     * @return 供应商列表
     */
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAuthority('supply:supplier:view')")
    public Result<List<SupSupplier>> listSuppliersByCategory(
            @PathVariable Long categoryId,
            @RequestParam Long tenantId) {
        List<SupSupplier> suppliers = supplierService.listSuppliersByCategory(categoryId, tenantId);
        return Result.success(suppliers);
    }

    /**
     * 根据类型查询供应商
     *
     * @param supplierType 供应商类型
     * @param tenantId      租户ID
     * @return 供应商列表
     */
    @GetMapping("/type/{supplierType}")
    @PreAuthorize("hasAuthority('supply:supplier:view')")
    public Result<List<SupSupplier>> listSuppliersByType(
            @PathVariable Integer supplierType,
            @RequestParam Long tenantId) {
        List<SupSupplier> suppliers = supplierService.listSuppliersByType(supplierType, tenantId);
        return Result.success(suppliers);
    }

    /**
     * 搜索供应商
     *
     * @param keyword  关键词
     * @param tenantId 租户ID
     * @return 供应商列表
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('supply:supplier:view')")
    public Result<List<SupSupplier>> searchSuppliers(
            @RequestParam String keyword,
            @RequestParam Long tenantId) {
        List<SupSupplier> suppliers = supplierService.searchSuppliers(keyword, tenantId);
        return Result.success(suppliers);
    }
}
