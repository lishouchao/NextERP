package com.nexterp.platform.auth.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 菜单实体
 *
 * @author NextERP
 */
@Entity
@Table(name = "sys_menu", indexes = {
    @Index(name = "idx_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_parent_id", columnList = "parentId")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysMenu extends TenantAwareEntity {

    /**
     * 菜单ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 父菜单ID
     */
    @Column(name = "parent_id", insertable = false, updatable = false)
    private Long parentId;

    /**
     * 父菜单 (用于JPA关联)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private SysMenu parent;

    /**
     * 菜单名称
     */
    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    /**
     * 菜单类型 (M-目录 C-菜单 F-按钮)
     */
    @Column(name = "menu_type", nullable = false, length = 1)
    private String menuType;

    /**
     * 显示顺序
     */
    @Column(name = "order_num")
    @Builder.Default
    private Integer orderNum = 0;

    /**
     * 路由地址
     */
    @Column(name = "path", length = 255)
    private String path;

    /**
     * 组件路径
     */
    @Column(name = "component", length = 255)
    private String component;

    /**
     * 路由参数
     */
    @Column(name = "query", columnDefinition = "TEXT")
    private String query;

    /**
     * 是否为外链
     */
    @Column(name = "is_frame")
    @Builder.Default
    private Boolean isFrame = false;

    /**
     * 是否缓存
     */
    @Column(name = "is_cache")
    @Builder.Default
    private Boolean isCache = false;

    /**
     * 菜单状态 (0-禁用 1-正常)
     */
    @Column(name = "visible", nullable = false)
    @Builder.Default
    private Boolean visible = true;

    /**
     * 菜单图标
     */
    @Column(name = "icon", length = 100)
    private String icon;

    /**
     * 权限标识
     */
    @Column(name = "permission", length = 100)
    private String permission;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * 子菜单
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @OrderBy("orderNum")
    @Builder.Default
    private Set<SysMenu> children = new HashSet<>();
}
