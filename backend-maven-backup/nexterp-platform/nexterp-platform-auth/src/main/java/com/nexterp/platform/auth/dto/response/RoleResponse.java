package com.nexterp.platform.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色响应
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 排序
     */
    private Integer roleSort;

    /**
     * 状态 (0-禁用 1-正常)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;

    /**
     * 权限编码列表
     */
    private List<String> permissionCodes;

    /**
     * 关联用户数量
     */
    private Integer userCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
