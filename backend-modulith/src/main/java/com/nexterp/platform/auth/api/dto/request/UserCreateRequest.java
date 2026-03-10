package com.nexterp.platform.auth.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户创建请求
 *
 * @author NextERP
 */
@Data
public class UserCreateRequest {

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    /**
     * 真实姓名
     */
    @Size(max = 100, message = "真实姓名长度不能超过100")
    private String realName;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 性别 (0-女 1-男 2-未知)
     */
    private Integer gender;

    /**
     * 头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500")
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
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
}
