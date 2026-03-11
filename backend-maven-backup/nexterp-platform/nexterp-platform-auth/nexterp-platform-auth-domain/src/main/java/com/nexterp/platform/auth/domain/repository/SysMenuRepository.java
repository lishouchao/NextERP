package com.nexterp.platform.auth.domain.repository;

import com.nexterp.platform.auth.domain.model.SysMenu;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 菜单仓储接口
 *
 * @author NextERP
 */
public interface SysMenuRepository extends JpaRepository<SysMenu, Long>, TenantAwareRepository<SysMenu> {

    /**
     * 根据父菜单ID查询子菜单列表
     *
     * @param parentId 父菜单ID
     * @param tenantId 租户ID
     * @return 菜单列表
     */
    @Query("SELECT m FROM SysMenu m WHERE m.parentId = :parentId AND m.tenantId = :tenantId AND m.isDeleted = false ORDER BY m.orderNum")
    List<SysMenu> findByParentId(@Param("parentId") Long parentId, @Param("tenantId") Long tenantId);

    /**
     * 查询所有根菜单
     *
     * @param tenantId 租户ID
     * @return 菜单列表
     */
    @Query("SELECT m FROM SysMenu m WHERE m.parentId IS NULL AND m.tenantId = :tenantId AND m.isDeleted = false ORDER BY m.orderNum")
    List<SysMenu> findRootMenus(@Param("tenantId") Long tenantId);

    /**
     * 查询租户所有菜单（按排序顺序）
     *
     * @param tenantId 租户ID
     * @return 菜单列表
     */
    @Query("SELECT m FROM SysMenu m WHERE m.tenantId = :tenantId AND m.isDeleted = false ORDER BY m.orderNum ASC")
    List<SysMenu> findAllByTenantIdAndIsDeletedFalseOrderByOrderNumAsc(@Param("tenantId") Long tenantId);

    /**
     * 检查菜单是否存在子菜单
     *
     * @param parentId 父级菜单ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(m) > 0 FROM SysMenu m WHERE m.parentId = :parentId AND m.isDeleted = false")
    boolean existsByParentIdAndIsDeletedFalse(@Param("parentId") Long parentId);

    /**
     * 查询用户可访问的菜单（根据用户ID和租户ID）
     * 通过用户的角色获取权限，然后匹配菜单的权限标识
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 菜单列表
     */
    @Query("SELECT DISTINCT m FROM SysMenu m " +
           "WHERE m.tenantId = :tenantId AND m.isDeleted = false AND m.visible = true " +
           "AND (" +
           "  m.permission IS NULL OR " +
           "  m.permission IN (" +
           "    SELECT DISTINCT p.permissionCode FROM com.nexterp.platform.auth.domain.model.SysPermission p " +
           "    INNER JOIN p.roles r " +
           "    INNER JOIN r.users u " +
           "    WHERE u.id = :userId" +
           "  )" +
           ") " +
           "ORDER BY m.orderNum")
    List<SysMenu> findMenusByUserId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    /**
     * 根据用户ID查询菜单列表（别名方法，与findMenusByUserId相同）
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 菜单列表
     */
    @Query("SELECT DISTINCT m FROM SysMenu m " +
           "WHERE m.tenantId = :tenantId AND m.isDeleted = false AND m.visible = true " +
           "AND (" +
           "  m.permission IS NULL OR " +
           "  m.permission IN (" +
           "    SELECT DISTINCT p.permissionCode FROM com.nexterp.platform.auth.domain.model.SysPermission p " +
           "    INNER JOIN p.roles r " +
           "    INNER JOIN r.users u " +
           "    WHERE u.id = :userId" +
           "  )" +
           ") " +
           "ORDER BY m.orderNum")
    List<SysMenu> findByUserId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    /**
     * 根据用户ID和租户ID查询菜单列表（别名方法，与findMenusByUserId相同）
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 菜单列表
     */
    default List<SysMenu> findMenusByUserIdAndTenantId(Long userId, Long tenantId) {
        return findMenusByUserId(userId, tenantId);
    }
}
