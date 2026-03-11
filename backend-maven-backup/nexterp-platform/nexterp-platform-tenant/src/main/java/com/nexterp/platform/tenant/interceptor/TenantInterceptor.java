package com.nexterp.platform.tenant.interceptor;

import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.data.context.TenantContext;
import com.nexterp.platform.tenant.application.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 租户拦截器
 *
 * @author NextERP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantService tenantService;

    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final String DEFAULT_TENANT_ID = "0";

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) throws Exception {
        // 从请求头获取租户ID
        String tenantIdHeader = request.getHeader(TENANT_HEADER);

        Long tenantId;
        if (tenantIdHeader != null && !tenantIdHeader.isEmpty()) {
            try {
                tenantId = Long.parseLong(tenantIdHeader);
            } catch (NumberFormatException e) {
                throw new BusinessException("无效的租户ID格式");
            }
        } else {
            // 使用默认租户
            tenantId = Long.parseLong(DEFAULT_TENANT_ID);
        }

        // 验证租户是否可用
        if (!tenantService.isTenantAvailable(tenantId)) {
            throw new BusinessException("租户不存在或已被禁用");
        }

        // 设置租户上下文
        TenantContext.setTenantId(tenantId);

        log.debug("设置租户上下文: tenantId={}", tenantId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        // 清除租户上下文
        TenantContext.clear();
        log.debug("清除租户上下文");
    }
}
