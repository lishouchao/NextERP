package com.nexterp.platform.workflow.service;

import com.nexterp.NexterpApplication;
import com.nexterp.platform.workflow.dto.StartProcessRequest;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 工作流服务测试
 *
 * @author NextERP
 */
@ApplicationModuleTest
@ActiveProfiles("test")
class WorkflowServiceTest {

    @Autowired
    private WorkflowService workflowService;

    @MockBean
    private RuntimeService runtimeService;

    @MockBean
    private TaskService taskService;

    @Test
    @DisplayName("启动流程实例")
    void testStartProcess() {
        // Given
        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getId()).thenReturn("test-instance-id");
        when(mockInstance.getProcessDefinitionId()).thenReturn("testProcess:1");
        when(mockInstance.getBusinessKey()).thenReturn("BIZ-001");

        when(runtimeService.startProcessInstanceByKey(
                anyString(), anyString(), anyMap()))
                .thenReturn(mockInstance);

        // When
        String processInstanceId = workflowService.startProcess(
                "testProcess",
                "BIZ-001",
                new HashMap<>(),
                "user1"
        );

        // Then
        assertThat(processInstanceId).isEqualTo("test-instance-id");
        verify(runtimeService).startProcessInstanceByKey(
                eq("testProcess"), eq("BIZ-001"), anyMap());
    }

    @Test
    @DisplayName("完成任务")
    void testCompleteTask() {
        // Given
        String taskId = "task-001";
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);

        doNothing().when(taskService).complete(eq(taskId), anyMap());

        // When
        workflowService.completeTask(taskId, variables);

        // Then
        verify(taskService).complete(eq(taskId), anyMap());
    }

    @Test
    @DisplayName("获取用户待办任务")
    void testGetUserTasks() {
        // Given
        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn("task-001");
        when(mockTask.getName()).thenReturn("审批任务");
        when(mockTask.getAssignee()).thenReturn("user1");
        when(mockTask.getCreateTime()).thenReturn(new java.util.Date());
        when(mockTask.getProcessInstanceId()).thenReturn("proc-001");

        List<Task> mockTasks = List.of(mockTask);
        when(taskService.createTaskQuery()).thenReturn(mock(org.flowable.task.api.TaskQuery.class));
        when(taskService.createTaskQuery().taskAssignee(anyString())).thenReturn(mock(org.flowable.task.api.TaskQuery.class));
        when(taskService.createTaskQuery().taskAssignee(anyString()).active()).thenReturn(mock(org.flowable.task.api.TaskQuery.class));
        when(taskService.createTaskQuery().taskAssignee(anyString()).active().orderByTaskCreateTime()).thenReturn(mock(org.flowable.task.api.TaskQuery.class));
        when(taskService.createTaskQuery().taskAssignee(anyString()).active().orderByTaskCreateTime().desc()).thenReturn(mock(org.flowable.task.api.TaskQuery.class));
        when(taskService.createTaskQuery().taskAssignee(anyString()).active().orderByTaskCreateTime().desc().list()).thenReturn(mockTasks);

        // When
        List<Task> tasks = workflowService.getUserTasks("user1");

        // Then
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getId()).isEqualTo("task-001");
    }
}
