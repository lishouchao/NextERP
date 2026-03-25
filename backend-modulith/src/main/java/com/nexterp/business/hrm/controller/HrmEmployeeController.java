package com.nexterp.business.hrm.controller;

import com.nexterp.business.hrm.application.service.HrmEmployeeService;
import com.nexterp.business.hrm.domain.model.HrmEmployee;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 员工控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hrm/employees")
@RequiredArgsConstructor
public class HrmEmployeeController {

    private final HrmEmployeeService employeeService;

    /**
     * 创建员工
     */
    @PostMapping
    @PreAuthorize("hasAuthority('hrm:employee:add')")
    public Result<Long> createEmployee(@Valid @RequestBody HrmEmployee employee) {
        Long id = employeeService.createEmployee(employee);
        return Result.success(id);
    }

    /**
     * 更新员工
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:employee:edit')")
    public Result<HrmEmployee> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody HrmEmployee employee) {
        HrmEmployee updated = employeeService.updateEmployee(id, employee);
        return Result.success(updated);
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:employee:delete')")
    public Result<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return Result.success();
    }

    /**
     * 获取员工详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:employee:view')")
    public Result<HrmEmployee> getEmployeeById(@PathVariable Long id) {
        HrmEmployee employee = employeeService.getEmployeeById(id);
        return Result.success(employee);
    }

    /**
     * 根据员工编号获取
     */
    @GetMapping("/no/{employeeNo}")
    @PreAuthorize("hasAuthority('hrm:employee:view')")
    public Result<HrmEmployee> getEmployeeByNo(
            @PathVariable String employeeNo,
            @RequestParam Long tenantId) {
        HrmEmployee employee = employeeService.getEmployeeByNo(employeeNo, tenantId);
        return Result.success(employee);
    }

    /**
     * 员工入职
     */
    @PostMapping("/{id}/hire")
    @PreAuthorize("hasAuthority('hrm:employee:edit')")
    public Result<HrmEmployee> hireEmployee(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        LocalDate hireDate = LocalDate.parse(params.get("hireDate").toString());
        String departmentName = params.get("departmentName").toString();
        String positionName = params.get("positionName").toString();
        HrmEmployee employee = employeeService.hireEmployee(id, hireDate, departmentName, positionName);
        return Result.success(employee);
    }

    /**
     * 员工转正
     */
    @PostMapping("/{id}/regular")
    @PreAuthorize("hasAuthority('hrm:employee:edit')")
    public Result<HrmEmployee> regularEmployee(
            @PathVariable Long id,
            @RequestParam String regularDate) {
        HrmEmployee employee = employeeService.regularEmployee(id, LocalDate.parse(regularDate));
        return Result.success(employee);
    }

    /**
     * 员工离职
     */
    @PostMapping("/{id}/resign")
    @PreAuthorize("hasAuthority('hrm:employee:edit')")
    public Result<HrmEmployee> resignEmployee(
            @PathVariable Long id,
            @RequestParam String resignDate) {
        HrmEmployee employee = employeeService.resignEmployee(id, LocalDate.parse(resignDate));
        return Result.success(employee);
    }

    /**
     * 按部门查询员工
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAuthority('hrm:employee:view')")
    public Result<List<HrmEmployee>> listByDepartment(
            @PathVariable Long departmentId,
            @RequestParam Long tenantId) {
        List<HrmEmployee> list = employeeService.listByDepartment(departmentId, tenantId);
        return Result.success(list);
    }

    /**
     * 查询在职员工
     */
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('hrm:employee:view')")
    public Result<List<HrmEmployee>> listActiveEmployees(@RequestParam Long tenantId) {
        List<HrmEmployee> list = employeeService.listActiveEmployees(tenantId);
        return Result.success(list);
    }

    /**
     * 分页查询员工
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('hrm:employee:view')")
    public Result<PageResult<HrmEmployee>> listEmployees(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<HrmEmployee> page = employeeService.listEmployees(
                tenantId, PageRequest.of(current - 1, size));

        PageResult<HrmEmployee> result = PageResult.<HrmEmployee>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
