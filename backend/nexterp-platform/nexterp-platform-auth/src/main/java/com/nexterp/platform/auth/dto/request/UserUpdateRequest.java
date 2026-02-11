package com.nexterp.platform.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户更新请求
 *
 * @author NextERP
 */
@Data
public class UserUpdateRequest {

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
