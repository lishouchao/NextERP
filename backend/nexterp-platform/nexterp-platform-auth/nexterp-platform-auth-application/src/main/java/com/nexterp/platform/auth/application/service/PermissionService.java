package com.nexterp.platform.auth.application.service;

import com.nexterp.platform.auth.domain.model.SysPermission;
import com.nexterp.platform.auth.domain.repository.SysPermissionRepository;
import com.nexterp.platform.auth.domain.repository.SysRoleRepository;
import com.nexterp.platform.auth.api.dto.request.PermissionCreateRequest;
import com.nexterp.platform.auth.api.dto.request.PermissionUpdateRequest;
import com.nexterp.platform.auth.api.dto.request.PermissionQueryRequest;
import com.nexterp.platform.auth.api.dto.response.PermissionResponse;
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
 * 权限管理服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final SysPermissionRepository permissionRepository;
    private final SysRoleRepository roleRepository;

    /**
     * 创建权限
     *
     * @param request 创建请求
     * @return 权限响应
     */
    @Transactional(rollbackFor = Exception.class)
    public PermissionResponse createPermission(PermissionCreateRequest request) {
        // 检查权限编码是否已存在
        if (permissionRepository.existsByPermissionCodeAndTenantIdAndIsDeletedFalse(
                request.getPermissionCode(), request.getTenantId())) {
            throw new BusinessException("权限编码已存在");
        }

        // 构建权限实体
        SysPermission permission = SysPermission.builder()
                .tenantId(request.getTenantId())
                .permissionCode(request.getPermissionCode())
                .permissionName(request.getPermissionName())
                .permissionType(request.getPermissionType() != null ? request.getPermissionType() : "button")
                .parentId(request.getParentId())
                .path(request.getPath())
                .component(request.getComponent())
                .icon(request.getIcon())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .visible(request.getVisible() != null ? request.getVisible() : true)
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();

        SysPermission savedPermission = permissionRepository.save(permission);
        log.info("创建权限成功: permissionCode={}, tenantId={}", request.getPermissionCode(), request.getTenantId());
        return toResponse(savedPermission);
    }

    /**
     * 更新权限
     *
     * @param id 权限ID
     * @param request 更新请求
     * @return 权限响应
     */
    @Transactional(rollbackFor = Exception.class)
    public PermissionResponse updatePermission(Long id, PermissionUpdateRequest request) {
        SysPermission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));

        // 检查权限编码是否被其他权限使用
        if (request.getPermissionCode() != null &&
            !request.getPermissionCode().equals(permission.getPermissionCode()) &&
            permissionRepository.existsByPermissionCodeAndTenantIdAndIsDeletedFalseAndIdNot(
                    request.getPermissionCode(), permission.getTenantId(), id)) {
            throw new BusinessException("权限编码已被其他权限使用");
        }

        // 检查是否将自己设置为父级
        if (request.getParentId() != null && request.getParentId().equals(id)) {
            throw new BusinessException("不能将自己设置为父级权限");
        }

        // 更新基本信息
        if (request.getPermissionCode() != null) {
            permission.setPermissionCode(request.getPermissionCode());
        }
        if (request.getPermissionName() != null) {
            permission.setPermissionName(request.getPermissionName());
        }
        if (request.getPermissionType() != null) {
            permission.setPermissionType(request.getPermissionType());
        }
        if (request.getParentId() != null) {
            permission.setParentId(request.getParentId());
        }
        if (request.getPath() != null) {
            permission.setPath(request.getPath());
        }
        if (request.getComponent() != null) {
            permission.setComponent(request.getComponent());
        }
        if (request.getIcon() != null) {
            permission.setIcon(request.getIcon());
        }
        if (request.getSortOrder() != null) {
            permission.setSortOrder(request.getSortOrder());
        }
        if (request.getVisible() != null) {
            permission.setVisible(request.getVisible());
        }
        if (request.getStatus() != null) {
            permission.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            permission.setRemark(request.getRemark());
        }

        SysPermission updatedPermission = permissionRepository.save(permission);
        log.info("更新权限成功: id={}", id);
        return toResponse(updatedPermission);
    }

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        SysPermission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));

        // 检查是否有子权限
        if (permissionRepository.existsByParentIdAndIsDeletedFalse(id)) {
            throw new BusinessException("该权限存在子权限，无法删除");
        }

        // 检查是否有角色关联
        if (!permission.getRoles().isEmpty()) {
            throw new BusinessException("该权限仍有角色关联，无法删除");
        }

        // 软删除
        permission.setIsDeleted(true);
        permission.setUpdatedAt(LocalDateTime.now());
        permissionRepository.save(permission);

        log.info("删除权限成功: id={}", id);
    }

    /**
     * 获取权限详情
     *
     * @param id 权限ID
     * @return 权限响应
     */
    public PermissionResponse getPermissionById(Long id) {
        SysPermission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));
        return toResponse(permission);
    }

    /**
     * 根据权限编码获取权限
     *
     * @param permissionCode 权限编码
     * @param tenantId 租户ID
     * @return 权限响应
     */
    public PermissionResponse getPermissionByCode(String permissionCode, Long tenantId) {
        SysPermission permission = permissionRepository.findByPermissionCodeAndTenantIdAndIsDeletedFalse(permissionCode, tenantId)
                .orElseThrow(() -> new BusinessException("权限不存在"));
        return toResponse(permission);
    }

    /**
     * 分页查询权限
     *
     * @param request 查询请求
     * @param pageable 分页参数
     * @return 分页结果
     */
    public PageResult<PermissionResponse> listPermissions(PermissionQueryRequest request, Pageable pageable) {
        // 构建查询条件
        Specification<SysPermission> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 租户ID
            if (request.getTenantId() != null) {
                predicates.add(cb.equal(root.get("tenantId"), request.getTenantId()));
            }

            // 未删除
            predicates.add(cb.equal(root.get("isDeleted"), false));

            // 权限编码模糊查询
            if (request.getPermissionCode() != null && !request.getPermissionCode().isEmpty()) {
                predicates.add(cb.like(root.get("permissionCode"), "%" + request.getPermissionCode() + "%"));
            }

            // 权限名称模糊查询
            if (request.getPermissionName() != null && !request.getPermissionName().isEmpty()) {
                predicates.add(cb.like(root.get("permissionName"), "%" + request.getPermissionName() + "%"));
            }

            // 权限类型
            if (request.getPermissionType() != null && !request.getPermissionType().isEmpty()) {
                predicates.add(cb.equal(root.get("permissionType"), request.getPermissionType()));
            }

            // 状态
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // 父级权限ID
            if (request.getParentId() != null) {
                if (request.getParentId() == 0) {
                    // 查询顶级权限
                    predicates.add(cb.or(
                        cb.isNull(root.get("parentId")),
                        cb.equal(root.get("parentId"), 0)
                    ));
                } else {
                    predicates.add(cb.equal(root.get("parentId"), request.getParentId()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SysPermission> page = permissionRepository.findAll(spec, pageable);

        return PageResult.<PermissionResponse>builder()
                .records(page.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 获取权限树
     *
     * @param tenantId 租户ID
     * @return 权限树列表
     */
    public List<PermissionResponse> getPermissionTree(Long tenantId) {
        List<SysPermission> allPermissions = permissionRepository
                .findAllByTenantIdAndIsDeletedFalseOrderBySortOrderAsc(tenantId);

        return buildPermissionTree(allPermissions, null);
    }

    /**
     * 构建权限树
     *
     * @param permissions 所有权限
     * @param parentId 父级ID
     * @return 权限树
     */
    private List<PermissionResponse> buildPermissionTree(List<SysPermission> permissions, Long parentId) {
        return permissions.stream()
                .filter(permission -> {
                    if (parentId == null) {
                        return permission.getParentId() == null || permission.getParentId() == 0;
                    }
                    return parentId.equals(permission.getParentId());
                })
                .map(permission -> {
                    PermissionResponse response = toResponse(permission);
                    // 递归获取子权限
                    List<PermissionResponse> children = buildPermissionTree(permissions, permission.getId());
                    if (!children.isEmpty()) {
                        response.setChildren(children);
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 启用/禁用权限
     *
     * @param id 权限ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePermissionStatus(Long id, Integer status) {
        SysPermission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));

        permission.setStatus(status);
        permissionRepository.save(permission);

        log.info("更新权限状态成功: id={}, status={}", id, status);
    }

    /**
     * 转换为响应对象
     *
     * @param permission 权限实体
     * @return 权限响应
     */
    private PermissionResponse toResponse(SysPermission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .tenantId(permission.getTenantId())
                .permissionCode(permission.getPermissionCode())
                .permissionName(permission.getPermissionName())
                .permissionType(permission.getPermissionType())
                .parentId(permission.getParentId())
                .path(permission.getPath())
                .component(permission.getComponent())
                .icon(permission.getIcon())
                .sortOrder(permission.getSortOrder())
                .visible(permission.getVisible())
                .status(permission.getStatus())
                .remark(permission.getRemark())
                .roleCount(permission.getRoles().size())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}
