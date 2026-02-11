package com.nexterp.platform.workflow.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务分配规则实体
 *
 * @author NextERP
 */
@Entity
@Table(name = "wf_task_assignment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignment extends BaseEntity {

    /**
     * 规则ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id")
    private Long tenantId;

    /**
     * 流程定义Key
     */
    @Column(name = "process_key", length = 100)
    private String processKey;

    /**
     * 任务定义Key
     */
    @Column(name = "task_key", nullable = false, length = 100)
    private String taskKey;

    /**
     * 分配类型 (user-用户 role-角色 dept-部门 expression-表达式)
     */
    @Column(name = "assignment_type", nullable = false, length = 20)
    private String assignmentType;

    /**
     * 分配值 (用户ID、角色ID、部门ID或表达式)
     */
    @Column(name = "assignment_value", length = 500)
    private String assignmentValue;

    /**
     * 优先级
     */
    @Column(name = "priority")
    private Integer priority = 0;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;
}
