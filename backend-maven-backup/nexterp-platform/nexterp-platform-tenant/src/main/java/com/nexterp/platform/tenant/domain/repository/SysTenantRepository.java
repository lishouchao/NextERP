package com.nexterp.platform.tenant.domain.repository;

import com.nexterp.platform.tenant.domain.model.SysTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 租户仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SysTenantRepository extends JpaRepository<SysTenant, Long>, JpaSpecificationExecutor<SysTenant> {

    /**
     * 根据租户编码查询租户
     *
     * @param tenantCode 租户编码
     * @return 租户
     */
    @Query("SELECT t FROM SysTenant t WHERE t.tenantCode = :tenantCode AND t.isDeleted = false")
    Optional<SysTenant> findByTenantCode(@Param("tenantCode") String tenantCode);

    /**
     * 检查租户编码是否存在
     *
     * @param tenantCode 租户编码
     * @return 是否存在
     */
    @Query("SELECT COUNT(t) > 0 FROM SysTenant t WHERE t.tenantCode = :tenantCode AND t.isDeleted = false")
    boolean existsByTenantCode(@Param("tenantCode") String tenantCode);

    /**
     * 检查租户编码是否被其他租户使用
     *
     * @param tenantCode 租户编码
     * @param id         租户ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(t) > 0 FROM SysTenant t WHERE t.tenantCode = :tenantCode AND t.isDeleted = false AND t.id != :id")
    boolean existsByTenantCodeAndIdNot(@Param("tenantCode") String tenantCode, @Param("id") Long id);

    /**
     * 查询所有启用状态的租户
     *
     * @return 租户列表
     */
    @Query("SELECT t FROM SysTenant t WHERE t.status = 1 AND t.isDeleted = false")
    List<SysTenant> findAllActive();

    /**
     * 查询即将过期的租户
     *
     * @param days 天数
     * @return 租户列表
     */
    @Query("SELECT t FROM SysTenant t WHERE t.expireTime BETWEEN CURRENT_TIMESTAMP AND :expireTime AND t.status = 1 AND t.isDeleted = false")
    List<SysTenant> findExpiringTenants(@Param("expireTime") java.time.LocalDateTime expireTime);
}
