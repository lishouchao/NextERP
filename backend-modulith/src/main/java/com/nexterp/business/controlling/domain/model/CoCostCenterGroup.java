package com.nexterp.business.controlling.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 成本中心组
 * 对标: SAP CCS_GRP
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_cost_center_group")
public class CoCostCenterGroup extends TenantAwareEntity {

    /**
     * 成本中心组ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 成本中心组代码
     */
    @Column(name = "group_code", nullable = false, length = 12)
    private String groupCode;

    /**
     * 成本中心组名称
     */
    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    /**
     * 父组ID
     */
    @Column(name = "parent_group_id")
    private Long parentGroupId;

    /**
     * 成本控制范围
     */
    @Column(name = "controlling_area", length = 4)
    private String controllingArea;

    /**
     * 层级
     */
    @Column(name = "hierarchy_level")
    private Integer hierarchyLevel;

    /**
     * 是否叶子节点
     */
    @Column(name = "is_leaf", nullable = false)
    @Builder.Default
    private Boolean isLeaf = true;

    /**
     * 描述
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 状态 (0-禁用 1-启用)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 子组列表 (不持久化)
     */
    @Transient
    private List<CoCostCenterGroup> children = new ArrayList<>();
}
