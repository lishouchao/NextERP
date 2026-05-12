package com.nexterp.business.production.domain.repository;

import com.nexterp.business.production.domain.model.ProWorkProcess;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工序仓储接口
 *
 * @author NextERP
 */
@Repository
public interface ProWorkProcessRepository extends TenantAwareRepository<ProWorkProcess> {

    /**
     * 根据工序编码查询
     *
     * @param processCode 工序编码
     * @param tenantId    租户ID
     * @return 工序
     */
    Optional<ProWorkProcess> findByProcessCodeAndTenantIdAndIsDeletedFalse(String processCode, Long tenantId);

    /**
     * 检查工序编码是否存在
     *
     * @param processCode 工序编码
     * @param tenantId    租户ID
     * @return 是否存在
     */
    boolean existsByProcessCodeAndTenantIdAndIsDeletedFalse(String processCode, Long tenantId);

    /**
     * 根据状态和租户ID分页查询
     *
     * @param status   状态
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 工序分页
     */
    Page<ProWorkProcess> findByStatusAndTenantIdAndIsDeletedFalse(Integer status, Long tenantId, Pageable pageable);

    /**
     * 根据租户ID分页查询
     *
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 工序分页
     */
    Page<ProWorkProcess> findByTenantIdAndIsDeletedFalse(Long tenantId, Pageable pageable);

    /**
     * 根据租户ID查询所有启用的工序
     *
     * @param tenantId 租户ID
     * @return 工序列表
     */
    List<ProWorkProcess> findByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, Integer status);

    /**
     * 根据工序类型和租户ID查询
     *
     * @param processType 工序类型
     * @param tenantId    租户ID
     * @return 工序列表
     */
    List<ProWorkProcess> findByProcessTypeAndTenantIdAndIsDeletedFalse(Integer processType, Long tenantId);
}
