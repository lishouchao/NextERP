package com.nexterp.platform.workflow.listener;

import com.nexterp.platform.workflow.event.ProcessStartedEvent;
import com.nexterp.shared.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 流程启动监听器
 *
 * @author NextERP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessStartListener implements ExecutionListener {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notify(DelegateExecution execution) {
        try {
            String processInstanceId = execution.getProcessInstanceId();
            String processDefinitionKey = execution.getProcessDefinitionId();
            String businessKey = execution.getProcessInstanceBusinessKey();

            log.info("流程启动: processInstanceId={}, processDefinitionKey={}, businessKey={}",
                    processInstanceId, processDefinitionKey, businessKey);

            // 获取租户ID
            Long tenantId = getTenantId(execution);

            // 获取发起人
            String initiator = getInitiator(execution);

            // 获取流程定义名称
            String processDefinitionName = (String) execution.getVariable("processDefinitionName");
            if (processDefinitionName == null) {
                processDefinitionName = processDefinitionKey;
            }

            // 发布流程启动事件
            eventPublisher.publishEvent(new ProcessStartedEvent(
                    processInstanceId,
                    processDefinitionKey,
                    processDefinitionName,
                    businessKey,
                    initiator,
                    java.time.LocalDateTime.now(),
                    execution.getVariables(),
                    tenantId
            ));

        } catch (Exception e) {
            log.error("流程启动监听器处理失败", e);
        }
    }

    /**
     * 获取租户ID
     */
    private Long getTenantId(DelegateExecution execution) {
        Object tenantId = execution.getVariable("tenantId");
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
     * 获取发起人
     */
    private String getInitiator(DelegateExecution execution) {
        Object initiator = execution.getVariable("initiator");
        if (initiator != null) {
            return initiator.toString();
        }
        try {
            return String.valueOf(UserContext.getUserId());
        } catch (Exception e) {
            return "system";
        }
    }
}
