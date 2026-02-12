package com.nexterp.platform.auth.api.dto.request;

import lombok.Data;

/**
 * 用户查询请求
 *
 * @author NextERP
 */
@Data
public class UserQueryRequest {

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * 真实姓名（模糊查询）
     */
    private String realName;

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户类型
     */
    private Integer userType;
}
