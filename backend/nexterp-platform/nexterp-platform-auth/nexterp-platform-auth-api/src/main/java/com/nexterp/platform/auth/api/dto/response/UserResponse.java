package com.nexterp.platform.auth.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别 (0-女 1-男 2-未知)
     */
    private Integer gender;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 用户状态 (0-禁用 1-正常)
     */
    private Integer status;

    /**
     * 用户类型 (0-系统用户 1-租户用户)
     */
    private Integer userType;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 密码最后修改时间
     */
    private LocalDateTime pwdUpdateTime;

    /**
     * 账号过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;

    /**
     * 角色名称列表
     */
    private List<String> roleNames;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
