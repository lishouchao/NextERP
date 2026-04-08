package com.nexterp.platform.workflow.listener;

import com.nexterp.platform.workflow.event.TaskCompletedEvent;
import com.nexterp.shared.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 任务完成监听器
 *
 * @author NextERP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCompleteListener implements TaskListener {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            String taskId = delegateTask.getId();
            String taskName = delegateTask.getName();
            String assignee = delegateTask.getAssignee();

            log.info("任务完成: taskId={}, taskName={}, assignee={}",
                    taskId, taskName, assignee);

            // 获取租户ID
            Long tenantId = getTenantId(delegateTask);

            // 获取审批意见
            String comment = (String) delegateTask.getVariable("comment");

            // 获取审批结果
            String approvalResult = (String) delegateTask.getVariable("approvalResult");

            // 获取开始时间
            Object startTimeObj = delegateTask.getVariable("taskCreateTime");
            java.time.LocalDateTime startTime = null;
            if (startTimeObj instanceof java.time.LocalDateTime) {
                startTime = (java.time.LocalDateTime) startTimeObj;
            }

            // 发布任务完成事件
            eventPublisher.publishEvent(new TaskCompletedEvent(
                    taskId,
                    taskName,
                    assignee,
                    delegateTask.getProcessInstanceId(),
                    delegateTask.getProcessDefinitionId(),
                    (String) delegateTask.getVariable("businessKey"),
                    startTime,
                    java.time.LocalDateTime.now(),
                    null,
                    comment,
                    approvalResult,
                    tenantId
            ));

        } catch (Exception e) {
            log.error("任务完成监听器处理失败", e);
        }
    }

    /**
     * 获取租户ID
     */
    private Long getTenantId(DelegateTask delegateTask) {
        Object tenantId = delegateTask.getVariable("tenantId");
        if (tenantId instanceof Long) {
            return (Long) tenantId;
        }
        if (tenantId instanceof String) {
            return Long.parseLong((String) tenantId);
        }
        try {
            return UserContext.getTenantId();
        } catch (Exception e) {
            return null;
        }
    }
}
