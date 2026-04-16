package com.nexterp.business.supply.controller;

import com.nexterp.business.supply.application.service.MmMaterialService;
import com.nexterp.business.supply.dto.*;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/supply/materials")
@RequiredArgsConstructor
public class MmMaterialController {

    private final MmMaterialService materialService;

    @PostMapping
    @PreAuthorize("hasAuthority('mm:material:add')")
    public Result<Long> createMaterial(@Valid @RequestBody CreateMaterialRequest request) {
        Long id = materialService.createMaterial(request);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('mm:material:edit')")
    public Result<Void> updateMaterial(@PathVariable Long id, @Valid @RequestBody CreateMaterialRequest request) {
        materialService.updateMaterial(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('mm:material:delete')")
    public Result<Void> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mm:material:view')")
    public Result<MaterialDTO> getMaterial(@PathVariable Long id) {
        MaterialDTO dto = materialService.getMaterialById(id);
        return Result.success(dto);
    }

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('mm:material:view')")
    public Result<PageResult<MaterialDTO>> listMaterials(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String materialType,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<MaterialDTO> result = materialService.listMaterials(tenantId, materialType, current, size);
        return Result.success(result);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('mm:material:view')")
    public Result<PageResult<MaterialDTO>> searchMaterials(
            @RequestParam String keyword,
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<MaterialDTO> result = materialService.searchMaterials(tenantId, keyword, current, size);
        return Result.success(result);
    }
}
