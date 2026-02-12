package com.nexterp.platform.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应 VO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间 (秒)
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    /**
     * 用户权限列表
     */
    private List<String> permissions;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 用户菜单列表
     */
    private List<MenuInfo> menus;

    /**
     * 用户信息 VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        /**
         * 用户ID
         */
        private Long userId;

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
         * 头像URL
         */
        private String avatar;

        /**
         * 部门ID
         */
        private Long deptId;

        /**
         * 部门名称
         */
        private String deptName;
    }

    /**
     * 菜单信息 VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuInfo {
        /**
         * 菜单ID
         */
        private Long id;

        /**
         * 父菜单ID
         */
        private Long parentId;

        /**
         * 菜单名称
         */
        private String name;

        /**
         * 菜单类型 (M-目录 C-菜单 F-按钮)
         */
        private String type;

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
         * 图标
         */
        private String icon;

        /**
         * 排序
         */
        private Integer orderNum;

        /**
         * 是否可见
         */
        private Boolean visible;

        /**
         * 权限标识
         */
        private String permission;

        /**
         * 子菜单
         */
        private List<MenuInfo> children;
    }
}
