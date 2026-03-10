package com.nexterp.platform.auth.infrastructure.security;

import com.nexterp.shared.security.properties.JwtProperties;
import com.nexterp.shared.security.context.UserContext;
import com.nexterp.shared.security.userdetails.UserInfo;
import com.nexterp.shared.security.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * JWT 安全上下文仓储
 *
 * @author NextERP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecurityContextRepository implements SecurityContextRepository {

    private final JwtProperties jwtProperties;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @SuppressWarnings("deprecation")
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        return loadContextInternal(requestResponseHolder.getRequest());
    }

    @Override
    public DeferredSecurityContext loadDeferredContext(HttpServletRequest request) {
        Supplier<SecurityContext> supplier = () -> loadContextInternal(request);
        return new DeferredSecurityContext() {
            private SecurityContext securityContext;
            private boolean initialized = false;

            @Override
            public SecurityContext get() {
                if (!initialized) {
                    securityContext = supplier.get();
                    initialized = true;
                }
                return securityContext;
            }

            @Override
            public boolean isGenerated() {
                return initialized && securityContext != null;
            }
        };
    }

    /**
     * 加载安全上下文
     */
    @SuppressWarnings("unchecked")
    private SecurityContext loadContextInternal(HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // 从请求中提取令牌
        String token = JwtUtils.extractToken(request, jwtProperties.getHeader(), jwtProperties.getTokenPrefix());

        if (StringUtils.hasText(token) && jwtTokenProvider.isTokenValid(token)) {
            try {
                // 从令牌中获取用户信息
                String username = jwtTokenProvider.getUsername(token);
                Long userId = jwtTokenProvider.getClaim(token, claims -> claims.get("userId", Long.class));
                Long tenantId = jwtTokenProvider.getClaim(token, claims -> claims.get("tenantId", Long.class));

                Collection<String> roles = jwtTokenProvider.getClaim(token, claims -> claims.get("roles", Collection.class));
                Collection<String> permissions = jwtTokenProvider.getClaim(token, claims -> claims.get("permissions", Collection.class));

                // 构建用户信息
                UserInfo userInfo = UserInfo.builder()
                        .userId(userId)
                        .username(username)
                        .tenantId(tenantId)
                        .roles(roles)
                        .permissions(permissions)
                        .build();

                // 设置用户上下文
                UserContext.setUserInfo(userInfo);

                log.debug("Loaded security context for user: {}, tenant: {}", username, tenantId);
            } catch (Exception e) {
                log.warn("Failed to load security context from token: {}", e.getMessage());
            }
        }

        return context;
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        // JWT 无状态，不需要保存
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        String token = JwtUtils.extractToken(request, jwtProperties.getHeader(), jwtProperties.getTokenPrefix());
        return StringUtils.hasText(token);
    }
}
