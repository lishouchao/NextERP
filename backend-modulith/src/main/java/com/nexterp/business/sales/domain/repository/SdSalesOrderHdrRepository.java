package com.nexterp.business.sales.domain.repository;

import com.nexterp.business.sales.domain.model.SdSalesOrderHdr;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 销售订单头仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SdSalesOrderHdrRepository extends TenantAwareRepository<SdSalesOrderHdr> {

    /**
     * 根据订单号查询
     *
     * @param orderNumber 订单号
     * @param tenantId    租户ID
     * @return 销售订单头
     */
    Optional<SdSalesOrderHdr> findByOrderNumberAndTenantId(String orderNumber, Long tenantId);

    /**
     * 根据租户ID和订单状态分页查询
     *
     * @param tenantId    租户ID
     * @param orderStatus 订单状态
     * @param pageable    分页
     * @return 销售订单头分页
     */
    Page<SdSalesOrderHdr> findByTenantIdAndOrderStatusAndIsDeletedFalse(Long tenantId, String orderStatus, Pageable pageable);

    /**
     * 根据租户ID分页查询未删除的订单
     *
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 销售订单头分页
     */
    Page<SdSalesOrderHdr> findByTenantIdAndIsDeletedFalse(Long tenantId, Pageable pageable);

    /**
     * 搜索销售订单 (按订单号、订单类型关键词)
     *
     * @param keyword  关键词
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 销售订单头分页
     */
    @Query("SELECT h FROM SdSalesOrderHdr h WHERE h.tenantId = :tenantId AND h.isDeleted = false " +
           "AND (h.orderNumber LIKE %:keyword% OR h.orderType LIKE %:keyword%) " +
           "ORDER BY h.id DESC")
    Page<SdSalesOrderHdr> searchByKeyword(@Param("keyword") String keyword,
                                          @Param("tenantId") Long tenantId,
                                          Pageable pageable);
}
