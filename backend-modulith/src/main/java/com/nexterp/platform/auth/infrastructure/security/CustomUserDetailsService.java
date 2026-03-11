package com.nexterp.platform.auth.infrastructure.security;

import com.nexterp.platform.auth.domain.repository.SysUserRepository;
import com.nexterp.platform.auth.domain.model.SysUser;
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
import java.util.List;
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

        // 默认查询系统租户（tenantId=0）
        SysUser user = userRepository.findByUsernameAndTenantIdAndIsDeletedFalse(username, 0L)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        log.debug("用户详情: userId={}, tenantId={}, username={}, status={}", 
            user.getId(), user.getTenantId(), user.getUsername(), user.getStatus());

        return buildUserDetails(user);
    }
    
    /**
     * 构建用户详情
     */
    private UserDetails buildUserDetails(SysUser user) {
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(collectAuthorities(user))
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
    
    /**
     * 收集用户权限
     */
    private Collection<GrantedAuthority> collectAuthorities(SysUser user) {
        Collection<GrantedAuthority> authorities = new HashSet<>();
        
        user.getRoles().forEach(role -> {
            if (!role.getIsDeleted() && role.getStatus() == 1) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
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
