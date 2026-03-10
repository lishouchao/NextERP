package com.nexterp.shared.security.userdetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * 用户信息
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 角色列表
     */
    private Collection<String> roles;

    /**
     * 权限列表
     */
    private Collection<String> permissions;

    /**
     * 检查是否有权限
     *
     * @param permission 权限码
     * @return 是否有权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 检查是否有角色
     *
     * @param role 角色码
     * @return 是否有角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
