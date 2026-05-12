package com.nexterp.business.production.domain.repository;

import com.nexterp.business.production.domain.model.ProRoutingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工艺路线明细仓储接口
 *
 * @author NextERP
 */
@Repository
public interface ProRoutingDetailRepository extends JpaRepository<ProRoutingDetail, Long> {

    /**
     * 根据工艺路线ID按顺序号排序查询明细
     *
     * @param routingId 工艺路线ID
     * @return 工艺路线明细列表
     */
    List<ProRoutingDetail> findByRoutingIdOrderBySequenceNo(Long routingId);

    /**
     * 根据工作中心ID查询明细
     *
     * @param workCenterId 工作中心ID
     * @return 工艺路线明细列表
     */
    List<ProRoutingDetail> findByWorkCenterId(Long workCenterId);

    /**
     * 根据工艺路线ID删除所有明细
     *
     * @param routingId 工艺路线ID
     */
    void deleteByRoutingId(Long routingId);
}
