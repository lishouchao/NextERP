package com.nexterp.shared.data.repository;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 租户感知仓储基类
 * 自动添加租户过滤条件
 *
 * @author NextERP
 */
public class TenantAwareRepositoryImpl<T extends TenantAwareEntity> extends SimpleJpaRepository<T, Long>
        implements TenantAwareRepository<T> {

    @PersistenceContext
    private EntityManager entityManager;

    public TenantAwareRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public Optional<T> findByIdAndTenantId(Long id, Long tenantId) {
        return findById(id).filter(entity -> entity.getTenantId().equals(tenantId));
    }

    @Override
    @Transactional
    public void deleteByIdAndTenantId(Long id, Long tenantId) {
        findByIdAndTenantId(id, tenantId).ifPresent(this::delete);
    }

    @Override
    public boolean existsByIdAndTenantId(Long id, Long tenantId) {
        return findByIdAndTenantId(id, tenantId).isPresent();
    }
}
