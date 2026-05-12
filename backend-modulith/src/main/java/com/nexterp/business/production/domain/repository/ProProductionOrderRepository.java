package com.nexterp.business.production.domain.repository;

import com.nexterp.business.production.domain.model.ProProductionOrder;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 生产订单仓储接口
 *
 * @author NextERP
 */
@Repository
public interface ProProductionOrderRepository extends TenantAwareRepository<ProProductionOrder> {

    /**
     * 根据租户ID分页查询生产订单
     *
     * @param tenantId 租户ID
     * @param pageable 分页参数
     * @return 生产订单分页
     */
    Page<ProProductionOrder> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * 根据状态和租户ID分页查询生产订单
     *
     * @param status   状态
     * @param tenantId 租户ID
     * @param pageable 分页参数
     * @return 生产订单分页
     */
    Page<ProProductionOrder> findByStatusAndTenantId(Integer status, Long tenantId, Pageable pageable);

    /**
     * 根据产品ID和租户ID查询生产订单
     *
     * @param productId 产品ID
     * @param tenantId  租户ID
     * @param pageable  分页参数
     * @return 生产订单分页
     */
    Page<ProProductionOrder> findByProductIdAndTenantId(Long productId, Long tenantId, Pageable pageable);

    /**
     * 根据车间ID和租户ID分页查询生产订单
     *
     * @param workshopId 车间ID
     * @param tenantId   租户ID
     * @param pageable   分页参数
     * @return 生产订单分页
     */
    Page<ProProductionOrder> findByWorkshopIdAndTenantId(Long workshopId, Long tenantId, Pageable pageable);

    /**
     * 根据订单号和租户ID查询生产订单
     *
     * @param orderNo  生产订单号
     * @param tenantId 租户ID
     * @return 生产订单
     */
    Optional<ProProductionOrder> findByOrderNoAndTenantId(String orderNo, Long tenantId);
}
