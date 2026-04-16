package com.nexterp.business.sales.domain.repository;

import com.nexterp.business.sales.domain.model.SdDeliveryHdr;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 交货单头仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SdDeliveryHdrRepository extends TenantAwareRepository<SdDeliveryHdr> {

    /**
     * 根据交货单号查询
     *
     * @param deliveryNumber 交货单号
     * @param tenantId       租户ID
     * @return 交货单头
     */
    Optional<SdDeliveryHdr> findByDeliveryNumberAndTenantId(String deliveryNumber, Long tenantId);

    /**
     * 根据租户ID和交货状态分页查询
     *
     * @param tenantId       租户ID
     * @param deliveryStatus 交货状态
     * @param pageable       分页
     * @return 交货单头分页
     */
    Page<SdDeliveryHdr> findByTenantIdAndDeliveryStatusAndIsDeletedFalse(Long tenantId, String deliveryStatus, Pageable pageable);

    /**
     * 根据来源订单ID查询未删除的交货单
     *
     * @param orderId 订单ID
     * @return 交货单头列表
     */
    List<SdDeliveryHdr> findByOrderIdAndIsDeletedFalse(Long orderId);

    /**
     * 根据租户ID分页查询未删除的交货单
     *
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 交货单头分页
     */
    Page<SdDeliveryHdr> findByTenantIdAndIsDeletedFalse(Long tenantId, Pageable pageable);
}
