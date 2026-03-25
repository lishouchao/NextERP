package com.nexterp.business.controlling.domain.repository;

import com.nexterp.business.controlling.domain.model.CoInternalOrder;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 内部订单仓储接口
 *
 * @author NextERP
 */
@Repository
public interface CoInternalOrderRepository extends TenantAwareRepository<CoInternalOrder> {

    /**
     * 根据订单号查询
     */
    Optional<CoInternalOrder> findByOrderNumberAndTenantIdAndIsDeletedFalse(String orderNumber, Long tenantId);

    /**
     * 检查订单号是否存在
     */
    boolean existsByOrderNumberAndTenantIdAndIsDeletedFalse(String orderNumber, Long tenantId);

    /**
     * 按订单类型查询
     */
    List<CoInternalOrder> findByOrderTypeAndTenantIdAndIsDeletedFalseOrderByOrderNumberAsc(String orderType, Long tenantId);

    /**
     * 按订单状态查询
     */
    List<CoInternalOrder> findByOrderStatusAndTenantIdAndIsDeletedFalseOrderByOrderNumberAsc(String orderStatus, Long tenantId);

    /**
     * 按成本中心查询
     */
    List<CoInternalOrder> findByResponsibleCostCenterIdAndTenantIdAndIsDeletedFalseOrderByOrderNumberAsc(Long costCenterId, Long tenantId);

    /**
     * 按公司代码查询
     */
    List<CoInternalOrder> findByCompanyCodeAndTenantIdAndIsDeletedFalseOrderByOrderNumberAsc(String companyCode, Long tenantId);

    /**
     * 按成本控制范围查询
     */
    List<CoInternalOrder> findByControllingAreaAndTenantIdAndIsDeletedFalseOrderByOrderNumberAsc(String controllingArea, Long tenantId);

    /**
     * 按负责人查询
     */
    List<CoInternalOrder> findByPersonResponsibleIdAndTenantIdAndIsDeletedFalseOrderByOrderNumberAsc(Long personId, Long tenantId);
}
