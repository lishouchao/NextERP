package com.nexterp.platform.workflow.event;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程完成事件
 *
 * @author NextERP
 */
public record ProcessCompletedEvent(
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
         * 发起人ID
         */
        String initiator,

        /**
         * 开始时间
         */
        LocalDateTime startTime,

        /**
         * 结束时间
         */
        LocalDateTime endTime,

        /**
         * 持续时间(毫秒)
         */
        Long duration,

        /**
         * 流程变量
         */
        Map<String, Object> variables,

        /**
         * 租户ID
         */
        Long tenantId
) {
    public ProcessCompletedEvent {
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        if (duration == null && startTime != null && endTime != null) {
            duration = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }
}
