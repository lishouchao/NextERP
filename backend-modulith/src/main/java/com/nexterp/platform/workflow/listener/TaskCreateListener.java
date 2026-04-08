package com.nexterp.platform.workflow.listener;

import com.nexterp.platform.workflow.event.TaskAssignedEvent;
import com.nexterp.shared.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 任务创建监听器
 *
 * @author NextERP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCreateListener implements TaskListener {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            String taskId = delegateTask.getId();
            String taskName = delegateTask.getName();
            String assignee = delegateTask.getAssignee();

            log.info("任务创建: taskId={}, taskName={}, assignee={}",
                    taskId, taskName, assignee);

            // 获取租户ID
            Long tenantId = getTenantId(delegateTask);

            // 获取流程变量
            var variables = delegateTask.getVariables();

            // 发布任务分配事件
            eventPublisher.publishEvent(new TaskAssignedEvent(
                    taskId,
                    taskName,
                    delegateTask.getTaskDefinitionKey(),
                    assignee,
                    null,
                    delegateTask.getProcessInstanceId(),
                    delegateTask.getProcessDefinitionId(),
                    (String) variables.get("businessKey"),
                    toLocalDateTime(delegateTask.getCreateTime()),
                    toLocalDateTime(delegateTask.getDueDate()),
                    variables,
                    tenantId
            ));

        } catch (Exception e) {
            log.error("任务创建监听器处理失败", e);
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

    /**
     * 转换Date为LocalDateTime
     */
    private java.time.LocalDateTime toLocalDateTime(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
