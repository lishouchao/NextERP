package com.nexterp.business.sales.controller;

import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 定价预览
     *
     * @param params 定价参数（tenantId, customerId, materialId, quantity, pricingDate）
     * @return 定价预览结果
     */
    @PostMapping("/preview")
    @PreAuthorize("hasAuthority('sd:pricing:view')")
    public Result<Map<String, Object>> previewPricing(@RequestBody Map<String, Object> params) {
        Long tenantId = params.get("tenantId") != null ? Long.valueOf(params.get("tenantId").toString()) : null;
        Long customerId = params.get("customerId") != null ? Long.valueOf(params.get("customerId").toString()) : null;
        Long materialId = params.get("materialId") != null ? Long.valueOf(params.get("materialId").toString()) : null;
        Object quantity = params.get("quantity");
        String pricingDate = params.get("pricingDate") != null ? params.get("pricingDate").toString() : null;

        log.info("定价预览, tenantId={}, customerId={}, materialId={}, quantity={}, pricingDate={}",
                tenantId, customerId, materialId, quantity, pricingDate);
        // TODO: 调用定价服务
        Map<String, Object> result = new HashMap<>();
        result.put("tenantId", tenantId);
        result.put("customerId", customerId);
        result.put("materialId", materialId);
        result.put("quantity", quantity);
        result.put("pricingDate", pricingDate);
        result.put("netPrice", 0);
        result.put("conditions", new ArrayList<>());
        return Result.success(result);
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
