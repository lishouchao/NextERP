package com.nexterp.business.production.controller;

import com.nexterp.business.production.application.service.ProOperationRecordService;
import com.nexterp.business.production.dto.*;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 工序执行记录控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/production/operation-records")
@RequiredArgsConstructor
public class ProOperationRecordController {

    private final ProOperationRecordService operationRecordService;

    /**
     * 分页查询工序执行记录
     *
     * @param tenantId     租户ID
     * @param status       状态 (可选)
     * @param workCenterId 工作中心ID (可选)
     * @param workerId     报工人员ID (可选)
     * @param current      当前页
     * @param size         每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('pp:operation:view')")
    public Result<PageResult<ProOperationRecordDTO>> listOperationRecords(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long workCenterId,
            @RequestParam(required = false) Long workerId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        log.info("查询工序执行记录列表, tenantId={}, status={}, workCenterId={}, workerId={}, current={}, size={}",
                tenantId, status, workCenterId, workerId, current, size);
        return Result.success(operationRecordService.listOperationRecords(tenantId, status, workCenterId, workerId, current, size));
    }

    /**
     * 创建工序执行记录
     *
     * @param request 创建请求
     * @return 记录ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('pp:operation:add')")
    public Result<Long> createOperationRecord(@Valid @RequestBody CreateOperationRecordRequest request) {
        log.info("创建工序执行记录, productionOrderId={}, sequenceNo={}", request.getProductionOrderId(), request.getSequenceNo());
        return Result.success(operationRecordService.createOperationRecord(request));
    }

    /**
     * 获取工序执行记录详情
     *
     * @param id 记录ID
     * @return 工序执行记录DTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:operation:view')")
    public Result<ProOperationRecordDTO> getOperationRecordById(@PathVariable Long id) {
        log.info("获取工序执行记录详情, id={}", id);
        return Result.success(operationRecordService.getOperationRecordById(id));
    }

    /**
     * 更新工序执行记录
     *
     * @param id      记录ID
     * @param request 更新请求
     * @return 成功
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:operation:edit')")
    public Result<Void> updateOperationRecord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOperationRecordRequest request) {
        log.info("更新工序执行记录, id={}", id);
        operationRecordService.updateOperationRecord(id, request);
        return Result.success();
    }

    /**
     * 获取指定生产订单的所有工序执行记录
     *
     * @param productionOrderId 生产订单ID
     * @return 工序执行记录列表
     */
    @GetMapping("/order/{productionOrderId}")
    @PreAuthorize("hasAuthority('pp:operation:view')")
    public Result<List<ProOperationRecordDTO>> getOrderOperationRecords(@PathVariable Long productionOrderId) {
        log.info("获取生产订单工序记录列表, productionOrderId={}", productionOrderId);
        return Result.success(operationRecordService.getOrderOperationRecords(productionOrderId));
    }

    /**
     * 获取生产进度
     *
     * @param productionOrderId 生产订单ID
     * @return 生产进度DTO
     */
    @GetMapping("/order/{productionOrderId}/progress")
    @PreAuthorize("hasAuthority('pp:operation:view')")
    public Result<ProductionProgressDTO> getProductionProgress(@PathVariable Long productionOrderId) {
        log.info("获取生产进度, productionOrderId={}", productionOrderId);
        return Result.success(operationRecordService.getProductionProgress(productionOrderId));
    }

    /**
     * 工序开工
     *
     * @param id         记录ID
     * @param workerId   报工人员ID
     * @param workerName 报工人员姓名
     * @return 成功
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('pp:operation:edit')")
    public Result<Void> startOperation(
            @PathVariable Long id,
            @RequestParam Long workerId,
            @RequestParam String workerName) {
        log.info("工序开工, id={}, workerId={}, workerName={}", id, workerId, workerName);
        operationRecordService.startOperation(id, workerId, workerName);
        return Result.success();
    }

    /**
     * 工序完工
     *
     * @param id                 记录ID
     * @param completedQty       完工数量
     * @param qualifiedQty       合格数量
     * @param scrappedQty        报废数量
     * @param actualManHours     实际工时
     * @param actualMachineHours 实际机时
     * @return 成功
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('pp:operation:edit')")
    public Result<Void> completeOperation(
            @PathVariable Long id,
            @RequestParam BigDecimal completedQty,
            @RequestParam BigDecimal qualifiedQty,
            @RequestParam BigDecimal scrappedQty,
            @RequestParam BigDecimal actualManHours,
            @RequestParam BigDecimal actualMachineHours) {
        log.info("工序完工, id={}, completedQty={}, qualifiedQty={}", id, completedQty, qualifiedQty);
        operationRecordService.completeOperation(id, completedQty, qualifiedQty, scrappedQty, actualManHours, actualMachineHours);
        return Result.success();
    }
}
