package com.nexterp.platform.auth.domain.repository;

import com.nexterp.platform.auth.domain.model.SysPermission;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * 权限仓储接口
 *
 * @author NextERP
 */
public interface SysPermissionRepository extends JpaRepository<SysPermission, Long>, TenantAwareRepository<SysPermission> {

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 权限编码列表
     */
    @Query("SELECT DISTINCT p.permissionCode FROM SysPermission p " +
           "INNER JOIN SysRolePermission rp ON p.id = rp.permissionId " +
           "INNER JOIN SysUserRole ur ON rp.roleId = ur.roleId " +
           "INNER JOIN SysRole r ON ur.roleId = r.id " +
           "WHERE ur.userId = :userId AND r.tenantId = :tenantId AND r.status = 1 AND p.status = 1 AND p.isDeleted = false")
    List<String> findPermissionCodesByUserId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 权限列表
     */
    @Query("SELECT p FROM SysPermission p " +
           "INNER JOIN SysRolePermission rp ON p.id = rp.permissionId " +
           "WHERE rp.roleId = :roleId AND p.tenantId = :tenantId AND p.isDeleted = false")
    Set<SysPermission> findByRoleId(@Param("roleId") Long roleId, @Param("tenantId") Long tenantId);

    /**
     * 检查权限编码是否存在
     *
     * @param permissionCode 权限编码
     * @param tenantId       租户ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(p) > 0 FROM SysPermission p WHERE p.permissionCode = :permissionCode AND p.tenantId = :tenantId AND p.isDeleted = false")
    boolean existsByPermissionCodeAndTenantIdAndIsDeletedFalse(@Param("permissionCode") String permissionCode, @Param("tenantId") Long tenantId);

    /**
     * 检查权限编码是否被其他权限使用
     *
     * @param permissionCode 权限编码
     * @param tenantId       租户ID
     * @param id             权限ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(p) > 0 FROM SysPermission p WHERE p.permissionCode = :permissionCode AND p.tenantId = :tenantId AND p.isDeleted = false AND p.id != :id")
    boolean existsByPermissionCodeAndTenantIdAndIsDeletedFalseAndIdNot(@Param("permissionCode") String permissionCode, @Param("tenantId") Long tenantId, @Param("id") Long id);

    /**
     * 根据权限编码和租户ID查询权限
     *
     * @param permissionCode 权限编码
     * @param tenantId       租户ID
     * @return 权限
     */
    @Query("SELECT p FROM SysPermission p WHERE p.permissionCode = :permissionCode AND p.tenantId = :tenantId AND p.isDeleted = false")
    Optional<SysPermission> findByPermissionCodeAndTenantIdAndIsDeletedFalse(@Param("permissionCode") String permissionCode, @Param("tenantId") Long tenantId);

    /**
     * 根据父级权限ID查询子权限列表
     *
     * @param parentId 父级权限ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(p) > 0 FROM SysPermission p WHERE p.parentId = :parentId AND p.isDeleted = false")
    boolean existsByParentIdAndIsDeletedFalse(@Param("parentId") Long parentId);

    /**
     * 查询租户所有权限（按排序顺序）
     *
     * @param tenantId 租户ID
     * @return 权限列表
     */
    @Query("SELECT p FROM SysPermission p WHERE p.tenantId = :tenantId AND p.isDeleted = false ORDER BY p.sortOrder ASC")
    List<SysPermission> findAllByTenantIdAndIsDeletedFalseOrderBySortOrderAsc(@Param("tenantId") Long tenantId);
}
