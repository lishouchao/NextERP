package com.nexterp.platform.auth.infrastructure.security;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.shared.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证成功处理器
 *
 * @author NextERP
 */
@Slf4j
@Component
public class AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final LoginAttemptService loginAttemptService;

    public AuthenticationSuccessHandler(ObjectMapper objectMapper, LoginAttemptService loginAttemptService) {
        this.objectMapper = objectMapper;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("认证成功: user={}", authentication.getName());

        // 清除失败尝试记录
        String username = authentication.getName();
        loginAttemptService.loginSucceeded(username);

        // 清除session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<Void> result = Result.<Void>builder()
                .code(200)
                .message("登录成功")
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
