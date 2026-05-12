package com.nexterp.business.sales.controller;

import com.nexterp.business.sales.application.service.SdConditionService;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定价控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/sd/pricing")
@RequiredArgsConstructor
public class SdPricingController {

    private final SdConditionService conditionService;

    /**
     * 定价预览
     *
     * @param tenantId     租户ID
     * @param customerId   客户ID
     * @param materialId   物料ID
     * @param qty          数量
     * @param pricingDate  定价日期
     * @return 定价预览结果
     */
    @PostMapping("/preview")
    @PreAuthorize("hasAuthority('sd:pricing:view')")
    public Result<Map<String, Object>> previewPricing(
            @RequestParam Long tenantId,
            @RequestParam Long customerId,
            @RequestParam Long materialId,
            @RequestParam BigDecimal qty,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pricingDate) {
        log.info("定价预览, tenantId={}, customerId={}, materialId={}, qty={}, pricingDate={}",
                tenantId, customerId, materialId, qty, pricingDate);
        return Result.success(conditionService.previewPricing(tenantId, customerId, materialId, qty, pricingDate));
    }

    /**
     * 查询定价过程列表
     *
     * @return 定价过程列表
     */
    @GetMapping("/pricing-procedures")
    @PreAuthorize("hasAuthority('sd:pricing:view')")
    public Result<List<Map<String, Object>>> listPricingProcedures() {
        log.info("查询定价过程列表");
        // 返回静态定价过程 RVAA01
        List<Map<String, Object>> procedures = new ArrayList<>();
        Map<String, Object> procedure = new HashMap<>();
        procedure.put("code", "RVAA01");
        procedure.put("name", "标准定价过程");
        procedure.put("description", "SAP标准销售定价过程");
        procedures.add(procedure);
        return Result.success(procedures);
    }
}
