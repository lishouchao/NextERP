package com.nexterp.business.production.domain.repository;

import com.nexterp.business.production.domain.model.ProOperationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工序执行记录仓储接口
 *
 * @author NextERP
 */
@Repository
public interface ProOperationRecordRepository extends JpaRepository<ProOperationRecord, Long>,
        JpaSpecificationExecutor<ProOperationRecord> {

    /**
     * 根据生产订单ID按顺序号排序查询工序记录
     *
     * @param productionOrderId 生产订单ID
     * @return 工序执行记录列表
     */
    List<ProOperationRecord> findByProductionOrderIdOrderBySequenceNo(Long productionOrderId);

    /**
     * 根据工序ID和状态查询工序记录
     *
     * @param processId 工序ID
     * @param status    状态
     * @return 工序执行记录列表
     */
    List<ProOperationRecord> findByProcessIdAndStatus(Long processId, Integer status);

    /**
     * 根据工作中心ID和状态查询工序记录
     *
     * @param workCenterId 工作中心ID
     * @param status       状态
     * @return 工序执行记录列表
     */
    List<ProOperationRecord> findByWorkCenterIdAndStatus(Long workCenterId, Integer status);

    /**
     * 根据报工人员ID和状态查询工序记录
     *
     * @param workerId 报工人员ID
     * @param status   状态
     * @return 工序执行记录列表
     */
    List<ProOperationRecord> findByWorkerIdAndStatus(Long workerId, Integer status);
}
