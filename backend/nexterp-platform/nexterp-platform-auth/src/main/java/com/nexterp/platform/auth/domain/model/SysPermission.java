package com.nexterp.platform.auth.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 权限实体
 *
 * @author NextERP
 */
@Entity
@Table(name = "sys_permission", indexes = {
    @Index(name = "idx_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_permission_code", columnList = "permissionCode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysPermission extends TenantAwareEntity {

    /**
     * 权限ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 权限编码 (格式: module:action:resource)
     * 示例: finance:voucher:view
     */
    @Column(name = "permission_code", nullable = false, length = 100)
    private String permissionCode;

    /**
     * 权限名称
     */
    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;

    /**
     * 权限类型 (menu-菜单权限 button-按钮权限 data-数据权限)
     */
    @Column(name = "permission_type", length = 20)
    @Builder.Default
    private String permissionType = "button";

    /**
     * 父权限ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 权限路径
     */
    @Column(name = "path", length = 255)
    private String path;

    /**
     * 组件路径
     */
    @Column(name = "component", length = 255)
    private String component;

    /**
     * 图标
     */
    @Column(name = "icon", length = 100)
    private String icon;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 是否可见
     */
    @Column(name = "visible")
    @Builder.Default
    private Boolean visible = true;

    /**
     * 状态 (0-禁用 1-正常)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 是否删除
     */
    @Override
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
