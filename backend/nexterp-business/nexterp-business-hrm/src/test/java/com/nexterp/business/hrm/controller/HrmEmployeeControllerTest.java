package com.nexterp.business.hrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.business.hrm.application.dto.request.EmployeeCreateRequest;
import com.nexterp.business.hrm.application.dto.request.EmployeeUpdateRequest;
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
 * 员工控制器集成测试
 *
 * @author NextERP
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("员工 API 集成测试")
@Transactional
class HrmEmployeeControllerTest {

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
    @DisplayName("创建员工 - 成功")
    void createEmployee_Success() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setEmployeeNo("EMP003");
        request.setEmployeeName("王五");
        request.setGender(1);
        request.setHireDate(LocalDate.now());
        request.setDepartmentId(1L);
        request.setDepartmentName("研发部");
        request.setPositionId(1L);
        request.setPositionName("开发工程师");
        request.setWorkStatus(1);
        request.setStatus(1);
        request.setMobile("13800138000");
        request.setEmail("wangwu@example.com");

        mockMvc.perform(post("/api/v1/hrm/employees")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("创建员工 - 员工编号已存在")
    void createEmployee_EmployeeNoExists() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setEmployeeNo("EMP001");
        request.setEmployeeName("测试员工");
        request.setGender(1);
        request.setHireDate(LocalDate.now());
        request.setWorkStatus(1);
        request.setStatus(1);

        mockMvc.perform(post("/api/v1/hrm/employees")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("更新员工 - 成功")
    void updateEmployee_Success() throws Exception {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        request.setEmployeeName("张三-更新");
        request.setMobile("13900139000");
        request.setEmail("zhangsan-updated@example.com");

        mockMvc.perform(put("/api/v1/hrm/employees/1")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除员工 - 成功")
    void deleteEmployee_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/hrm/employees/2")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取员工详情 - 成功")
    void getEmployee_Success() throws Exception {
        mockMvc.perform(get("/api/v1/hrm/employees/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.employeeNo").exists())
                .andExpect(jsonPath("$.data.employeeName").exists())
                .andExpect(jsonPath("$.data.genderName").exists());
    }

    @Test
    @DisplayName("分页查询员工 - 成功")
    void listEmployees_Success() throws Exception {
        mockMvc.perform(post("/api/v1/hrm/employees/page")
                        .header("Authorization", jwtToken)
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("按部门查询员工 - 成功")
    void listEmployeesByDepartment_Success() throws Exception {
        mockMvc.perform(get("/api/v1/hrm/employees/department/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("按岗位查询员工 - 成功")
    void listEmployeesByPosition_Success() throws Exception {
        mockMvc.perform(get("/api/v1/hrm/employees/position/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("无Token访问 - 未授权")
    void accessWithoutToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/hrm/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("查询在职员工 - 成功")
    void listActiveEmployees_Success() throws Exception {
        mockMvc.perform(get("/api/v1/hrm/employees/active")
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
