package com.nexterp.platform.workflow.service;

import com.nexterp.platform.workflow.domain.model.TaskAssignment;
import com.nexterp.platform.workflow.domain.repository.TaskAssignmentRepository;
import com.nexterp.platform.workflow.expression.WorkflowExpressionParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 任务分配服务测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
class TaskAssignmentServiceTest {

    @Mock
    private TaskAssignmentRepository taskAssignmentRepository;

    @Mock
    private WorkflowExpressionParser expressionParser;

    @InjectMocks
    private TaskAssignmentService taskAssignmentService;

    @Test
    @DisplayName("创建任务分配规则")
    void testCreateAssignment() {
        // Given
        TaskAssignment assignment = TaskAssignment.builder()
                .tenantId(1L)
                .processKey("purchaseApproval")
                .taskKey("managerApproval")
                .assignmentType("user")
                .assignmentValue("123")
                .priority(0)
                .enabled(true)
                .build();

        when(taskAssignmentRepository.save(any(TaskAssignment.class)))
                .thenAnswer(invocation -> {
                    TaskAssignment saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        // When
        Long id = taskAssignmentService.createAssignment(assignment);

        // Then
        assertThat(id).isEqualTo(1L);
        verify(taskAssignmentRepository).save(assignment);
    }

    @Test
    @DisplayName("更新任务分配规则")
    void testUpdateAssignment() {
        // Given
        TaskAssignment existing = TaskAssignment.builder()
                .id(1L)
                .tenantId(1L)
                .processKey("purchaseApproval")
                .taskKey("managerApproval")
                .assignmentType("user")
                .assignmentValue("123")
                .priority(0)
                .enabled(true)
                .build();

        TaskAssignment update = TaskAssignment.builder()
                .assignmentType("role")
                .assignmentValue("manager")
                .priority(1)
                .enabled(false)
                .build();

        when(taskAssignmentRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(taskAssignmentRepository.save(any(TaskAssignment.class)))
                .thenReturn(existing);

        // When
        TaskAssignment result = taskAssignmentService.updateAssignment(1L, update);

        // Then
        assertThat(result.getAssignmentType()).isEqualTo("role");
        assertThat(result.getAssignmentValue()).isEqualTo("manager");
        assertThat(result.getPriority()).isEqualTo(1);
        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("删除任务分配规则")
    void testDeleteAssignment() {
        // Given
        TaskAssignment assignment = TaskAssignment.builder()
                .id(1L)
                .tenantId(1L)
                .processKey("purchaseApproval")
                .taskKey("managerApproval")
                .assignmentType("user")
                .assignmentValue("123")
                .enabled(true)
                .isDeleted(false)
                .build();

        when(taskAssignmentRepository.findById(1L))
                .thenReturn(Optional.of(assignment));
        when(taskAssignmentRepository.save(any(TaskAssignment.class)))
                .thenReturn(assignment);

        // When
        taskAssignmentService.deleteAssignment(1L);

        // Then
        assertThat(assignment.getIsDeleted()).isTrue();
        verify(taskAssignmentRepository).save(assignment);
    }

    @Test
    @DisplayName("获取租户所有分配规则")
    void testGetTenantAssignments() {
        // Given
        List<TaskAssignment> assignments = List.of(
                TaskAssignment.builder().id(1L).tenantId(1L).build(),
                TaskAssignment.builder().id(2L).tenantId(1L).build()
        );

        when(taskAssignmentRepository.findByTenantId(1L))
                .thenReturn(assignments);

        // When
        List<TaskAssignment> result = taskAssignmentService.getTenantAssignments(1L);

        // Then
        assertThat(result).hasSize(2);
        verify(taskAssignmentRepository).findByTenantId(1L);
    }
}
