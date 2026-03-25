package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrmDepartment;
import com.nexterp.business.hrm.domain.repository.HrmDepartmentRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrmDepartmentService {

    private final HrmDepartmentRepository departmentRepository;

    /**
     * 创建部门
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createDepartment(HrmDepartment department) {
        // 检查部门编码是否已存在
        if (departmentRepository.existsByDeptCodeAndTenantIdAndIsDeletedFalse(
                department.getDeptCode(), department.getTenantId())) {
            throw new BusinessException("部门编码已存在: " + department.getDeptCode());
        }

        // 设置默认值
        if (department.getStatus() == null) {
            department.setStatus(1);
        }
        if (department.getValidFrom() == null) {
            department.setValidFrom(LocalDate.now());
        }
        if (department.getValidTo() == null) {
            department.setValidTo(LocalDate.of(9999, 12, 31));
        }
        if (department.getIsLeaf() == null) {
            department.setIsLeaf(true);
        }

        // 如果有父部门，更新层级和路径
        if (department.getParentId() != null) {
            HrmDepartment parent = departmentRepository.findById(department.getParentId())
                    .orElseThrow(() -> new BusinessException("父部门不存在"));

            department.setDeptLevel(parent.getDeptLevel() + 1);
            department.setDeptPath(parent.getDeptPath() + "/" + parent.getId());

            // 更新父部门的isLeaf状态
            if (parent.getIsLeaf()) {
                parent.setIsLeaf(false);
                departmentRepository.save(parent);
            }
        } else {
            department.setDeptLevel(1);
            department.setDeptPath("");
        }

        HrmDepartment saved = departmentRepository.save(department);
        log.info("创建部门成功: deptCode={}, name={}", saved.getDeptCode(), saved.getDeptName());
        return saved.getId();
    }

    /**
     * 更新部门
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmDepartment updateDepartment(Long id, HrmDepartment department) {
        HrmDepartment existing = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));

        existing.setDeptName(department.getDeptName());
        existing.setDeptShortName(department.getDeptShortName());
        existing.setLeaderId(department.getLeaderId());
        existing.setLeaderName(department.getLeaderName());
        existing.setPhone(department.getPhone());
        existing.setEmail(department.getEmail());
        existing.setCostCenterId(department.getCostCenterId());
        existing.setCostCenterCode(department.getCostCenterCode());
        existing.setRemark(department.getRemark());

        return departmentRepository.save(existing);
    }

    /**
     * 删除部门
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long id) {
        HrmDepartment department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));

        // 检查是否有子部门
        List<HrmDepartment> children = departmentRepository.findByParentIdAndTenantIdAndIsDeletedFalseOrderBySortOrderAsc(id, department.getTenantId());
        if (!children.isEmpty()) {
            throw new BusinessException("该部门存在子部门，无法删除");
        }

        department.setIsDeleted(true);
        departmentRepository.save(department);
        log.info("删除部门成功: id={}", id);
    }

    /**
     * 获取部门详情
     */
    public HrmDepartment getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));
    }

    /**
     * 根据部门编码获取
     */
    public HrmDepartment getDepartmentByCode(String deptCode, Long tenantId) {
        return departmentRepository.findByDeptCodeAndTenantIdAndIsDeletedFalse(deptCode, tenantId)
                .orElseThrow(() -> new BusinessException("部门不存在: " + deptCode));
    }

    /**
     * 获取部门树
     */
    public List<HrmDepartment> getDepartmentTree(Long tenantId) {
        List<HrmDepartment> allDepts = departmentRepository.findByTenantIdAndIsDeletedFalseOrderBySortOrderAsc(tenantId);
        return buildDepartmentTree(allDepts, null);
    }

    /**
     * 获取子部门
     */
    public List<HrmDepartment> getChildDepartments(Long parentId, Long tenantId) {
        return departmentRepository.findByParentIdAndTenantIdAndIsDeletedFalseOrderBySortOrderAsc(parentId, tenantId);
    }

    /**
     * 分页查询部门
     */
    public Page<HrmDepartment> listDepartments(Long tenantId, Pageable pageable) {
        return departmentRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }

    /**
     * 构建部门树
     */
    private List<HrmDepartment> buildDepartmentTree(List<HrmDepartment> departments, Long parentId) {
        return departments.stream()
                .filter(dept -> {
                    if (parentId == null) {
                        return dept.getParentId() == null;
                    }
                    return parentId.equals(dept.getParentId());
                })
                .peek(dept -> {
                    List<HrmDepartment> children = buildDepartmentTree(departments, dept.getId());
                    if (!children.isEmpty()) {
                        dept.setChildren(children);
                        dept.setIsLeaf(false);
                    }
                })
                .collect(Collectors.toList());
    }
}
