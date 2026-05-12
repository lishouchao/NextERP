package com.nexterp.business.sales.controller;

import com.nexterp.business.sales.application.service.SdConditionService;
import com.nexterp.business.sales.dto.ConditionDTO;
import com.nexterp.business.sales.dto.CreateConditionRequest;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 条件控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/sd/conditions")
@RequiredArgsConstructor
public class SdConditionController {

    private final SdConditionService conditionService;

    /**
     * 分页查询条件记录
     *
     * @param tenantId      租户ID
     * @param conditionType 条件类型
     * @param current       当前页
     * @param size          每页大小
     * @return 分页结果
     */
    @GetMapping
    @PreAuthorize("hasAuthority('sd:condition:view')")
    public Result<PageResult<ConditionDTO>> listConditions(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String conditionType,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        log.info("查询条件记录列表, tenantId={}, conditionType={}, current={}, size={}", tenantId, conditionType, current, size);
        return Result.success(conditionService.listConditions(tenantId, conditionType, current, size));
    }

    /**
     * 创建条件记录
     *
     * @param request 创建条件请求
     * @return 条件ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sd:condition:add')")
    public Result<Long> createCondition(@Valid @RequestBody CreateConditionRequest request) {
        log.info("创建条件记录");
        return Result.success(conditionService.createCondition(request));
    }

    /**
     * 获取条件记录详情
     *
     * @param id 条件ID
     * @return 条件记录
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sd:condition:view')")
    public Result<ConditionDTO> getConditionById(@PathVariable Long id) {
        log.info("获取条件记录详情, id={}", id);
        return Result.success(conditionService.getConditionById(id));
    }

    /**
     * 更新条件记录
     *
     * @param id      条件ID
     * @param request 创建条件请求
     * @return 更新后的条件记录
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sd:condition:edit')")
    public Result<Void> updateCondition(
            @PathVariable Long id,
            @Valid @RequestBody CreateConditionRequest request) {
        log.info("更新条件记录, id={}", id);
        conditionService.updateCondition(id, request);
        return Result.success();
    }

    /**
     * 删除条件记录
     *
     * @param id 条件ID
     * @return 成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sd:condition:delete')")
    public Result<Void> deleteCondition(@PathVariable Long id) {
        log.info("删除条件记录, id={}", id);
        conditionService.deleteCondition(id);
        return Result.success();
    }
}
