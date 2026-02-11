package com.nexterp.business.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.business.finance.application.dto.request.FinAccountCreateRequest;
import com.nexterp.business.finance.application.dto.request.FinAccountUpdateRequest;
import com.nexterp.platform.auth.application.dto.request.LoginRequest;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 科目控制器集成测试
 *
 * @author NextERP
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("科目 API 集成测试")
@Transactional
class FinAccountControllerTest {

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
            jwtToken = "Bearer test-token";
        }
    }

    @Test
    @DisplayName("创建科目 - 成功")
    void createAccount_Success() throws Exception {
        FinAccountCreateRequest request = new FinAccountCreateRequest();
        request.setAccountCode("1003");
        request.setAccountName("其他货币资金");
        request.setAccountType(1);
        request.setAccountDirection(1);
        request.setLevel(1);
        request.setStatus(1);
        request.setOpeningBalance(BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/finance/accounts")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("创建科目 - 科目编码已存在")
    void createAccount_CodeExists() throws Exception {
        FinAccountCreateRequest request = new FinAccountCreateRequest();
        request.setAccountCode("1001");
        request.setAccountName("测试科目");
        request.setAccountType(1);
        request.setAccountDirection(1);
        request.setLevel(1);
        request.setStatus(1);

        mockMvc.perform(post("/api/v1/finance/accounts")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("更新科目 - 成功")
    void updateAccount_Success() throws Exception {
        FinAccountUpdateRequest request = new FinAccountUpdateRequest();
        request.setAccountName("库存现金-更新");
        request.setStatus(1);

        mockMvc.perform(put("/api/v1/finance/accounts/1")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除科目 - 成功")
    void deleteAccount_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/finance/accounts/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取科目详情 - 成功")
    void getAccount_Success() throws Exception {
        mockMvc.perform(get("/api/v1/finance/accounts/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accountCode").exists())
                .andExpect(jsonPath("$.data.accountName").exists());
    }

    @Test
    @DisplayName("分页查询科目 - 成功")
    void listAccounts_Success() throws Exception {
        mockMvc.perform(post("/api/v1/finance/accounts/page")
                        .header("Authorization", jwtToken)
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("按科目类型查询 - 成功")
    void listAccountsByType_Success() throws Exception {
        mockMvc.perform(get("/api/v1/finance/accounts/type/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("无Token访问 - 未授权")
    void accessWithoutToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/finance/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("获取科目树 - 成功")
    void getAccountTree_Success() throws Exception {
        mockMvc.perform(get("/api/v1/finance/accounts/tree")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");
        request.setTenantId(0L);
        return request;
    }
}
