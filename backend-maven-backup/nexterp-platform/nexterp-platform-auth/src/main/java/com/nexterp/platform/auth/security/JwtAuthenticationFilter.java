package com.nexterp.platform.auth.security;

import com.nexterp.shared.security.properties.JwtProperties;
import com.nexterp.shared.security.context.UserContext;
import com.nexterp.shared.security.userdetails.UserInfo;
import com.nexterp.shared.security.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 *
 * @author NextERP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
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

                // 构建认证信息
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userInfo,
                    null,
                    permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 设置用户上下文
                UserContext.setUserInfo(userInfo);

                log.debug("Authenticated user: {}, tenant: {}", username, tenantId);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
