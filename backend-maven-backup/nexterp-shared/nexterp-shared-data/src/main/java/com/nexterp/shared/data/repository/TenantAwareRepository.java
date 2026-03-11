package com.nexterp.shared.data.repository;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * 租户感知仓储接口
 * 所有需要租户隔离的仓储都应继承此接口
 *
 * @author NextERP
 */
@NoRepositoryBean
public interface TenantAwareRepository<T extends TenantAwareEntity>
        extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    /**
     * 根据ID和租户ID查询
     *
     * @param id       实体ID
     * @param tenantId 租户ID
     * @return 实体
     */
    Optional<T> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * 删除指定租户的实体
     *
     * @param id       实体ID
     * @param tenantId 租户ID
     */
    void deleteByIdAndTenantId(Long id, Long tenantId);

    /**
     * 检查实体是否属于指定租户
     *
     * @param id       实体ID
     * @param tenantId 租户ID
     * @return 是否属于
     */
    boolean existsByIdAndTenantId(Long id, Long tenantId);
}
