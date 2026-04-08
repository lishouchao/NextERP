package com.nexterp.platform.workflow.event;

import java.time.LocalDateTime;

/**
 * 流程取消事件
 *
 * @author NextERP
 */
public record ProcessCancelledEvent(
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
         * 取消时间
         */
        LocalDateTime cancelTime,

        /**
         * 取消原因
         */
        String cancelReason,

        /**
         * 取消人ID
         */
        String cancelledBy,

        /**
         * 租户ID
         */
        Long tenantId
) {
    public ProcessCancelledEvent {
        if (cancelTime == null) {
            cancelTime = LocalDateTime.now();
        }
    }
}
