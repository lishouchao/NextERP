package com.nexterp.business.hrm.controller;

import com.nexterp.business.hrm.application.service.HrmDepartmentService;
import com.nexterp.business.hrm.domain.model.HrmDepartment;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hrm/departments")
@RequiredArgsConstructor
public class HrmDepartmentController {

    private final HrmDepartmentService departmentService;

    /**
     * 创建部门
     */
    @PostMapping
    @PreAuthorize("hasAuthority('hrm:department:add')")
    public Result<Long> createDepartment(@Valid @RequestBody HrmDepartment department) {
        Long id = departmentService.createDepartment(department);
        return Result.success(id);
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:department:edit')")
    public Result<HrmDepartment> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody HrmDepartment department) {
        HrmDepartment updated = departmentService.updateDepartment(id, department);
        return Result.success(updated);
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:department:delete')")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:department:view')")
    public Result<HrmDepartment> getDepartmentById(@PathVariable Long id) {
        HrmDepartment department = departmentService.getDepartmentById(id);
        return Result.success(department);
    }

    /**
     * 根据部门编码获取
     */
    @GetMapping("/code/{deptCode}")
    @PreAuthorize("hasAuthority('hrm:department:view')")
    public Result<HrmDepartment> getDepartmentByCode(
            @PathVariable String deptCode,
            @RequestParam Long tenantId) {
        HrmDepartment department = departmentService.getDepartmentByCode(deptCode, tenantId);
        return Result.success(department);
    }

    /**
     * 获取部门树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('hrm:department:view')")
    public Result<List<HrmDepartment>> getDepartmentTree(@RequestParam Long tenantId) {
        List<HrmDepartment> tree = departmentService.getDepartmentTree(tenantId);
        return Result.success(tree);
    }

    /**
     * 获取子部门
     */
    @GetMapping("/{parentId}/children")
    @PreAuthorize("hasAuthority('hrm:department:view')")
    public Result<List<HrmDepartment>> getChildDepartments(
            @PathVariable Long parentId,
            @RequestParam Long tenantId) {
        List<HrmDepartment> children = departmentService.getChildDepartments(parentId, tenantId);
        return Result.success(children);
    }

    /**
     * 分页查询部门
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('hrm:department:view')")
    public Result<PageResult<HrmDepartment>> listDepartments(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<HrmDepartment> page = departmentService.listDepartments(
                tenantId, PageRequest.of(current - 1, size));

        PageResult<HrmDepartment> result = PageResult.<HrmDepartment>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
