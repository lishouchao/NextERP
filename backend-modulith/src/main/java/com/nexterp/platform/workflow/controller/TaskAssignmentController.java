package com.nexterp.platform.workflow.controller;

import com.nexterp.platform.workflow.domain.model.TaskAssignment;
import com.nexterp.platform.workflow.service.TaskAssignmentService;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务分配控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow/assignments")
@RequiredArgsConstructor
public class TaskAssignmentController {

    private final TaskAssignmentService taskAssignmentService;

    /**
     * 创建任务分配规则
     *
     * @param assignment 分配规则
     * @return 规则ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:workflow:assignment:add')")
    public Result<Long> createAssignment(@RequestBody TaskAssignment assignment) {
        Long id = taskAssignmentService.createAssignment(assignment);
        return Result.success(id);
    }

    /**
     * 更新任务分配规则
     *
     * @param id 规则ID
     * @param assignment 分配规则
     * @return 更新后的规则
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:workflow:assignment:edit')")
    public Result<TaskAssignment> updateAssignment(
            @PathVariable Long id,
            @RequestBody TaskAssignment assignment) {
        TaskAssignment updated = taskAssignmentService.updateAssignment(id, assignment);
        return Result.success(updated);
    }

    /**
     * 删除任务分配规则
     *
     * @param id 规则ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:workflow:assignment:delete')")
    public Result<Void> deleteAssignment(@PathVariable Long id) {
        taskAssignmentService.deleteAssignment(id);
        return Result.success();
    }

    /**
     * 获取任务的分配规则
     *
     * @param processKey 流程Key
     * @param taskKey 任务Key
     * @param tenantId 租户ID
     * @return 分配规则列表
     */
    @GetMapping("/task")
    @PreAuthorize("hasAuthority('system:workflow:assignment:view')")
    public Result<List<TaskAssignment>> getTaskAssignments(
            @RequestParam String processKey,
            @RequestParam String taskKey,
            @RequestParam Long tenantId) {
        List<TaskAssignment> assignments = taskAssignmentService.getTaskAssignments(processKey, taskKey, tenantId);
        return Result.success(assignments);
    }

    /**
     * 获取租户所有分配规则
     *
     * @param tenantId 租户ID
     * @return 分配规则列表
     */
    @GetMapping("/tenant")
    @PreAuthorize("hasAuthority('system:workflow:assignment:view')")
    public Result<List<TaskAssignment>> getTenantAssignments(@RequestParam Long tenantId) {
        List<TaskAssignment> assignments = taskAssignmentService.getTenantAssignments(tenantId);
        return Result.success(assignments);
    }

    /**
     * 根据分配规则计算任务候选人
     *
     * @param processKey 流程Key
     * @param taskKey 任务Key
     * @param tenantId 租户ID
     * @return 候选人列表
     */
    @GetMapping("/candidates")
    @PreAuthorize("hasAuthority('system:workflow:assignment:view')")
    public Result<List<String>> calculateCandidates(
            @RequestParam String processKey,
            @RequestParam String taskKey,
            @RequestParam Long tenantId) {
        List<String> candidates = taskAssignmentService.calculateCandidates(processKey, taskKey, tenantId);
        return Result.success(candidates);
    }
}
