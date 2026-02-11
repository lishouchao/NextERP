package com.nexterp.platform.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.shared.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证失败处理器
 *
 * @author NextERP
 */
@Slf4j
@Component
public class AuthenticationFailureHandler implements org.springframework.security.web.authentication.AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;
    private final LoginAttemptService loginAttemptService;

    public AuthenticationFailureHandler(ObjectMapper objectMapper, LoginAttemptService loginAttemptService) {
        this.objectMapper = objectMapper;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.error("认证失败: {}", exception.getMessage());

        // 记录失败尝试
        String username = request.getParameter("username");
        if (username != null) {
            loginAttemptService.loginFailed(username);
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<Void> result = Result.unauthorized(exception.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
