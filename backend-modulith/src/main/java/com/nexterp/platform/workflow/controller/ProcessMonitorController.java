package com.nexterp.platform.workflow.controller;

import java.util.List;

import com.nexterp.platform.workflow.service.ProcessMonitorService;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 流程监控控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow/monitor")
@RequiredArgsConstructor
public class ProcessMonitorController {

    private final ProcessMonitorService processMonitorService;

    /**
     * 获取运行中的流程实例
     *
     * @param tenantId 租户ID
     * @return 流程实例列表
     */
    @GetMapping("/instances/running")
    @PreAuthorize("hasAuthority('system:workflow:monitor:view')")
    public Result<List<ProcessMonitorService.ProcessInstanceInfo>> getRunningProcessInstances(
            @RequestParam Long tenantId) {
        List<ProcessMonitorService.ProcessInstanceInfo> instances =
                processMonitorService.getRunningProcessInstances(tenantId);
        return Result.success(instances);
    }

    /**
     * 获取已完成的流程实例
     *
     * @param tenantId 租户ID
     * @return 流程实例列表
     */
    @GetMapping("/instances/finished")
    @PreAuthorize("hasAuthority('system:workflow:monitor:view')")
    public Result<List<ProcessMonitorService.ProcessInstanceInfo>> getFinishedProcessInstances(
            @RequestParam Long tenantId) {
        List<ProcessMonitorService.ProcessInstanceInfo> instances =
                processMonitorService.getFinishedProcessInstances(tenantId);
        return Result.success(instances);
    }

    /**
     * 获取流程实例详情
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例详情
     */
    @GetMapping("/instances/{processInstanceId}")
    @PreAuthorize("hasAuthority('system:workflow:monitor:view')")
    public Result<ProcessMonitorService.ProcessInstanceDetail> getProcessInstanceDetail(
            @PathVariable String processInstanceId) {
        ProcessMonitorService.ProcessInstanceDetail detail =
                processMonitorService.getProcessInstanceDetail(processInstanceId);
        return Result.success(detail);
    }

    /**
     * 获取流程实例的任务列表
     *
     * @param processInstanceId 流程实例ID
     * @return 任务列表
     */
    @GetMapping("/instances/{processInstanceId}/tasks")
    @PreAuthorize("hasAuthority('system:workflow:monitor:view')")
    public Result<List<ProcessMonitorService.TaskInfo>> getProcessInstanceTasks(
            @PathVariable String processInstanceId) {
        List<ProcessMonitorService.TaskInfo> tasks =
                processMonitorService.getProcessInstanceTasks(processInstanceId);
        return Result.success(tasks);
    }

    /**
     * 获取流程统计信息
     *
     * @param tenantId 租户ID
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('system:workflow:monitor:view')")
    public Result<ProcessMonitorService.ProcessStatistics> getProcessStatistics(
            @RequestParam Long tenantId) {
        ProcessMonitorService.ProcessStatistics statistics =
                processMonitorService.getProcessStatistics(tenantId);
        return Result.success(statistics);
    }
}
