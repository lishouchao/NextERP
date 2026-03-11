package com.nexterp.platform.auth.api.dto.request;

import lombok.Data;

/**
 * 权限查询请求
 *
 * @author NextERP
 */
@Data
public class PermissionQueryRequest {

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 权限编码（模糊查询）
     */
    private String permissionCode;

    /**
     * 权限名称（模糊查询）
     */
    private String permissionName;

    /**
     * 权限类型
     */
    private String permissionType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 父级权限ID (0表示查询顶级权限)
     */
    private Long parentId;
}
