package com.nexterp.platform.auth.application.service;

import com.nexterp.platform.auth.application.dto.LoginRequest;
import com.nexterp.platform.auth.application.dto.LoginResponse;
import com.nexterp.platform.auth.application.dto.LoginResponse.UserInfo;
import com.nexterp.platform.auth.application.dto.LoginResponse.MenuInfo;
import com.nexterp.platform.auth.domain.model.SysMenu;
import com.nexterp.platform.auth.domain.model.SysPermission;
import com.nexterp.platform.auth.domain.model.SysRole;
import com.nexterp.platform.auth.domain.model.SysUser;
import com.nexterp.platform.auth.domain.repository.SysMenuRepository;
import com.nexterp.platform.auth.domain.repository.SysPermissionRepository;
import com.nexterp.platform.auth.domain.repository.SysUserRepository;
import com.nexterp.shared.security.context.UserContext;
import com.nexterp.shared.security.exception.AuthenticationException;
import com.nexterp.shared.security.userdetails.UserInfo;
import com.nexterp.shared.security.utils.JwtUtils;
import com.nexterp.shared.security.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserRepository userRepository;
    private final SysMenuRepository menuRepository;
    private final SysPermissionRepository permissionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) {
        log.info("User login attempt: username={}", request.getUsername());

        // 验证用户名和密码
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 获取当前租户ID
        Long tenantId = UserContext.getTenantId();

        // 查询用户信息
        SysUser user = userRepository.findByUsername(request.getUsername(), tenantId)
            .orElseThrow(() -> new AuthenticationException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("用户名或密码错误");
        }

        // 检查用户状态
        if (!user.isEnabled()) {
            throw new AuthenticationException("账号已被禁用");
        }

        // 检查账号是否过期
        if (user.isAccountExpired()) {
            throw new AuthenticationException("账号已过期");
        }

        // 检查密码是否需要修改
        if (user.isPasswordExpired()) {
            throw new AuthenticationException("密码已过期，请修改密码");
        }

        // 更新最后登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(JwtUtils.getClientIP());
        userRepository.save(user);

        // 构建用户详情
        UserDetails userDetails = buildUserDetails(user);

        // 生成令牌
        String accessToken = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // 查询用户权限和菜单
        List<String> permissions = permissionRepository.findPermissionCodesByUserId(user.getId(), tenantId);
        List<String> roles = user.getRoles().stream()
            .map(SysRole::getRoleCode)
            .collect(Collectors.toList());

        // 查询用户菜单
        List<SysMenu> menus = menuRepository.findByUserId(user.getId(), tenantId);
        List<MenuInfo> menuTree = buildMenuTree(menus);

        // 构建登录响应
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtProperties.getExpiration())
            .userInfo(userDetails)
            .permissions(permissions)
            .roles(roles)
            .menus(menuTree)
            .build();
    }

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    public String refreshToken(String refreshToken) {
        // TODO: 实现令牌刷新逻辑
        return null;
    }

    /**
     * 用户登出
     *
     * @param token 令牌
     */
    public void logout(String token) {
        // TODO: 将令牌加入黑名单 (Redis)
        log.info("User logged out");
    }

    /**
     * 构建用户详情
     *
     * @param user 用户实体
     * @return 用户详情
     */
    private UserDetails buildUserDetails(SysUser user) {
        return UserDetails.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .avatar(user.getAvatarUrl())
            .deptId(user.getDeptId())
            .build();
    }

    /**
     * 构建菜单树
     *
     * @param menus 菜单列表
     * @return 菜单树
     */
    private List<MenuInfo> buildMenuTree(List<SysMenu> menus) {
        // 获取所有根菜单
        List<MenuInfo> rootMenus = menus.stream()
            .filter(menu -> menu.getParentId() == null)
            .map(this::convertToMenuInfo)
            .collect(Collectors.toList());

        // 递归构建子菜单
        for (MenuInfo rootMenu : rootMenus) {
            buildChildMenus(rootMenu, menus);
        }

        return rootMenus;
    }

    /**
     * 递归构建子菜单
     *
     * @param parentMenu 父菜单
     * @param allMenus   所有菜单
     */
    private void buildChildMenus(MenuInfo parentMenu, List<SysMenu> allMenus) {
        List<MenuInfo> children = allMenus.stream()
            .filter(menu -> parentMenu.getId().equals(menu.getParentId()))
            .map(this::convertToMenuInfo)
            .collect(Collectors.toList());

        for (MenuInfo child : children) {
            buildChildMenus(child, allMenus);
        }

        if (!children.isEmpty()) {
            parentMenu.setChildren(children);
        }
    }

    /**
     * 转换为菜单VO
     *
     * @param menu 菜单实体
     * @return 菜单VO
     */
    private MenuInfo convertToMenuInfo(SysMenu menu) {
        return MenuInfo.builder()
            .id(menu.getId())
            .parentId(menu.getParentId())
            .name(menu.getMenuName())
            .type(menu.getMenuType())
            .path(menu.getPath())
            .component(menu.getComponent())
            .query(menu.getQuery())
            .icon(menu.getIcon())
            .orderNum(menu.getOrderNum())
            .visible(menu.getVisible())
            .permission(menu.getPermission())
            .build();
    }
}
