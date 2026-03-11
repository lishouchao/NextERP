package com.nexterp.platform.auth.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限更新请求
 *
 * @author NextERP
 */
@Data
public class PermissionUpdateRequest {

    /**
     * 权限编码 (格式: module:action:resource)
     */
    @Size(max = 100, message = "权限编码长度不能超过100")
    private String permissionCode;

    /**
     * 权限名称
     */
    @Size(max = 100, message = "权限名称长度不能超过100")
    private String permissionName;

    /**
     * 权限类型 (menu-菜单权限 button-按钮权限 data-数据权限)
     */
    private String permissionType;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 路由路径
     */
    @Size(max = 255, message = "路由路径长度不能超过255")
    private String path;

    /**
     * 组件路径
     */
    @Size(max = 255, message = "组件路径长度不能超过255")
    private String component;

    /**
     * 图标
     */
    @Size(max = 100, message = "图标长度不能超过100")
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否可见
     */
    private Boolean visible;

    /**
     * 状态 (0-禁用 1-正常)
     */
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
