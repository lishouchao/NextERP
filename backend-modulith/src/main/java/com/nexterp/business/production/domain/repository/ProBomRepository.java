package com.nexterp.business.production.domain.repository;

import com.nexterp.business.production.domain.model.ProBom;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 物料清单(BOM)仓储接口
 *
 * @author NextERP
 */
@Repository
public interface ProBomRepository extends TenantAwareRepository<ProBom> {

    /**
     * 根据BOM编码和租户ID查询
     *
     * @param bomCode  BOM编码
     * @param tenantId 租户ID
     * @return BOM
     */
    Optional<ProBom> findByBomCodeAndTenantIdAndIsDeletedFalse(String bomCode, Long tenantId);

    /**
     * 检查BOM编码是否存在
     *
     * @param bomCode  BOM编码
     * @param tenantId 租户ID
     * @return 是否存在
     */
    boolean existsByBomCodeAndTenantIdAndIsDeletedFalse(String bomCode, Long tenantId);

    /**
     * 根据成品物料ID和租户ID查询
     *
     * @param productId 成品物料ID
     * @param tenantId  租户ID
     * @return BOM列表
     */
    List<ProBom> findByProductIdAndTenantIdAndIsDeletedFalse(Long productId, Long tenantId);

    /**
     * 根据BOM类型和租户ID分页查询
     *
     * @param bomType  BOM类型
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return BOM分页
     */
    Page<ProBom> findByBomTypeAndTenantIdAndIsDeletedFalse(Integer bomType, Long tenantId, Pageable pageable);

    /**
     * 根据状态和租户ID分页查询
     *
     * @param status   状态
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return BOM分页
     */
    Page<ProBom> findByStatusAndTenantIdAndIsDeletedFalse(Integer status, Long tenantId, Pageable pageable);

    /**
     * 根据BOM类型和状态和租户ID分页查询
     *
     * @param bomType  BOM类型
     * @param status   状态
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return BOM分页
     */
    Page<ProBom> findByBomTypeAndStatusAndTenantIdAndIsDeletedFalse(Integer bomType, Integer status, Long tenantId, Pageable pageable);

    /**
     * 根据租户ID分页查询
     *
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return BOM分页
     */
    Page<ProBom> findByTenantIdAndIsDeletedFalse(Long tenantId, Pageable pageable);
}
