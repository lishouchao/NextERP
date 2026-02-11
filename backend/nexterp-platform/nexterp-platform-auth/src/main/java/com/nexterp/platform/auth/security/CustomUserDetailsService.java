package com.nexterp.platform.auth.security;

import com.nexterp.platform.auth.domain.model.SysUser;
import com.nexterp.platform.auth.domain.repository.SysUserRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户信息: {}", username);

        SysUser user = userRepository.findByUsername(username, 0L) // 默认查询系统租户
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        if (user.getIsDeleted()) {
            throw new UsernameNotFoundException("用户已删除: " + username);
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        // 检查账号是否过期
        if (user.getExpireTime() != null && user.getExpireTime().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("账号已过期");
        }

        return buildUserDetails(user);
    }

    /**
     * 根据用户ID和租户ID加载用户
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 用户详情
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByIdAndTenantId(Long userId, Long tenantId) {
        log.debug("加载用户信息: userId={}, tenantId={}", userId, tenantId);

        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + userId));

        if (!user.getTenantId().equals(tenantId)) {
            throw new BusinessException("用户不属于该租户");
        }

        if (user.getIsDeleted()) {
            throw new UsernameNotFoundException("用户已删除: " + userId);
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        return buildUserDetails(user);
    }

    /**
     * 构建用户详情
     *
     * @param user 用户实体
     * @return 用户详情
     */
    private UserDetails buildUserDetails(SysUser user) {
        // 收集权限
        Collection<GrantedAuthority> authorities = collectAuthorities(user);

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() != 1)
                .build();
    }

    /**
     * 收集用户权限
     *
     * @param user 用户
     * @return 权限集合
     */
    private Collection<GrantedAuthority> collectAuthorities(SysUser user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 收集角色权限
        user.getRoles().forEach(role -> {
            if (!role.getIsDeleted() && role.getStatus() == 1) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));

                // 收集角色关联的权限
                role.getPermissions().forEach(permission -> {
                    if (!permission.getIsDeleted() && permission.getStatus() == 1) {
                        authorities.add(new SimpleGrantedAuthority(permission.getPermissionCode()));
                    }
                });
            }
        });

        log.debug("用户权限集合: username={}, authorities={}", user.getUsername(), authorities);
        return authorities;
    }
}
