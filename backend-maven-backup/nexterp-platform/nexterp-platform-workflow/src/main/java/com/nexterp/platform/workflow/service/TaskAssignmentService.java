package com.nexterp.platform.workflow.service;

import com.nexterp.platform.workflow.domain.model.TaskAssignment;
import com.nexterp.platform.workflow.domain.repository.TaskAssignmentRepository;
import com.nexterp.platform.workflow.expression.ExpressionParser;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务分配服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskAssignmentService {

    private final TaskAssignmentRepository taskAssignmentRepository;
    private final ExpressionParser expressionParser;

    /**
     * 创建任务分配规则
     *
     * @param assignment 分配规则
     * @return 规则ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createAssignment(TaskAssignment assignment) {
        TaskAssignment saved = taskAssignmentRepository.save(assignment);
        log.info("创建任务分配规则: processKey={}, taskKey={}, type={}",
                assignment.getProcessKey(), assignment.getTaskKey(), assignment.getAssignmentType());
        return saved.getId();
    }

    /**
     * 更新任务分配规则
     *
     * @param id 规则ID
     * @param assignment 分配规则
     * @return 更新后的规则
     */
    @Transactional(rollbackFor = Exception.class)
    public TaskAssignment updateAssignment(Long id, TaskAssignment assignment) {
        TaskAssignment existing = taskAssignmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务分配规则不存在"));

        existing.setAssignmentType(assignment.getAssignmentType());
        existing.setAssignmentValue(assignment.getAssignmentValue());
        existing.setPriority(assignment.getPriority());
        existing.setEnabled(assignment.getEnabled());

        return taskAssignmentRepository.save(existing);
    }

    /**
     * 删除任务分配规则
     *
     * @param id 规则ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAssignment(Long id) {
        TaskAssignment assignment = taskAssignmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务分配规则不存在"));

        assignment.setIsDeleted(true);
        taskAssignmentRepository.save(assignment);
    }

    /**
     * 获取任务的分配规则
     *
     * @param processKey 流程Key
     * @param taskKey 任务Key
     * @param tenantId 租户ID
     * @return 分配规则列表
     */
    public List<TaskAssignment> getTaskAssignments(String processKey, String taskKey, Long tenantId) {
        return taskAssignmentRepository.findByTaskKey(processKey, taskKey, tenantId);
    }

    /**
     * 获取租户所有分配规则
     *
     * @param tenantId 租户ID
     * @return 分配规则列表
     */
    public List<TaskAssignment> getTenantAssignments(Long tenantId) {
        return taskAssignmentRepository.findByTenantId(tenantId);
    }

    /**
     * 根据分配规则计算任务候选人
     *
     * @param processKey 流程Key
     * @param taskKey 任务Key
     * @param tenantId 租户ID
     * @return 候选人列表
     */
    public List<String> calculateCandidates(String processKey, String taskKey, Long tenantId) {
        List<TaskAssignment> assignments = getTaskAssignments(processKey, taskKey, tenantId);

        return assignments.stream()
                .filter(TaskAssignment::getEnabled)
                .sorted((a, b) -> b.getPriority().compareTo(a.getPriority()))
                .flatMap(assignment -> calculateAssignmentValue(assignment).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 计算分配值
     *
     * @param assignment 分配规则
     * @return 候选人列表
     */
    private List<String> calculateAssignmentValue(TaskAssignment assignment) {
        String value = assignment.getAssignmentValue();
        if (value == null || value.isEmpty()) {
            return List.of();
        }

        return switch (assignment.getAssignmentType()) {
            case "user" -> List.of(value);
            case "role" -> List.of("ROLE_" + value);
            case "dept" -> List.of("DEPT_" + value);
            case "expression" -> {
                // 使用表达式解析器
                ExpressionParser.ExpressionContext context = ExpressionParser.ExpressionContext.builder()
                        .variables(Map.of())
                        .build();

                List<Long> assigneeIds = expressionParser.parseAssignmentExpression(value, context);
                yield assigneeIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
            }
            default -> List.of();
        };
    }
}
