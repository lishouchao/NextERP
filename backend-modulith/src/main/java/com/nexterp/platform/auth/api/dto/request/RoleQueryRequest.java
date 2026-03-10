package com.nexterp.platform.auth.api.dto.request;

import lombok.Data;

/**
 * 角色查询请求
 *
 * @author NextERP
 */
@Data
public class RoleQueryRequest {

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 角色编码（模糊查询）
     */
    private String roleCode;

    /**
     * 角色名称（模糊查询）
     */
    private String roleName;

    /**
     * 状态
     */
    private Integer status;
}
