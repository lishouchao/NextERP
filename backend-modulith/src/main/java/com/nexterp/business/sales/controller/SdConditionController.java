package com.nexterp.business.sales.controller;

import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

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
    public Result<PageResult<Map<String, Object>>> listConditions(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String conditionType,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        log.info("查询条件记录列表, tenantId={}, conditionType={}, current={}, size={}", tenantId, conditionType, current, size);
        // TODO: 调用条件服务
        PageResult<Map<String, Object>> pageResult = PageResult.<Map<String, Object>>builder()
                .records(Collections.emptyList())
                .total(0L)
                .current(current)
                .size(size)
                .build();
        return Result.success(pageResult);
    }

    /**
     * 创建条件记录
     *
     * @param request 创建条件请求
     * @return 条件ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sd:condition:add')")
    public Result<Long> createCondition(@Valid @RequestBody Map<String, Object> request) {
        log.info("创建条件记录");
        // TODO: 调用条件服务
        return Result.success(1L);
    }

    /**
     * 获取条件记录详情
     *
     * @param id 条件ID
     * @return 条件记录
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sd:condition:view')")
    public Result<Map<String, Object>> getConditionById(@PathVariable Long id) {
        log.info("获取条件记录详情, id={}", id);
        // TODO: 调用条件服务
        return Result.success(Map.of("id", id));
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
    public Result<Map<String, Object>> updateCondition(
            @PathVariable Long id,
            @Valid @RequestBody Map<String, Object> request) {
        log.info("更新条件记录, id={}", id);
        // TODO: 调用条件服务
        return Result.success(Map.of("id", id));
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
        // TODO: 调用条件服务
        return Result.success();
    }
}
