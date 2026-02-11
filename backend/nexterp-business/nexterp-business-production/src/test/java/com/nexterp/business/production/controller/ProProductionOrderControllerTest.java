package com.nexterp.business.production.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.business.production.application.dto.request.ProductionOrderCreateRequest;
import com.nexterp.business.production.application.dto.request.ProductionOrderUpdateRequest;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 生产工单控制器集成测试
 *
 * @author NextERP
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("生产工单 API 集成测试")
@Transactional
class ProProductionOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        try {
            LoginResponse response = authService.login(createLoginRequest());
            jwtToken = response.getAccessToken();
        } catch (Exception e) {
            jwtToken = "Bearer test-token";
        }
    }

    @Test
    @DisplayName("创建生产工单 - 成功")
    void createOrder_Success() throws Exception {
        ProductionOrderCreateRequest request = new ProductionOrderCreateRequest();
        request.setProductId(1L);
        request.setProductCode("PROD001");
        request.setProductName("产品A");
        request.setProductSpec("标准规格");
        request.setPlannedQty(new BigDecimal("100.00"));
        request.setPlannedStartDate(LocalDate.now().plusDays(1));
        request.setPlannedEndDate(LocalDate.now().plusDays(7));
        request.setPriority(1);
        request.setWorkshopId(1L);
        request.setWorkshopName("车间A");

        mockMvc.perform(post("/api/v1/production/orders")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("更新生产工单 - 成功")
    void updateOrder_Success() throws Exception {
        ProductionOrderUpdateRequest request = new ProductionOrderUpdateRequest();
        request.setPlannedQty(new BigDecimal("150.00"));
        request.setPriority(2);

        mockMvc.perform(put("/api/v1/production/orders/1")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("开工 - 成功")
    void startOrder_Success() throws Exception {
        mockMvc.perform(post("/api/v1/production/orders/1/start")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("完工 - 成功")
    void completeOrder_Success() throws Exception {
        mockMvc.perform(post("/api/v1/production/orders/1/complete")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("关闭工单 - 成功")
    void closeOrder_Success() throws Exception {
        mockMvc.perform(post("/api/v1/production/orders/1/close")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("暂停工单 - 成功")
    void suspendOrder_Success() throws Exception {
        mockMvc.perform(post("/api/v1/production/orders/1/suspend")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("恢复工单 - 成功")
    void resumeOrder_Success() throws Exception {
        mockMvc.perform(post("/api/v1/production/orders/1/resume")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除生产工单 - 成功")
    void deleteOrder_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/production/orders/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取工单详情 - 成功")
    void getOrder_Success() throws Exception {
        mockMvc.perform(get("/api/v1/production/orders/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").exists())
                .andExpect(jsonPath("$.data.productName").exists())
                .andExpect(jsonPath("$.data.plannedQty").exists());
    }

    @Test
    @DisplayName("分页查询工单 - 成功")
    void listOrders_Success() throws Exception {
        mockMvc.perform(post("/api/v1/production/orders/page")
                        .header("Authorization", jwtToken)
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("按产品查询工单 - 成功")
    void listOrdersByProduct_Success() throws Exception {
        mockMvc.perform(get("/api/v1/production/orders/product/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("按车间查询工单 - 成功")
    void listOrdersByWorkshop_Success() throws Exception {
        mockMvc.perform(get("/api/v1/production/orders/workshop/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("按状态查询工单 - 成功")
    void listOrdersByStatus_Success() throws Exception {
        mockMvc.perform(get("/api/v1/production/orders/status/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("无Token访问 - 未授权")
    void accessWithoutToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/production/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("获取工单进度 - 成功")
    void getOrderProgress_Success() throws Exception {
        mockMvc.perform(get("/api/v1/production/orders/1/progress")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.completionRate").exists());
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");
        request.setTenantId(0L);
        return request;
    }
}
