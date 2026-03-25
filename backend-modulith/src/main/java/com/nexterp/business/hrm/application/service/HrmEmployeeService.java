package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrmEmployee;
import com.nexterp.business.hrm.domain.repository.HrmEmployeeRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrmEmployeeService {

    private final HrmEmployeeRepository employeeRepository;

    /**
     * 创建员工
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createEmployee(HrmEmployee employee) {
        // 检查员工编号是否已存在
        if (employeeRepository.existsByEmployeeNoAndTenantIdAndIsDeletedFalse(
                employee.getEmployeeNo(), employee.getTenantId())) {
            throw new BusinessException("员工编号已存在: " + employee.getEmployeeNo());
        }

        // 生成员工编号
        if (employee.getEmployeeNo() == null || employee.getEmployeeNo().isEmpty()) {
            employee.setEmployeeNo(generateEmployeeNo());
        }

        // 设置默认值
        if (employee.getWorkStatus() == null) {
            employee.setWorkStatus(2); // 试用期
        }
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        if (employee.getBaseSalary() == null) {
            employee.setBaseSalary(BigDecimal.ZERO);
        }

        HrmEmployee saved = employeeRepository.save(employee);
        log.info("创建员工成功: employeeNo={}, name={}", saved.getEmployeeNo(), saved.getEmployeeName());
        return saved.getId();
    }

    /**
     * 生成员工编号
     */
    private String generateEmployeeNo() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        return "EMP" + timestamp;
    }

    /**
     * 更新员工
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmEmployee updateEmployee(Long id, HrmEmployee employee) {
        HrmEmployee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));

        // 更新基本信息
        existing.setEmployeeName(employee.getEmployeeName());
        existing.setEnglishName(employee.getEnglishName());
        existing.setMobile(employee.getMobile());
        existing.setEmail(employee.getEmail());
        existing.setIdCard(employee.getIdCard());
        existing.setBirthDate(employee.getBirthDate());
        existing.setEducation(employee.getEducation());
        existing.setGraduateSchool(employee.getGraduateSchool());
        existing.setMajor(employee.getMajor());
        existing.setWorkLocation(employee.getWorkLocation());
        existing.setBankAccount(employee.getBankAccount());
        existing.setBankName(employee.getBankName());
        existing.setRemark(employee.getRemark());

        return employeeRepository.save(existing);
    }

    /**
     * 员工入职
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmEmployee hireEmployee(Long id, LocalDate hireDate, String departmentName, String positionName) {
        HrmEmployee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));

        employee.setHireDate(hireDate);
        employee.setDepartmentName(departmentName);
        employee.setPositionName(positionName);
        employee.setWorkStatus(1); // 在职

        return employeeRepository.save(employee);
    }

    /**
     * 员工转正
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmEmployee regularEmployee(Long id, LocalDate regularDate) {
        HrmEmployee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));

        employee.setRegularDate(regularDate);
        employee.setWorkStatus(1); // 在职

        return employeeRepository.save(employee);
    }

    /**
     * 员工离职
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmEmployee resignEmployee(Long id, LocalDate resignDate) {
        HrmEmployee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));

        employee.setResignDate(resignDate);
        employee.setWorkStatus(3); // 离职

        return employeeRepository.save(employee);
    }

    /**
     * 删除员工
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployee(Long id) {
        HrmEmployee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));

        employee.setIsDeleted(true);
        employeeRepository.save(employee);
        log.info("删除员工成功: id={}", id);
    }

    /**
     * 获取员工详情
     */
    public HrmEmployee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));
    }

    /**
     * 根据员工编号获取
     */
    public HrmEmployee getEmployeeByNo(String employeeNo, Long tenantId) {
        return employeeRepository.findByEmployeeNoAndTenantIdAndIsDeletedFalse(employeeNo, tenantId)
                .orElseThrow(() -> new BusinessException("员工不存在: " + employeeNo));
    }

    /**
     * 按部门查询员工
     */
    public List<HrmEmployee> listByDepartment(Long departmentId, Long tenantId) {
        return employeeRepository.findByDepartmentIdAndTenantIdAndIsDeletedFalseOrderByEmployeeNoAsc(departmentId, tenantId);
    }

    /**
     * 查询在职员工
     */
    public List<HrmEmployee> listActiveEmployees(Long tenantId) {
        return employeeRepository.findByWorkStatusNotAndTenantIdAndIsDeletedFalseOrderByEmployeeNoAsc(3, tenantId);
    }

    /**
     * 分页查询员工
     */
    public Page<HrmEmployee> listEmployees(Long tenantId, Pageable pageable) {
        return employeeRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }
}
