package com.nexterp.platform.workflow.event;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 任务分配事件
 *
 * @author NextERP
 */
public record TaskAssignedEvent(
        /**
         * 任务ID
         */
        String taskId,

        /**
         * 任务名称
         */
        String taskName,

        /**
         * 任务定义Key
         */
        String taskDefinitionKey,

        /**
         * 分配人ID
         */
        String assignee,

        /**
         * 候选人/组
         */
        String candidates,

        /**
         * 流程实例ID
         */
        String processInstanceId,

        /**
         * 流程定义Key
         */
        String processDefinitionKey,

        /**
         * 业务Key
         */
        String businessKey,

        /**
         * 创建时间
         */
        LocalDateTime createTime,

        /**
         * 到期时间
         */
        LocalDateTime dueDate,

        /**
         * 任务变量
         */
        Map<String, Object> variables,

        /**
         * 租户ID
         */
        Long tenantId
) {
    public TaskAssignedEvent {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
