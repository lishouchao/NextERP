package com.nexterp.platform.workflow.service;

import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final IdentityService identityService;

    /**
     * 部署流程定义
     *
     * @param processName 流程名称
     * @param bpmnStream  BPMN文件流
     * @return 部署ID
     */
    @Transactional(rollbackFor = Exception.class)
    public String deployProcess(String processName, InputStream bpmnStream) {
        try {
            Deployment deployment = repositoryService.createDeployment()
                    .name(processName)
                    .addInputStream(processName + ".bpmn20.xml", bpmnStream)
                    .deploy();

            log.info("部署流程成功: deploymentId={}, processName={}", deployment.getId(), processName);
            return deployment.getId();
        } catch (Exception e) {
            log.error("部署流程失败: processName={}", processName, e);
            throw new BusinessException("部署流程失败: " + e.getMessage());
        }
    }

    /**
     * 启动流程实例
     *
     * @param processDefinitionKey 流程定义Key
     * @param businessKey         业务Key
     * @param variables            流程变量
     * @param initiator            发起人
     * @return 流程实例ID
     */
    @Transactional(rollbackFor = Exception.class)
    public String startProcess(String processDefinitionKey, String businessKey,
                               Map<String, Object> variables, String initiator) {
        try {
            // 设置发起人
            if (initiator != null) {
                identityService.setAuthenticatedUserId(initiator);
            }

            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                    processDefinitionKey,
                    businessKey,
                    variables
            );

            log.info("启动流程成功: processInstanceId={}, processDefinitionKey={}, businessKey={}",
                    processInstance.getId(), processDefinitionKey, businessKey);
            return processInstance.getId();
        } catch (Exception e) {
            log.error("启动流程失败: processDefinitionKey={}", processDefinitionKey, e);
            throw new BusinessException("启动流程失败: " + e.getMessage());
        }
    }

    /**
     * 完成任务
     *
     * @param taskId    任务ID
     * @param variables 流程变量
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, Map<String, Object> variables) {
        try {
            taskService.complete(taskId, variables);
            log.info("完成任务成功: taskId={}", taskId);
        } catch (Exception e) {
            log.error("完成任务失败: taskId={}", taskId, e);
            throw new BusinessException("完成任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户待办任务
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    public List<Task> getUserTasks(String userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId)
                .active()
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    /**
     * 获取用户候选任务
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    public List<Task> getUserCandidateTasks(String userId) {
        return taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .active()
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务
     */
    public Task getTask(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        return task;
    }

    /**
     * 获取流程变量
     *
     * @param taskId 任务ID
     * @return 流程变量
     */
    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    /**
     * 设置流程变量
     *
     * @param taskId    任务ID
     * @param variables 流程变量
     */
    @Transactional(rollbackFor = Exception.class)
    public void setTaskVariables(String taskId, Map<String, Object> variables) {
        taskService.setVariables(taskId, variables);
    }

    /**
     * 删除流程实例
     *
     * @param processInstanceId 流程实例ID
     * @param deleteReason      删除原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessInstance(String processInstanceId, String deleteReason) {
        try {
            runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
            log.info("删除流程实例成功: processInstanceId={}", processInstanceId);
        } catch (Exception e) {
            log.error("删除流程实例失败: processInstanceId={}", processInstanceId, e);
            throw new BusinessException("删除流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 挂起流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void suspendProcessInstance(String processInstanceId) {
        try {
            runtimeService.suspendProcessInstanceById(processInstanceId);
            log.info("挂起流程实例成功: processInstanceId={}", processInstanceId);
        } catch (Exception e) {
            log.error("挂起流程实例失败: processInstanceId={}", processInstanceId, e);
            throw new BusinessException("挂起流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 激活流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateProcessInstance(String processInstanceId) {
        try {
            runtimeService.activateProcessInstanceById(processInstanceId);
            log.info("激活流程实例成功: processInstanceId={}", processInstanceId);
        } catch (Exception e) {
            log.error("激活流程实例失败: processInstanceId={}", processInstanceId, e);
            throw new BusinessException("激活流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 获取流程定义列表
     *
     * @return 流程定义列表
     */
    public List<ProcessDefinition> getProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .list();
    }

    /**
     * 获取流程定义
     *
     * @param processDefinitionKey 流程定义Key
     * @return 流程定义
     */
    public ProcessDefinition getProcessDefinition(String processDefinitionKey) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
        if (processDefinition == null) {
            throw new BusinessException("流程定义不存在");
        }
        return processDefinition;
    }

    /**
     * 获取流程实例
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例
     */
    public ProcessInstance getProcessInstance(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (processInstance == null) {
            throw new BusinessException("流程实例不存在");
        }
        return processInstance;
    }

    /**
     * 获取流程实例状态
     *
     * @param processInstanceId 流程实例ID
     * @return 状态 (running-运行中 suspended-已挂起 finished-已结束)
     */
    public String getProcessInstanceStatus(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance == null) {
            // 检查是否已结束
            var finishedInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (finishedInstance != null && finishedInstance.getEndTime() != null) {
                return "finished";
            }
            return "not_found";
        }

        return processInstance.isSuspended() ? "suspended" : "running";
    }
}
