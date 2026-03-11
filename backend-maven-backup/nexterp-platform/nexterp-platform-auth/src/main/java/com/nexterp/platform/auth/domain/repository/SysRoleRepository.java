package com.nexterp.platform.auth.domain.repository;

import com.nexterp.platform.auth.domain.model.SysRole;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 角色仓储接口
 *
 * @author NextERP
 */
public interface SysRoleRepository extends JpaRepository<SysRole, Long>, TenantAwareRepository<SysRole> {

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @param tenantId 租户ID
     * @return 角色
     */
    @Query("SELECT r FROM SysRole r WHERE r.roleCode = :roleCode AND r.tenantId = :tenantId AND r.isDeleted = false")
    Optional<SysRole> findByRoleCode(@Param("roleCode") String roleCode, @Param("tenantId") Long tenantId);

    /**
     * 检查角色编码是否存在
     *
     * @param roleCode 角色编码
     * @param tenantId 租户ID
     * @param roleId   角色ID (排除自己)
     * @return 是否存在
     */
    @Query("SELECT COUNT(r) > 0 FROM SysRole r WHERE r.roleCode = :roleCode AND r.tenantId = :tenantId AND r.isDeleted = false AND (:roleId IS NULL OR r.id != :roleId)")
    boolean existsByRoleCode(@Param("roleCode") String roleCode, @Param("tenantId") Long tenantId, @Param("roleId") Long roleId);

    /**
     * 检查角色编码是否存在（忽略软删除）
     *
     * @param roleCode 角色编码
     * @param tenantId 租户ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(r) > 0 FROM SysRole r WHERE r.roleCode = :roleCode AND r.tenantId = :tenantId AND r.isDeleted = false")
    boolean existsByRoleCodeAndTenantIdAndIsDeletedFalse(@Param("roleCode") String roleCode, @Param("tenantId") Long tenantId);

    /**
     * 检查角色编码是否被其他角色使用
     *
     * @param roleCode 角色编码
     * @param tenantId 租户ID
     * @param id       角色ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(r) > 0 FROM SysRole r WHERE r.roleCode = :roleCode AND r.tenantId = :tenantId AND r.isDeleted = false AND r.id != :id")
    boolean existsByRoleCodeAndTenantIdAndIsDeletedFalseAndIdNot(@Param("roleCode") String roleCode, @Param("tenantId") Long tenantId, @Param("id") Long id);

    /**
     * 根据角色编码和租户ID查询角色
     *
     * @param roleCode 角色编码
     * @param tenantId 租户ID
     * @return 角色
     */
    @Query("SELECT r FROM SysRole r WHERE r.roleCode = :roleCode AND r.tenantId = :tenantId AND r.isDeleted = false")
    Optional<SysRole> findByRoleCodeAndTenantIdAndIsDeletedFalse(@Param("roleCode") String roleCode, @Param("tenantId") Long tenantId);
}
