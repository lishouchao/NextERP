package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmMaterial;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 物料主数据仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmMaterialRepository extends TenantAwareRepository<MmMaterial> {

    /**
     * 根据物料编码和租户ID查询
     *
     * @param materialNumber 物料编码
     * @param tenantId       租户ID
     * @return 物料
     */
    Optional<MmMaterial> findByMaterialNumberAndTenantIdAndIsDeletedFalse(String materialNumber, Long tenantId);

    /**
     * 检查物料编码是否存在
     *
     * @param materialNumber 物料编码
     * @param tenantId       租户ID
     * @return 是否存在
     */
    boolean existsByMaterialNumberAndTenantIdAndIsDeletedFalse(String materialNumber, Long tenantId);

    /**
     * 分页查询物料
     *
     * @param tenantId     租户ID
     * @param materialType 物料类型
     * @param pageable     分页
     * @return 物料分页
     */
    Page<MmMaterial> findByTenantIdAndMaterialTypeAndIsDeletedFalse(Long tenantId, String materialType, Pageable pageable);

    /**
     * 搜索物料
     *
     * @param tenantId 租户ID
     * @param keyword  关键词
     * @param pageable 分页
     * @return 物料分页
     */
    @Query("SELECT m FROM MmMaterial m WHERE m.tenantId = :tenantId AND m.isDeleted = false " +
           "AND (m.materialNumber LIKE %:keyword% OR m.description LIKE %:keyword% OR m.descriptionEn LIKE %:keyword%) " +
           "ORDER BY m.materialNumber ASC")
    Page<MmMaterial> searchMaterials(@Param("tenantId") Long tenantId, @Param("keyword") String keyword, Pageable pageable);
}
