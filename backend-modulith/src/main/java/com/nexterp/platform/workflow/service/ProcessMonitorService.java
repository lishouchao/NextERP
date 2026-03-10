package com.nexterp.platform.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.engine.TaskService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程监控服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessMonitorService {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    /**
     * 获取运行中的流程实例
     *
     * @param tenantId 租户ID
     * @return 流程实例列表
     */
    public List<ProcessInstanceInfo> getRunningProcessInstances(Long tenantId) {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .variableValueEquals("tenantId", tenantId)
                .active()
                .orderByStartTime()
                .desc()
                .list();

        return instances.stream()
                .map(this::convertToInfo)
                .collect(Collectors.toList());
    }

    /**
     * 获取已完成的流程实例
     *
     * @param tenantId 租户ID
     * @return 流程实例列表
     */
    public List<ProcessInstanceInfo> getFinishedProcessInstances(Long tenantId) {
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .variableValueEquals("tenantId", tenantId)
                .finished()
                .orderByProcessInstanceEndTime()
                .desc()
                .list();

        return instances.stream()
                .map(this::convertToInfo)
                .collect(Collectors.toList());
    }

    /**
     * 获取流程实例详情
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例详情
     */
    public ProcessInstanceDetail getProcessInstanceDetail(String processInstanceId) {
        // 先查询运行中的实例
        ProcessInstance runningInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (runningInstance != null) {
            return buildDetail(runningInstance, null);
        }

        // 查询历史实例
        HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (historicInstance != null) {
            return buildDetail(null, historicInstance);
        }

        throw new BusinessException("流程实例不存在");
    }

    /**
     * 获取流程实例的任务列表
     *
     * @param processInstanceId 流程实例ID
     * @return 任务列表
     */
    public List<TaskInfo> getProcessInstanceTasks(String processInstanceId) {
        // 获取当前活动任务
        List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .orderByTaskCreateTime()
                .desc()
                .list();

        // 获取历史任务
        List<org.flowable.task.api.history.HistoricTaskInstance> historicTasks =
                historyService.createHistoricTaskInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .finished()
                        .orderByTaskCreateTime()
                        .desc()
                        .list();

        List<TaskInfo> allTasks = new ArrayList<>();

        // 添加当前活动任务
        for (Task task : activeTasks) {
            allTasks.add(TaskInfo.builder()
                    .taskId(task.getId())
                    .taskName(task.getName())
                    .assignee(task.getAssignee())
                    .createTime(toLocalDateTime(task.getCreateTime()))
                    .dueDate(toLocalDateTime(task.getDueDate()))
                    .status("active")
                    .build());
        }

        // 添加已完成任务
        for (org.flowable.task.api.history.HistoricTaskInstance task : historicTasks) {
            allTasks.add(TaskInfo.builder()
                    .taskId(task.getId())
                    .taskName(task.getName())
                    .assignee(task.getAssignee())
                    .createTime(toLocalDateTime(task.getCreateTime()))
                    .dueDate(toLocalDateTime(task.getDueDate()))
                    .endTime(toLocalDateTime(task.getEndTime()))
                    .status("completed")
                    .duration(task.getDurationInMillis())
                    .build());
        }

        return allTasks;
    }

    /**
     * 获取流程统计信息
     *
     * @param tenantId 租户ID
     * @return 统计信息
     */
    public ProcessStatistics getProcessStatistics(Long tenantId) {
        long runningCount = runtimeService.createProcessInstanceQuery()
                .variableValueEquals("tenantId", tenantId)
                .active()
                .count();

        long finishedCount = historyService.createHistoricProcessInstanceQuery()
                .variableValueEquals("tenantId", tenantId)
                .finished()
                .count();

        long activeTaskCount = taskService.createTaskQuery()
                .processVariableValueEquals("tenantId", tenantId)
                .active()
                .count();

        return ProcessStatistics.builder()
                .runningCount(runningCount)
                .finishedCount(finishedCount)
                .activeTaskCount(activeTaskCount)
                .totalCount(runningCount + finishedCount)
                .build();
    }

    /**
     * 转换为流程实例信息
     *
     * @param instance 流程实例
     * @return 流程实例信息
     */
    private ProcessInstanceInfo convertToInfo(ProcessInstance instance) {
        return ProcessInstanceInfo.builder()
                .processInstanceId(instance.getId())
                .processDefinitionKey(instance.getProcessDefinitionKey())
                .processDefinitionName(instance.getProcessDefinitionName())
                .businessKey(instance.getBusinessKey())
                .startTime(toLocalDateTime(instance.getStartTime()))
                .status("running")
                .build();
    }

    /**
     * 转换为流程实例信息
     *
     * @param instance 历史流程实例
     * @return 流程实例信息
     */
    private ProcessInstanceInfo convertToInfo(HistoricProcessInstance instance) {
        return ProcessInstanceInfo.builder()
                .processInstanceId(instance.getId())
                .processDefinitionKey(instance.getProcessDefinitionKey())
                .processDefinitionName(instance.getProcessDefinitionName())
                .businessKey(instance.getBusinessKey())
                .startTime(toLocalDateTime(instance.getStartTime()))
                .endTime(toLocalDateTime(instance.getEndTime()))
                .duration(instance.getDurationInMillis())
                .status("finished")
                .build();
    }

    /**
     * 构建流程实例详情
     *
     * @param runningInstance 运行中实例
     * @param historicInstance 历史实例
     * @return 流程实例详情
     */
    private ProcessInstanceDetail buildDetail(ProcessInstance runningInstance,
                                               HistoricProcessInstance historicInstance) {
        if (runningInstance != null) {
            Map<String, Object> variables = runtimeService.getVariables(runningInstance.getId());
            List<TaskInfo> tasks = getProcessInstanceTasks(runningInstance.getId());

            return ProcessInstanceDetail.builder()
                    .processInstanceId(runningInstance.getId())
                    .processDefinitionKey(runningInstance.getProcessDefinitionKey())
                    .processDefinitionName(runningInstance.getProcessDefinitionName())
                    .businessKey(runningInstance.getBusinessKey())
                    .startTime(toLocalDateTime(runningInstance.getStartTime()))
                    .status("running")
                    .variables(variables)
                    .tasks(tasks)
                    .build();
        } else {
            Map<String, Object> variables = historicInstance.getProcessVariables();
            List<TaskInfo> tasks = getProcessInstanceTasks(historicInstance.getId());

            return ProcessInstanceDetail.builder()
                    .processInstanceId(historicInstance.getId())
                    .processDefinitionKey(historicInstance.getProcessDefinitionKey())
                    .processDefinitionName(historicInstance.getProcessDefinitionName())
                    .businessKey(historicInstance.getBusinessKey())
                    .startTime(toLocalDateTime(historicInstance.getStartTime()))
                    .endTime(toLocalDateTime(historicInstance.getEndTime()))
                    .duration(historicInstance.getDurationInMillis())
                    .status("finished")
                    .variables(variables)
                    .tasks(tasks)
                    .build();
        }
    }

    /**
     * 转换Date为LocalDateTime
     *
     * @param date Date
     * @return LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * 流程实例信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProcessInstanceInfo {
        private String processInstanceId;
        private String processDefinitionKey;
        private String processDefinitionName;
        private String businessKey;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long duration;
        private String status;
    }

    /**
     * 流程实例详情
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProcessInstanceDetail {
        private String processInstanceId;
        private String processDefinitionKey;
        private String processDefinitionName;
        private String businessKey;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long duration;
        private String status;
        private Map<String, Object> variables;
        private List<TaskInfo> tasks;
    }

    /**
     * 任务信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TaskInfo {
        private String taskId;
        private String taskName;
        private String assignee;
        private LocalDateTime createTime;
        private LocalDateTime dueDate;
        private LocalDateTime endTime;
        private Long duration;
        private String status;
    }

    /**
     * 流程统计信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProcessStatistics {
        private Long runningCount;
        private Long finishedCount;
        private Long activeTaskCount;
        private Long totalCount;
    }
}
