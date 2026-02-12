package com.nexterp.shared.data.context;

/**
 * 租户上下文
 *
 * @author NextERP
 */
public class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    /**
     * 设置租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * 获取租户ID
     *
     * @return 租户ID
     */
    public static Long getTenantId() {
        Long tenantId = TENANT_ID.get();
        return tenantId != null ? tenantId : 0L;
    }

    /**
     * 清除租户ID
     */
    public static void clear() {
        TENANT_ID.remove();
    }
}
