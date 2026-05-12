package com.nexterp.business.production.domain.repository;

import com.nexterp.business.production.domain.model.ProRouting;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工艺路线仓储接口
 *
 * @author NextERP
 */
@Repository
public interface ProRoutingRepository extends TenantAwareRepository<ProRouting> {

    /**
     * 根据租户ID分页查询工艺路线
     *
     * @param tenantId 租户ID
     * @param pageable 分页参数
     * @return 工艺路线分页
     */
    Page<ProRouting> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * 根据产品ID和租户ID查询工艺路线
     *
     * @param productId 产品ID
     * @param tenantId  租户ID
     * @return 工艺路线列表
     */
    List<ProRouting> findByProductIdAndTenantId(Long productId, Long tenantId);

    /**
     * 根据状态和租户ID分页查询工艺路线
     *
     * @param status   状态
     * @param tenantId 租户ID
     * @param pageable 分页参数
     * @return 工艺路线分页
     */
    Page<ProRouting> findByStatusAndTenantId(Integer status, Long tenantId, Pageable pageable);

    /**
     * 根据工艺路线类型和租户ID查询工艺路线
     *
     * @param routingType 工艺路线类型
     * @param tenantId    租户ID
     * @return 工艺路线列表
     */
    List<ProRouting> findByRoutingTypeAndTenantId(Integer routingType, Long tenantId);

    /**
     * 根据工艺路线编码和租户ID查询
     *
     * @param routingCode 工艺路线编码
     * @param tenantId    租户ID
     * @return 工艺路线
     */
    Optional<ProRouting> findByRoutingCodeAndTenantId(String routingCode, Long tenantId);
}
