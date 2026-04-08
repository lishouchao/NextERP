package com.nexterp.platform.workflow.event;

import java.time.LocalDateTime;

/**
 * 任务完成事件
 *
 * @author NextERP
 */
public record TaskCompletedEvent(
        /**
         * 任务ID
         */
        String taskId,

        /**
         * 任务名称
         */
        String taskName,

        /**
         * 处理人ID
         */
        String assignee,

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
         * 开始时间
         */
        LocalDateTime startTime,

        /**
         * 完成时间
         */
        LocalDateTime endTime,

        /**
         * 持续时间(毫秒)
         */
        Long duration,

        /**
         * 审批意见
         */
        String comment,

        /**
         * 审批结果 (approved-通过 rejected-驳回)
         */
        String approvalResult,

        /**
         * 租户ID
         */
        Long tenantId
) {
    public TaskCompletedEvent {
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        if (duration == null && startTime != null && endTime != null) {
            duration = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }
}
