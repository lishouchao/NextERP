package com.nexterp.platform.auth.application.service;

import com.nexterp.platform.auth.domain.model.SysMenu;
import com.nexterp.platform.auth.domain.repository.SysMenuRepository;
import com.nexterp.platform.auth.api.dto.request.MenuCreateRequest;
import com.nexterp.platform.auth.api.dto.request.MenuUpdateRequest;
import com.nexterp.platform.auth.api.dto.response.MenuResponse;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单管理服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final SysMenuRepository menuRepository;

    /**
     * 创建菜单
     *
     * @param request 创建请求
     * @return 菜单响应
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuResponse createMenu(MenuCreateRequest request) {
        // 构建菜单实体
        SysMenu menu = SysMenu.builder()
                .parentId(request.getParentId())
                .menuName(request.getMenuName())
                .menuType(request.getMenuType())
                .orderNum(request.getOrderNum() != null ? request.getOrderNum() : 0)
                .path(request.getPath())
                .component(request.getComponent())
                .query(request.getQuery())
                .isFrame(request.getIsFrame() != null ? request.getIsFrame() : false)
                .isCache(request.getIsCache() != null ? request.getIsCache() : false)
                .visible(request.getVisible() != null ? request.getVisible() : true)
                .icon(request.getIcon())
                .permission(request.getPermission())
                .remark(request.getRemark())
                .build();
        // 设置租户ID (从父类继承)
        menu.setTenantId(request.getTenantId());

        SysMenu savedMenu = menuRepository.save(menu);
        log.info("创建菜单成功: menuName={}, tenantId={}", request.getMenuName(), request.getTenantId());
        return toResponse(savedMenu);
    }

    /**
     * 更新菜单
     *
     * @param id 菜单ID
     * @param request 更新请求
     * @return 菜单响应
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuResponse updateMenu(Long id, MenuUpdateRequest request) {
        SysMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));

        // 检查是否将自己设置为父级
        if (request.getParentId() != null && request.getParentId().equals(id)) {
            throw new BusinessException("不能将自己设置为父级菜单");
        }

        // 更新基本信息
        if (request.getParentId() != null) {
            menu.setParentId(request.getParentId());
        }
        if (request.getMenuName() != null) {
            menu.setMenuName(request.getMenuName());
        }
        if (request.getMenuType() != null) {
            menu.setMenuType(request.getMenuType());
        }
        if (request.getOrderNum() != null) {
            menu.setOrderNum(request.getOrderNum());
        }
        if (request.getPath() != null) {
            menu.setPath(request.getPath());
        }
        if (request.getComponent() != null) {
            menu.setComponent(request.getComponent());
        }
        if (request.getQuery() != null) {
            menu.setQuery(request.getQuery());
        }
        if (request.getIsFrame() != null) {
            menu.setIsFrame(request.getIsFrame());
        }
        if (request.getIsCache() != null) {
            menu.setIsCache(request.getIsCache());
        }
        if (request.getVisible() != null) {
            menu.setVisible(request.getVisible());
        }
        if (request.getIcon() != null) {
            menu.setIcon(request.getIcon());
        }
        if (request.getPermission() != null) {
            menu.setPermission(request.getPermission());
        }
        if (request.getRemark() != null) {
            menu.setRemark(request.getRemark());
        }

        SysMenu updatedMenu = menuRepository.save(menu);
        log.info("更新菜单成功: id={}", id);
        return toResponse(updatedMenu);
    }

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        SysMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));

        // 检查是否有子菜单
        if (menuRepository.existsByParentIdAndIsDeletedFalse(id)) {
            throw new BusinessException("该菜单存在子菜单，无法删除");
        }

        // 软删除
        menu.setIsDeleted(true);
        menu.setUpdatedAt(java.time.LocalDateTime.now());
        menuRepository.save(menu);

        log.info("删除菜单成功: id={}", id);
    }

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单响应
     */
    public MenuResponse getMenuById(Long id) {
        SysMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));
        return toResponse(menu);
    }

    /**
     * 获取菜单树
     *
     * @param tenantId 租户ID
     * @return 菜单树列表
     */
    public List<MenuResponse> getMenuTree(Long tenantId) {
        List<SysMenu> allMenus = menuRepository
                .findAllByTenantIdAndIsDeletedFalseOrderByOrderNumAsc(tenantId);
        return buildMenuTree(allMenus, null);
    }

    /**
     * 获取用户菜单树
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 菜单树列表
     */
    public List<MenuResponse> getUserMenuTree(Long userId, Long tenantId) {
        // 获取用户可访问的菜单（通过权限关联）
        List<SysMenu> userMenus = menuRepository
                .findMenusByUserIdAndTenantId(userId, tenantId);
        return buildMenuTree(userMenus, null);
    }

    /**
     * 构建菜单树
     *
     * @param menus 所有菜单
     * @param parentId 父级ID
     * @return 菜单树
     */
    private List<MenuResponse> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> {
                    if (parentId == null) {
                        return menu.getParentId() == null || menu.getParentId() == 0;
                    }
                    return parentId.equals(menu.getParentId());
                })
                .map(menu -> {
                    MenuResponse response = toResponse(menu);
                    // 递归获取子菜单
                    List<MenuResponse> children = buildMenuTree(menus, menu.getId());
                    if (!children.isEmpty()) {
                        response.setChildren(children);
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应对象
     *
     * @param menu 菜单实体
     * @return 菜单响应
     */
    private MenuResponse toResponse(SysMenu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .tenantId(menu.getTenantId())
                .parentId(menu.getParentId())
                .menuName(menu.getMenuName())
                .menuType(menu.getMenuType())
                .orderNum(menu.getOrderNum())
                .path(menu.getPath())
                .component(menu.getComponent())
                .query(menu.getQuery())
                .isFrame(menu.getIsFrame())
                .isCache(menu.getIsCache())
                .visible(menu.getVisible())
                .icon(menu.getIcon())
                .permission(menu.getPermission())
                .remark(menu.getRemark())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }
}
