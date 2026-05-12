package com.nexterp.business.production.domain.repository;

import com.nexterp.business.production.domain.model.ProProductionOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 生产订单明细仓储接口
 *
 * @author NextERP
 */
@Repository
public interface ProProductionOrderDetailRepository extends JpaRepository<ProProductionOrderDetail, Long> {

    /**
     * 根据生产订单ID按行号排序查询明细
     *
     * @param productionOrderId 生产订单ID
     * @return 生产订单明细列表
     */
    List<ProProductionOrderDetail> findByProductionOrderIdOrderByLineNo(Long productionOrderId);

    /**
     * 根据生产订单ID删除所有明细
     *
     * @param productionOrderId 生产订单ID
     */
    void deleteByProductionOrderId(Long productionOrderId);
}
