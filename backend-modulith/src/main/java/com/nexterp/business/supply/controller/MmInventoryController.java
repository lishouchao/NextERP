package com.nexterp.business.supply.controller;

import com.nexterp.business.supply.application.service.MmInventoryService;
import com.nexterp.business.supply.dto.MaterialDocDTO;
import com.nexterp.business.supply.dto.StockDTO;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/supply/inventory")
@RequiredArgsConstructor
public class MmInventoryController {

    private final MmInventoryService inventoryService;

    @PostMapping("/goods-receipt")
    @PreAuthorize("hasAuthority('mm:inventory:goodsreceipt')")
    public Result<Long> postGoodsReceipt(
            @RequestParam Long poId,
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "101") String movementType) {
        Map<String, Object> params = new HashMap<>();
        params.put("poId", poId);
        params.put("tenantId", tenantId);
        params.put("movementType", movementType);
        params.put("movementCategory", "GR");
        Long docId = inventoryService.postGoodsMovement(params);
        return Result.success(docId);
    }

    @PostMapping("/goods-issue")
    @PreAuthorize("hasAuthority('mm:inventory:goodsissue')")
    public Result<Long> postGoodsIssue(
            @RequestParam Long deliveryId,
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "601") String movementType) {
        Map<String, Object> params = new HashMap<>();
        params.put("deliveryId", deliveryId);
        params.put("tenantId", tenantId);
        params.put("movementType", movementType);
        params.put("movementCategory", "GI");
        Long docId = inventoryService.postGoodsMovement(params);
        return Result.success(docId);
    }

    @GetMapping("/stock")
    @PreAuthorize("hasAuthority('mm:inventory:view')")
    public Result<PageResult<StockDTO>> listStock(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) Long materialId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        if (plantId != null) {
            return Result.success(inventoryService.listStockByPlant(plantId, tenantId, current, size));
        }
        if (materialId != null) {
            List<StockDTO> list = inventoryService.listStockByMaterial(materialId, tenantId);
            PageResult<StockDTO> result = PageResult.<StockDTO>builder()
                    .records(list).total(list.size()).current(1).size(list.size()).build();
            return Result.success(result);
        }
        return Result.success(PageResult.<StockDTO>builder()
                .records(List.of()).total(0L).current(1).size(size).build());
    }

    @GetMapping("/material-docs")
    @PreAuthorize("hasAuthority('mm:inventory:view')")
    public Result<PageResult<MaterialDocDTO>> listMaterialDocs(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(inventoryService.listMaterialDocs(tenantId, current, size));
    }
}
