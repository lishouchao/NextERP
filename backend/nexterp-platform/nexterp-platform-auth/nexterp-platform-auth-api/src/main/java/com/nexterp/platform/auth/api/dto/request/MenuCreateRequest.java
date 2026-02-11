package com.nexterp.platform.auth.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜单创建请求
 *
 * @author NextERP
 */
@Data
public class MenuCreateRequest {

    /**
     * 租户ID
     */
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 100, message = "菜单名称长度不能超过100")
    private String menuName;

    /**
     * 菜单类型 (M-目录 C-菜单 F-按钮)
     */
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 路由地址
     */
    @Size(max = 255, message = "路由地址长度不能超过255")
    private String path;

    /**
     * 组件路径
     */
    @Size(max = 255, message = "组件路径长度不能超过255")
    private String component;

    /**
     * 路由参数
     */
    private String query;

    /**
     * 是否为外链
     */
    private Boolean isFrame;

    /**
     * 是否缓存
     */
    private Boolean isCache;

    /**
     * 是否可见
     */
    private Boolean visible;

    /**
     * 图标
     */
    @Size(max = 100, message = "图标长度不能超过100")
    private String icon;

    /**
     * 权限标识
     */
    @Size(max = 100, message = "权限标识长度不能超过100")
    private String permission;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
