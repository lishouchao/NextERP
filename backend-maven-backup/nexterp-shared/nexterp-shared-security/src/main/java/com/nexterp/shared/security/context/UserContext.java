package com.nexterp.shared.security.context;

import com.nexterp.shared.security.userdetails.UserInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文
 * 使用 ThreadLocal 存储当前请求的用户信息
 *
 * @author NextERP
 */
@Slf4j
public class UserContext {

    private static final ThreadLocal<UserInfo> USER_CONTEXT = new ThreadLocal<>();

    /**
     * 设置用户信息
     *
     * @param userInfo 用户信息
     */
    public static void setUserInfo(UserInfo userInfo) {
        USER_CONTEXT.set(userInfo);
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    public static UserInfo getUserInfo() {
        UserInfo userInfo = USER_CONTEXT.get();
        if (userInfo == null) {
            log.warn("User context is null");
        }
        return userInfo;
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getUsername() : null;
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID
     */
    public static Long getTenantId() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getTenantId() : null;
    }

    /**
     * 清除用户上下文
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }
}
