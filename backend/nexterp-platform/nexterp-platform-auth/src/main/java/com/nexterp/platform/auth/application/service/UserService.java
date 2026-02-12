package com.nexterp.platform.auth.application.service;

import com.nexterp.platform.auth.domain.model.SysUser;
import com.nexterp.platform.auth.domain.repository.SysUserRepository;
import com.nexterp.platform.auth.domain.repository.SysRoleRepository;
import com.nexterp.platform.auth.dto.request.UserCreateRequest;
import com.nexterp.platform.auth.dto.request.UserUpdateRequest;
import com.nexterp.platform.auth.dto.request.UserQueryRequest;
import com.nexterp.platform.auth.dto.response.UserResponse;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.data.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户
     *
     * @param request 创建请求
     * @return 用户响应
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResponse createUser(UserCreateRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsernameAndTenantIdAndIsDeletedFalse(
                request.getUsername(), request.getTenantId())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null &&
            userRepository.existsByEmailAndTenantIdAndIsDeletedFalse(
                    request.getEmail(), request.getTenantId())) {
            throw new BusinessException("邮箱已被使用");
        }

        // 构建用户实体
        SysUser user = SysUser.builder()
                .tenantId(request.getTenantId())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .gender(request.getGender())
                .avatarUrl(request.getAvatarUrl())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .userType(request.getUserType() != null ? request.getUserType() : 1)
                .deptId(request.getDeptId())
                .remark(request.getRemark())
                .pwdUpdateTime(LocalDateTime.now())
                .build();

        // 保存用户
        SysUser savedUser = userRepository.save(user);

        // 分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            request.getRoleIds().forEach(roleId -> {
                roleRepository.findById(roleId).ifPresent(role -> {
                    savedUser.getRoles().add(role);
                });
            });
            userRepository.save(savedUser);
        }

        log.info("创建用户成功: username={}, tenantId={}", request.getUsername(), request.getTenantId());
        return toResponse(savedUser);
    }

    /**
     * 更新用户
     *
     * @param id 用户ID
     * @param request 更新请求
     * @return 用户响应
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查邮箱是否被其他用户使用
        if (request.getEmail() != null &&
            !request.getEmail().equals(user.getEmail()) &&
            userRepository.existsByEmailAndTenantIdAndIsDeletedFalseAndIdNot(
                    request.getEmail(), user.getTenantId(), id)) {
            throw new BusinessException("邮箱已被其他用户使用");
        }

        // 更新基本信息
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getDeptId() != null) {
            user.setDeptId(request.getDeptId());
        }
        if (request.getRemark() != null) {
            user.setRemark(request.getRemark());
        }

        // 更新角色
        if (request.getRoleIds() != null) {
            user.getRoles().clear();
            request.getRoleIds().forEach(roleId -> {
                roleRepository.findById(roleId).ifPresent(role -> {
                    user.getRoles().add(role);
                });
            });
        }

        SysUser updatedUser = userRepository.save(user);
        log.info("更新用户成功: id={}", id);
        return toResponse(updatedUser);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 软删除
        user.setIsDeleted(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("删除用户成功: id={}", id);
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户响应
     */
    public UserResponse getUserById(Long id) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toResponse(user);
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @param tenantId 租户ID
     * @return 用户响应
     */
    public UserResponse getUserByUsername(String username, Long tenantId) {
        SysUser user = userRepository.findByUsernameAndTenantIdAndIsDeletedFalse(username, tenantId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toResponse(user);
    }

    /**
     * 分页查询用户
     *
     * @param request 查询请求
     * @param pageable 分页参数
     * @return 分页结果
     */
    public PageResult<UserResponse> listUsers(UserQueryRequest request, Pageable pageable) {
        // 构建查询条件
        Specification<SysUser> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 租户ID
            if (request.getTenantId() != null) {
                predicates.add(cb.equal(root.get("tenantId"), request.getTenantId()));
            }

            // 未删除
            predicates.add(cb.equal(root.get("isDeleted"), false));

            // 用户名模糊查询
            if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                predicates.add(cb.like(root.get("username"), "%" + request.getUsername() + "%"));
            }

            // 真实姓名模糊查询
            if (request.getRealName() != null && !request.getRealName().isEmpty()) {
                predicates.add(cb.like(root.get("realName"), "%" + request.getRealName() + "%"));
            }

            // 状态
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // 部门ID
            if (request.getDeptId() != null) {
                predicates.add(cb.equal(root.get("deptId"), request.getDeptId()));
            }

            // 用户类型
            if (request.getUserType() != null) {
                predicates.add(cb.equal(root.get("userType"), request.getUserType()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SysUser> page = userRepository.findAll(spec, pageable);

        return PageResult.<UserResponse>builder()
                .records(page.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 修改密码
     *
     * @param id 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long id, String oldPassword, String newPassword) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPwdUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("修改密码成功: id={}", id);
    }

    /**
     * 重置密码
     *
     * @param id 用户ID
     * @param newPassword 新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPwdUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("重置密码成功: id={}", id);
    }

    /**
     * 启用/禁用用户
     *
     * @param id 用户ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setStatus(status);
        userRepository.save(user);

        log.info("更新用户状态成功: id={}, status={}", id, status);
    }

    /**
     * 转换为响应对象
     *
     * @param user 用户实体
     * @return 用户响应
     */
    private UserResponse toResponse(SysUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .userType(user.getUserType())
                .deptId(user.getDeptId())
                .remark(user.getRemark())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .pwdUpdateTime(user.getPwdUpdateTime())
                .expireTime(user.getExpireTime())
                .roleIds(user.getRoles().stream()
                        .map(role -> role.getId())
                        .collect(Collectors.toList()))
                .roleNames(user.getRoles().stream()
                        .map(role -> role.getRoleName())
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
