package com.nexterp.platform.auth.domain.repository;

import com.nexterp.platform.auth.domain.model.SysUser;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 用户仓储接口
 *
 * @author NextERP
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long>, TenantAwareRepository<SysUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @param tenantId 租户ID
     * @return 用户
     */
    @Query("SELECT u FROM SysUser u WHERE u.username = :username AND u.tenantId = :tenantId AND u.isDeleted = false")
    Optional<SysUser> findByUsername(@Param("username") String username, @Param("tenantId") Long tenantId);

    /**
     * 根据邮箱查询用户
     *
     * @param email    邮箱
     * @param tenantId 租户ID
     * @return 用户
     */
    @Query("SELECT u FROM SysUser u WHERE u.email = :email AND u.tenantId = :tenantId AND u.isDeleted = false")
    Optional<SysUser> findByEmail(@Param("email") String email, @Param("tenantId") Long tenantId);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param tenantId 租户ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM SysUser u WHERE u.username = :username AND u.tenantId = :tenantId AND u.isDeleted = false")
    boolean existsByUsername(@Param("username") String username, @Param("tenantId") Long tenantId);

    /**
     * 检查邮箱是否存在
     *
     * @param email    邮箱
     * @param tenantId 租户ID
     * @param userId   用户ID (排除自己)
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM SysUser u WHERE u.email = :email AND u.tenantId = :tenantId AND u.isDeleted = false AND (:userId IS NULL OR u.id != :userId)")
    boolean existsByEmail(@Param("email") String email, @Param("tenantId") Long tenantId, @Param("userId") Long userId);

    /**
     * 检查用户名是否存在（忽略软删除）
     *
     * @param username 用户名
     * @param tenantId 租户ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM SysUser u WHERE u.username = :username AND u.tenantId = :tenantId AND u.isDeleted = false")
    boolean existsByUsernameAndTenantIdAndIsDeletedFalse(@Param("username") String username, @Param("tenantId") Long tenantId);

    /**
     * 检查邮箱是否存在（忽略软删除）
     *
     * @param email    邮箱
     * @param tenantId 租户ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM SysUser u WHERE u.email = :email AND u.tenantId = :tenantId AND u.isDeleted = false")
    boolean existsByEmailAndTenantIdAndIsDeletedFalse(@Param("email") String email, @Param("tenantId") Long tenantId);

    /**
     * 检查邮箱是否被其他用户使用
     *
     * @param email    邮箱
     * @param tenantId 租户ID
     * @param id       用户ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM SysUser u WHERE u.email = :email AND u.tenantId = :tenantId AND u.isDeleted = false AND u.id != :id")
    boolean existsByEmailAndTenantIdAndIsDeletedFalseAndIdNot(@Param("email") String email, @Param("tenantId") Long tenantId, @Param("id") Long id);

    /**
     * 根据用户名和租户ID查询用户
     *
     * @param username 用户名
     * @param tenantId 租户ID
     * @return 用户
     */
    @Query("SELECT u FROM SysUser u WHERE u.username = :username AND u.tenantId = :tenantId AND u.isDeleted = false")
    Optional<SysUser> findByUsernameAndTenantIdAndIsDeletedFalse(@Param("username") String username, @Param("tenantId") Long tenantId);

    /**
     * 统计租户下的未删除用户数量
     *
     * @param tenantId 租户ID
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM SysUser u WHERE u.tenantId = :tenantId AND u.isDeleted = false")
    long countByTenantIdAndIsDeletedFalse(@Param("tenantId") Long tenantId);
}
