package com.nexterp.platform.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.platform.auth.application.dto.request.LoginRequest;
import com.nexterp.platform.auth.application.dto.request.UserCreateRequest;
import com.nexterp.platform.auth.application.dto.response.LoginResponse;
import com.nexterp.platform.auth.application.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

/**
 * AuthController API 集成测试
 *
 * @author NextERP
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("认证授权 API 集成测试")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        // 登录获取Token
        try {
            LoginResponse response = authService.login(createLoginRequest());
            jwtToken = response.getAccessToken();
        } catch (Exception e) {
            // 如果测试环境没有数据，使用模拟token
            jwtToken = "Bearer test-token";
        }
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void login_Success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createLoginRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void login_InvalidPassword() throws Exception {
        LoginRequest request = createLoginRequest();
        request.setPassword("wrong_password");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("创建用户 - 需要")
    void createUser_RequiresAuth() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRealName("新用户");

        mockMvc.perform(post("/api/v1/users")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取用户信息 - 成功")
    void getUserInfo_Success() throws Exception {
        mockMvc.perform(get("/api/v1/users/1")
                .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("分页查询用户 - 成功")
    void listUsers_Success() throws Exception {
        mockMvc.perform(post("/api/v1/users/page")
                .header("Authorization", jwtToken)
                .param("tenantId", "1")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("无Token访问 - 未授权")
    void accessWithoutToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");
        request.setTenantId(0L);
        return request;
    }
}
