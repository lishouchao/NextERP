package com.nexterp.platform.workflow.controller;

import com.nexterp.platform.workflow.service.WorkflowService;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    /**
     * 部署流程
     *
     * @param processName 流程名称
     * @param file        BPMN文件
     * @return 部署ID
     */
    @PostMapping("/deploy")
    public Result<String> deployProcess(
            @RequestParam String processName,
            @RequestParam MultipartFile file) {
        try {
            String deploymentId = workflowService.deployProcess(processName, file.getInputStream());
            return Result.success(deploymentId);
        } catch (IOException e) {
            log.error("读取文件失败", e);
            return Result.error("读取文件失败");
        }
    }

    /**
     * 启动流程
     *
     * @param request 启动请求
     * @return 流程实例ID
     */
    @PostMapping("/start")
    public Result<String> startProcess(@RequestBody StartProcessRequest request) {
        String processInstanceId = workflowService.startProcess(
                request.getProcessDefinitionKey(),
                request.getBusinessKey(),
                request.getVariables(),
                request.getInitiator()
        );
        return Result.success(processInstanceId);
    }

    /**
     * 完成任务
     *
     * @param request 完成任务请求
     * @return 成功响应
     */
    @PostMapping("/task/complete")
    public Result<Void> completeTask(@RequestBody CompleteTaskRequest request) {
        workflowService.completeTask(request.getTaskId(), request.getVariables());
        return Result.success();
    }

    /**
     * 获取用户待办任务
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    @GetMapping("/tasks/todo")
    public Result<List<TaskInfo>> getUserTasks(@RequestParam String userId) {
        List<Task> tasks = workflowService.getUserTasks(userId);
        List<TaskInfo> taskInfos = tasks.stream()
                .map(this::convertToTaskInfo)
                .toList();
        return Result.success(taskInfos);
    }

    /**
     * 获取用户候选任务
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    @GetMapping("/tasks/candidate")
    public Result<List<TaskInfo>> getUserCandidateTasks(@RequestParam String userId) {
        List<Task> tasks = workflowService.getUserCandidateTasks(userId);
        List<TaskInfo> taskInfos = tasks.stream()
                .map(this::convertToTaskInfo)
                .toList();
        return Result.success(taskInfos);
    }

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    @GetMapping("/task/{taskId}")
    public Result<TaskDetail> getTaskDetail(@PathVariable String taskId) {
        Task task = workflowService.getTask(taskId);
        Map<String, Object> variables = workflowService.getTaskVariables(taskId);

        TaskDetail detail = new TaskDetail();
        detail.setTaskId(task.getId());
        detail.setTaskName(task.getName());
        detail.setAssignee(task.getAssignee());
        detail.setCreateTime(task.getCreateTime());
        detail.setVariables(variables);

        return Result.success(detail);
    }

    /**
     * 获取流程定义列表
     *
     * @return 流程定义列表
     */
    @GetMapping("/definitions")
    public Result<List<ProcessDefinitionInfo>> getProcessDefinitions() {
        List<ProcessDefinition> definitions = workflowService.getProcessDefinitions();
        List<ProcessDefinitionInfo> infos = definitions.stream()
                .map(this::convertToProcessDefinitionInfo)
                .toList();
        return Result.success(infos);
    }

    /**
     * 获取流程实例状态
     *
     * @param processInstanceId 流程实例ID
     * @return 状态
     */
    @GetMapping("/instance/{processInstanceId}/status")
    public Result<String> getProcessInstanceStatus(@PathVariable String processInstanceId) {
        String status = workflowService.getProcessInstanceStatus(processInstanceId);
        return Result.success(status);
    }

    /**
     * 删除流程实例
     *
     * @param processInstanceId 流程实例ID
     * @param deleteReason      删除原因
     * @return 成功响应
     */
    @DeleteMapping("/instance/{processInstanceId}")
    public Result<Void> deleteProcessInstance(
            @PathVariable String processInstanceId,
            @RequestParam(required = false) String deleteReason) {
        workflowService.deleteProcessInstance(processInstanceId, deleteReason);
        return Result.success();
    }

    /**
     * 转换为任务信息
     *
     * @param task 任务
     * @return 任务信息
     */
    private TaskInfo convertToTaskInfo(Task task) {
        TaskInfo info = new TaskInfo();
        info.setTaskId(task.getId());
        info.setTaskName(task.getName());
        info.setAssignee(task.getAssignee());
        info.setCreateTime(task.getCreateTime());
        info.setProcessInstanceId(task.getProcessInstanceId());
        return info;
    }

    /**
     * 转换为流程定义信息
     *
     * @param definition 流程定义
     * @return 流程定义信息
     */
    private ProcessDefinitionInfo convertToProcessDefinitionInfo(ProcessDefinition definition) {
        ProcessDefinitionInfo info = new ProcessDefinitionInfo();
        info.setId(definition.getId());
        info.setKey(definition.getKey());
        info.setName(definition.getName());
        info.setVersion(definition.getVersion());
        info.setDeploymentId(definition.getDeploymentId());
        return info;
    }

    /**
     * 启动流程请求
     */
    public static class StartProcessRequest {
        private String processDefinitionKey;
        private String businessKey;
        private Map<String, Object> variables;
        private String initiator;

        public String getProcessDefinitionKey() {
            return processDefinitionKey;
        }

        public void setProcessDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
        }

        public String getBusinessKey() {
            return businessKey;
        }

        public void setBusinessKey(String businessKey) {
            this.businessKey = businessKey;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }

        public String getInitiator() {
            return initiator;
        }

        public void setInitiator(String initiator) {
            this.initiator = initiator;
        }
    }

    /**
     * 完成任务请求
     */
    public static class CompleteTaskRequest {
        private String taskId;
        private Map<String, Object> variables;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }

    /**
     * 任务信息
     */
    public static class TaskInfo {
        private String taskId;
        private String taskName;
        private String assignee;
        private java.util.Date createTime;
        private String processInstanceId;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getAssignee() {
            return assignee;
        }

        public void setAssignee(String assignee) {
            this.assignee = assignee;
        }

        public java.util.Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(java.util.Date createTime) {
            this.createTime = createTime;
        }

        public String getProcessInstanceId() {
            return processInstanceId;
        }

        public void setProcessInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
        }
    }

    /**
     * 任务详情
     */
    public static class TaskDetail extends TaskInfo {
        private Map<String, Object> variables;

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }

    /**
     * 流程定义信息
     */
    public static class ProcessDefinitionInfo {
        private String id;
        private String key;
        private String name;
        private Integer version;
        private String deploymentId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public String getDeploymentId() {
            return deploymentId;
        }

        public void setDeploymentId(String deploymentId) {
            this.deploymentId = deploymentId;
        }
    }
}
