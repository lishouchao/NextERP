package com.nexterp.platform.auth.security;

import com.nexterp.shared.security.properties.JwtProperties;
import com.nexterp.shared.security.context.UserContext;
import com.nexterp.shared.security.userdetails.UserInfo;
import com.nexterp.shared.security.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JWT 安全上下文仓储
 *
 * @author NextERP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecurityContextRepository implements SecurityContextRepository {

    private final HttpRequestResponseSecurityContextRepository delegate =
        new HttpRequestResponseSecurityContextRepository();

    private final JwtProperties jwtProperties;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public SecurityContext loadContext(HttpServletRequest request) {
        // 从请求中提取令牌
        String token = JwtUtils.extractToken(request, jwtProperties.getHeader(), jwtProperties.getTokenPrefix());

        if (StringUtils.hasText(token) && jwtTokenProvider.isTokenValid(token)) {
            // 从令牌中获取用户信息
            String username = jwtTokenProvider.getUsername(token);
            Long userId = jwtTokenProvider.getClaim(token, claims -> claims.get("userId", Long.class));
            Long tenantId = jwtTokenProvider.getClaim(token, claims -> claims.get("tenantId", Long.class));

            @SuppressWarnings("unchecked")
            Collection<String> roles = jwtTokenProvider.getClaim(token, claims -> claims.get("roles", Collection.class));

            @SuppressWarnings("unchecked")
            Collection<String> permissions = jwtTokenProvider.getClaim(token, claims -> claims.get("permissions", Collection.class));

            // 构建用户信息
            UserInfo userInfo = new UserInfo(userId, username, tenantId, roles, permissions);

            // 设置用户上下文
            UserContext.setUserInfo(userInfo);

            log.debug("Loaded security context for user: {}, tenant: {}", username, tenantId);
        }

        return delegate.loadContext(request);
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        delegate.saveContext(context, request, response);
    }
}
