package com.nexterp.business.hrm.service;

import com.nexterp.business.hrm.application.dto.request.EmployeeCreateRequest;
import com.nexterp.business.hrm.application.dto.request.EmployeeUpdateRequest;
import com.nexterp.business.hrm.domain.model.HrmEmployee;
import com.nexterp.business.hrm.domain.repository.HrmEmployeeRepository;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 员工服务单元测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("员工服务单元测试")
class HrmEmployeeServiceTest {

    @Mock
    private HrmEmployeeRepository employeeRepository;

    @InjectMocks
    private HrmEmployeeService employeeService;

    private HrmEmployee testEmployee;
    private EmployeeCreateRequest createRequest;
    private EmployeeUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testEmployee = HrmEmployee.builder()
                .id(1L)
                .tenantId(0L)
                .employeeNo("EMP001")
                .employeeName("张三")
                .gender(1)
                .hireDate(LocalDate.now())
                .departmentId(1L)
                .departmentName("研发部")
                .positionId(1L)
                .positionName("开发工程师")
                .workStatus(1)
                .status(1)
                .baseSalary(new BigDecimal("5000.00"))
                .build();

        createRequest = new EmployeeCreateRequest();
        createRequest.setEmployeeNo("EMP002");
        createRequest.setEmployeeName("李四");
        createRequest.setGender(1);
        createRequest.setHireDate(LocalDate.now());
        createRequest.setDepartmentId(1L);
        createRequest.setDepartmentName("研发部");
        createRequest.setPositionId(1L);
        createRequest.setPositionName("开发工程师");
        createRequest.setWorkStatus(1);
        createRequest.setStatus(1);

        updateRequest = new EmployeeUpdateRequest();
        updateRequest.setEmployeeName("张三-更新");
        updateRequest.setDepartmentId(2L);
        updateRequest.setDepartmentName("销售部");
    }

    @Test
    @DisplayName("创建员工 - 成功")
    void createEmployee_Success() {
        when(employeeRepository.existsByEmployeeNoAndTenantIdAndIsDeletedFalse(anyString(), anyLong()))
                .thenReturn(false);
        when(employeeRepository.save(any(HrmEmployee.class))).thenReturn(testEmployee);

        Long employeeId = employeeService.createEmployee(createRequest, 0L);

        assertThat(employeeId).isEqualTo(1L);
        verify(employeeRepository, times(1)).save(any(HrmEmployee.class));
    }

    @Test
    @DisplayName("创建员工 - 员工编号已存在")
    void createEmployee_EmployeeNoExists() {
        when(employeeRepository.existsByEmployeeNoAndTenantIdAndIsDeletedFalse(anyString(), anyLong()))
                .thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(createRequest, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("员工编号已存在");

        verify(employeeRepository, never()).save(any(HrmEmployee.class));
    }

    @Test
    @DisplayName("更新员工 - 成功")
    void updateEmployee_Success() {
        when(employeeRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByEmployeeNoAndTenantIdAndIsDeletedFalseAndIdNot(
                anyString(), anyLong(), anyLong())).thenReturn(false);
        when(employeeRepository.save(any(HrmEmployee.class))).thenReturn(testEmployee);

        employeeService.updateEmployee(1L, updateRequest, 0L);

        verify(employeeRepository, times(1)).save(any(HrmEmployee.class));
    }

    @Test
    @DisplayName("更新员工 - 员工不存在")
    void updateEmployee_NotFound() {
        when(employeeRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, updateRequest, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("员工不存在");

        verify(employeeRepository, never()).save(any(HrmEmployee.class));
    }

    @Test
    @DisplayName("删除员工 - 成功")
    void deleteEmployee_Success() {
        when(employeeRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testEmployee));

        employeeService.deleteEmployee(1L, 0L);

        verify(employeeRepository, times(1)).save(any(HrmEmployee.class));
    }

    @Test
    @DisplayName("获取员工详情 - 成功")
    void getEmployeeById_Success() {
        when(employeeRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testEmployee));

        HrmEmployee employee = employeeService.getEmployeeById(1L, 0L);

        assertThat(employee).isNotNull();
        assertThat(employee.getEmployeeNo()).isEqualTo("EMP001");
        assertThat(employee.getEmployeeName()).isEqualTo("张三");
        assertThat(employee.getGenderName()).isEqualTo("男");
    }

    @Test
    @DisplayName("获取员工详情 - 不存在")
    void getEmployeeById_NotFound() {
        when(employeeRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(1L, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("员工不存在");
    }

    @Test
    @DisplayName("检查员工是否在职 - 在职")
    void isEmployeeActive_Active() {
        when(employeeRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testEmployee));

        boolean isActive = employeeService.isEmployeeActive(1L, 0L);

        assertThat(isActive).isTrue();
    }

    @Test
    @DisplayName("检查员工是否在职 - 离职")
    void isEmployeeActive_Inactive() {
        testEmployee.setWorkStatus(3); // 离职
        when(employeeRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testEmployee));

        boolean isActive = employeeService.isEmployeeActive(1L, 0L);

        assertThat(isActive).isFalse();
    }
}
