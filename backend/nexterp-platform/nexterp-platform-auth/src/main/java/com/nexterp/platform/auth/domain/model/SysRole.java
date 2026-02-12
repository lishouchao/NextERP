package com.nexterp.platform.auth.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体
 *
 * @author NextERP
 */
@Entity
@Table(name = "sys_role", indexes = {
    @Index(name = "idx_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_role_code", columnList = "roleCode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysRole extends TenantAwareEntity {

    /**
     * 角色ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色编码
     */
    @Column(name = "role_code", nullable = false, length = 50)
    private String roleCode;

    /**
     * 角色名称
     */
    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    /**
     * 角色排序
     */
    @Column(name = "role_sort")
    @Builder.Default
    private Integer roleSort = 0;

    /**
     * 角色状态 (0-禁用 1-正常)
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

    /**
     * 角色权限关联
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "sys_role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<SysPermission> permissions = new HashSet<>();
}
