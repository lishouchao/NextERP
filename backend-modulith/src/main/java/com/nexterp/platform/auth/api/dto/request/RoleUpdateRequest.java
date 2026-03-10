package com.nexterp.platform.auth.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色更新请求
 *
 * @author NextERP
 */
@Data
public class RoleUpdateRequest {

    /**
     * 角色编码
     */
    @Size(max = 50, message = "角色编码长度不能超过50")
    private String roleCode;

    /**
     * 角色名称
     */
    @Size(max = 100, message = "角色名称长度不能超过100")
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
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;
}
