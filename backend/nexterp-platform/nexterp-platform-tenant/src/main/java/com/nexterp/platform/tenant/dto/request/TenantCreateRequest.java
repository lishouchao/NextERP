package com.nexterp.platform.tenant.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 租户创建请求
 *
 * @author NextERP
 */
@Data
public class TenantCreateRequest {

    /**
     * 租户编码
     */
    @NotBlank(message = "租户编码不能为空")
    @Size(min = 2, max = 50, message = "租户编码长度必须在2-50之间")
    private String tenantCode;

    /**
     * 租户名称
     */
    @NotBlank(message = "租户名称不能为空")
    @Size(max = 100, message = "租户名称长度不能超过100")
    private String tenantName;

    /**
     * 联系人
     */
    @Size(max = 50, message = "联系人长度不能超过50")
    private String contactName;

    /**
     * 联系电话
     */
    @Size(max = 20, message = "联系电话长度不能超过20")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Email(message = "联系邮箱格式不正确")
    @Size(max = 100, message = "联系邮箱长度不能超过100")
    private String contactEmail;

    /**
     * 租户地址
     */
    @Size(max = 255, message = "租户地址长度不能超过255")
    private String address;

    /**
     * 状态 (0-禁用 1-正常)
     */
    private Integer status;

    /**
     * 过期时间
     */
    private java.time.LocalDateTime expireTime;

    /**
     * 最大用户数
     */
    private Integer maxUsers;

    /**
     * 最大存储空间(MB)
     */
    private Long maxStorage;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    /**
     * 租户配置
     */
    private String config;
}
