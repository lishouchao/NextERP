package com.nexterp.platform.auth.application.service;

import com.nexterp.platform.auth.domain.model.SysRole;
import com.nexterp.platform.auth.domain.model.SysPermission;
import com.nexterp.platform.auth.domain.repository.SysRoleRepository;
import com.nexterp.platform.auth.domain.repository.SysPermissionRepository;
import com.nexterp.platform.auth.api.dto.request.RoleCreateRequest;
import com.nexterp.platform.auth.api.dto.request.RoleUpdateRequest;
import com.nexterp.platform.auth.api.dto.request.RoleQueryRequest;
import com.nexterp.platform.auth.api.dto.response.RoleResponse;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;

    /**
     * 创建角色
     *
     * @param request 创建请求
     * @return 角色响应
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleResponse createRole(RoleCreateRequest request) {
        // 检查角色编码是否已存在
        if (roleRepository.existsByRoleCodeAndTenantIdAndIsDeletedFalse(
                request.getRoleCode(), request.getTenantId())) {
            throw new BusinessException("角色编码已存在");
        }

        // 构建角色实体
        SysRole role = SysRole.builder()
                .tenantId(request.getTenantId())
                .roleCode(request.getRoleCode())
                .roleName(request.getRoleName())
                .roleSort(request.getRoleSort() != null ? request.getRoleSort() : 0)
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();

        // 保存角色
        SysRole savedRole = roleRepository.save(role);

        // 分配权限
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            request.getPermissionIds().forEach(permissionId -> {
                permissionRepository.findById(permissionId).ifPresent(permission -> {
                    savedRole.getPermissions().add(permission);
                });
            });
            roleRepository.save(savedRole);
        }

        log.info("创建角色成功: roleCode={}, tenantId={}", request.getRoleCode(), request.getTenantId());
        return toResponse(savedRole);
    }

    /**
     * 更新角色
     *
     * @param id 角色ID
     * @param request 更新请求
     * @return 角色响应
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleResponse updateRole(Long id, RoleUpdateRequest request) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        // 检查角色编码是否被其他角色使用
        if (request.getRoleCode() != null &&
            !request.getRoleCode().equals(role.getRoleCode()) &&
            roleRepository.existsByRoleCodeAndTenantIdAndIsDeletedFalseAndIdNot(
                    request.getRoleCode(), role.getTenantId(), id)) {
            throw new BusinessException("角色编码已被其他角色使用");
        }

        // 更新基本信息
        if (request.getRoleCode() != null) {
            role.setRoleCode(request.getRoleCode());
        }
        if (request.getRoleName() != null) {
            role.setRoleName(request.getRoleName());
        }
        if (request.getRoleSort() != null) {
            role.setRoleSort(request.getRoleSort());
        }
        if (request.getStatus() != null) {
            role.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            role.setRemark(request.getRemark());
        }

        // 更新权限
        if (request.getPermissionIds() != null) {
            role.getPermissions().clear();
            request.getPermissionIds().forEach(permissionId -> {
                permissionRepository.findById(permissionId).ifPresent(permission -> {
                    role.getPermissions().add(permission);
                });
            });
        }

        SysRole updatedRole = roleRepository.save(role);
        log.info("更新角色成功: id={}", id);
        return toResponse(updatedRole);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        // 检查是否有用户关联
        if (!role.getUsers().isEmpty()) {
            throw new BusinessException("该角色仍有用户关联，无法删除");
        }

        // 软删除
        role.setIsDeleted(true);
        role.setUpdatedAt(LocalDateTime.now());
        roleRepository.save(role);

        log.info("删除角色成功: id={}", id);
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色响应
     */
    public RoleResponse getRoleById(Long id) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        return toResponse(role);
    }

    /**
     * 根据角色编码获取角色
     *
     * @param roleCode 角色编码
     * @param tenantId 租户ID
     * @return 角色响应
     */
    public RoleResponse getRoleByCode(String roleCode, Long tenantId) {
        SysRole role = roleRepository.findByRoleCodeAndTenantIdAndIsDeletedFalse(roleCode, tenantId)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        return toResponse(role);
    }

    /**
     * 分页查询角色
     *
     * @param request 查询请求
     * @param pageable 分页参数
     * @return 分页结果
     */
    public PageResult<RoleResponse> listRoles(RoleQueryRequest request, Pageable pageable) {
        // 构建查询条件
        Specification<SysRole> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 租户ID
            if (request.getTenantId() != null) {
                predicates.add(cb.equal(root.get("tenantId"), request.getTenantId()));
            }

            // 未删除
            predicates.add(cb.equal(root.get("isDeleted"), false));

            // 角色编码模糊查询
            if (request.getRoleCode() != null && !request.getRoleCode().isEmpty()) {
                predicates.add(cb.like(root.get("roleCode"), "%" + request.getRoleCode() + "%"));
            }

            // 角色名称模糊查询
            if (request.getRoleName() != null && !request.getRoleName().isEmpty()) {
                predicates.add(cb.like(root.get("roleName"), "%" + request.getRoleName() + "%"));
            }

            // 状态
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SysRole> page = roleRepository.findAll(spec, pageable);

        return PageResult.<RoleResponse>builder()
                .records(page.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        role.getPermissions().clear();
        permissionIds.forEach(permissionId -> {
            permissionRepository.findById(permissionId).ifPresent(permission -> {
                role.getPermissions().add(permission);
            });
        });
        roleRepository.save(role);

        log.info("为角色分配权限成功: roleId={}, permissionCount={}", roleId, permissionIds.size());
    }

    /**
     * 启用/禁用角色
     *
     * @param id 角色ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleStatus(Long id, Integer status) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        role.setStatus(status);
        roleRepository.save(role);

        log.info("更新角色状态成功: id={}, status={}", id, status);
    }

    /**
     * 获取角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    public List<Long> getRolePermissions(Long roleId) {
        SysRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        return role.getPermissions().stream()
                .map(SysPermission::getId)
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应对象
     *
     * @param role 角色实体
     * @return 角色响应
     */
    private RoleResponse toResponse(SysRole role) {
        return RoleResponse.builder()
                .id(role.getId())
                .tenantId(role.getTenantId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .roleSort(role.getRoleSort())
                .status(role.getStatus())
                .remark(role.getRemark())
                .permissionIds(role.getPermissions().stream()
                        .map(permission -> permission.getId())
                        .collect(Collectors.toList()))
                .permissionCodes(role.getPermissions().stream()
                        .map(SysPermission::getPermissionCode)
                        .collect(Collectors.toList()))
                .userCount(role.getUsers().size())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
