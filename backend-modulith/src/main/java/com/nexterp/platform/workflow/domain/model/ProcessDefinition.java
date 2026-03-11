package com.nexterp.platform.workflow.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 流程定义实体
 *
 * @author NextERP
 */
@Entity
@Table(name = "wf_process_definition")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDefinition extends BaseEntity {

    /**
     * 定义ID
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
     * 流程Key
     */
    @Column(name = "process_key", nullable = false, length = 100)
    private String processKey;

    /**
     * 流程名称
     */
    @Column(name = "process_name", nullable = false, length = 100)
    private String processName;

    /**
     * 流程版本
     */
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    /**
     * 流程描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * BPMN XML
     */
    @Column(name = "bpmn_xml", columnDefinition = "TEXT")
    private String bpmnXml;

    /**
     * 流程分类
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * 状态 (0-草稿 1-发布 2-已归档)
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 发布时间
     */
    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
