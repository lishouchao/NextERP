package com.nexterp.business.controlling.domain.repository;

import com.nexterp.business.controlling.domain.model.CoSettlementRule;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 结算规则仓储接口
 *
 * @author NextERP
 */
@Repository
public interface CoSettlementRuleRepository extends TenantAwareRepository<CoSettlementRule> {

    /**
     * 按内部订单查询
     */
    List<CoSettlementRule> findByInternalOrderIdAndTenantIdAndIsDeletedFalseOrderByRuleSequenceAsc(Long internalOrderId, Long tenantId);

    /**
     * 按接收方类型查询
     */
    List<CoSettlementRule> findByReceiverTypeAndTenantIdAndIsDeletedFalse(String receiverType, Long tenantId);

    /**
     * 按接收方ID查询
     */
    List<CoSettlementRule> findByReceiverIdAndTenantIdAndIsDeletedFalse(Long receiverId, Long tenantId);
}
