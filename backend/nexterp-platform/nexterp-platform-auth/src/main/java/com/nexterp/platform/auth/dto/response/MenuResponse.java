package com.nexterp.platform.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单响应
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型 (M-目录 C-菜单 F-按钮)
     */
    private String menuType;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
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
    private String icon;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 子菜单列表
     */
    private List<MenuResponse> children;
}
