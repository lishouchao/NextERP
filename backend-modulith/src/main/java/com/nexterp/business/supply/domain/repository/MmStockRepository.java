package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmStock;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 库存仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmStockRepository extends TenantAwareRepository<MmStock> {

    /**
     * 根据物料/工厂/库位/租户查询库存
     *
     * @param materialId 物料ID
     * @param plantId    工厂ID
     * @param slocId     库存地点ID
     * @param tenantId   租户ID
     * @return 库存记录
     */
    Optional<MmStock> findByMaterialIdAndPlantIdAndSlocIdAndTenantId(Long materialId, Long plantId, Long slocId, Long tenantId);

    /**
     * 根据物料和租户查询库存
     *
     * @param materialId 物料ID
     * @param tenantId   租户ID
     * @return 库存列表
     */
    List<MmStock> findByMaterialIdAndTenantId(Long materialId, Long tenantId);

    /**
     * 根据工厂和租户分页查询库存
     *
     * @param plantId  工厂ID
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 库存分页
     */
    Page<MmStock> findByPlantIdAndTenantId(Long plantId, Long tenantId, Pageable pageable);
}
