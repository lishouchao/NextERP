package com.nexterp.platform.workflow.event;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程启动事件
 *
 * @author NextERP
 */
public record ProcessStartedEvent(
        /**
         * 流程实例ID
         */
        String processInstanceId,

        /**
         * 流程定义Key
         */
        String processDefinitionKey,

        /**
         * 流程定义名称
         */
        String processDefinitionName,

        /**
         * 业务Key
         */
        String businessKey,

        /**
         * 发起人ID
         */
        String initiator,

        /**
         * 发起时间
         */
        LocalDateTime startTime,

        /**
         * 流程变量
         */
        Map<String, Object> variables,

        /**
         * 租户ID
         */
        Long tenantId
) {
    public ProcessStartedEvent {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }
}
