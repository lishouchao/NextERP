package com.nexterp.business.production.controller;

import com.nexterp.business.production.application.service.ProWorkProcessService;
import com.nexterp.business.production.dto.CreateWorkProcessRequest;
import com.nexterp.business.production.dto.ProWorkProcessDTO;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 工序控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/production/work-processes")
@RequiredArgsConstructor
public class ProWorkProcessController {

    private final ProWorkProcessService workProcessService;

    /**
     * 获取工序详情
     *
     * @param id 工序ID
     * @return 工序详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:process:view')")
    public Result<ProWorkProcessDTO> getWorkProcessById(@PathVariable Long id) {
        ProWorkProcessDTO process = workProcessService.getWorkProcessById(id);
        return Result.success(process);
    }

    /**
     * 分页查询工序
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('pp:process:view')")
    public Result<PageResult<ProWorkProcessDTO>> listWorkProcesses(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ProWorkProcessDTO> result = workProcessService.listWorkProcesses(tenantId, status, current, size);
        return Result.success(result);
    }

    /**
     * 创建工序
     *
     * @param request 创建请求
     * @return 工序ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('pp:process:add')")
    public Result<Long> createWorkProcess(@Valid @RequestBody CreateWorkProcessRequest request) {
        Long id = workProcessService.createWorkProcess(request);
        return Result.success(id);
    }

    /**
     * 更新工序
     *
     * @param id      工序ID
     * @param request 更新请求
     * @return 成功
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:process:edit')")
    public Result<Void> updateWorkProcess(@PathVariable Long id, @Valid @RequestBody CreateWorkProcessRequest request) {
        workProcessService.updateWorkProcess(id, request);
        return Result.success();
    }

    /**
     * 删除工序
     *
     * @param id 工序ID
     * @return 成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('pp:process:delete')")
    public Result<Void> deleteWorkProcess(@PathVariable Long id) {
        workProcessService.deleteWorkProcess(id);
        return Result.success();
    }
}
