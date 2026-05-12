package com.nexterp.business.production.application.service;

import com.nexterp.business.production.domain.model.ProOperationRecord;
import com.nexterp.business.production.domain.model.ProProductionOrder;
import com.nexterp.business.production.domain.repository.ProOperationRecordRepository;
import com.nexterp.business.production.domain.repository.ProProductionOrderRepository;
import com.nexterp.business.production.dto.*;
import com.nexterp.business.production.event.OperationCompletedEvent;
import com.nexterp.business.production.event.OperationStartedEvent;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工序执行记录服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProOperationRecordService {

    private final ProOperationRecordRepository operationRecordRepository;
    private final ProProductionOrderRepository productionOrderRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建工序执行记录
     *
     * @param request 创建请求
     * @return 记录ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOperationRecord(CreateOperationRecordRequest request) {
        // 校验生产订单是否存在
        ProProductionOrder order = productionOrderRepository.findById(request.getProductionOrderId())
                .orElseThrow(() -> new BusinessException("生产订单不存在"));

        ProOperationRecord record = ProOperationRecord.builder()
                .productionOrderId(request.getProductionOrderId())
                .sequenceNo(request.getSequenceNo())
                .processId(request.getProcessId())
                .processCode(request.getProcessCode())
                .processName(request.getProcessName())
                .workCenterId(request.getWorkCenterId())
                .workCenterName(request.getWorkCenterName())
                .plannedQty(request.getPlannedQty())
                .completedQty(BigDecimal.ZERO)
                .qualifiedQty(BigDecimal.ZERO)
                .scrappedQty(BigDecimal.ZERO)
                .planStartTime(request.getPlanStartTime())
                .planEndTime(request.getPlanEndTime())
                .actualStartTime(request.getActualStartTime())
                .actualEndTime(request.getActualEndTime())
                .workerId(request.getWorkerId())
                .workerName(request.getWorkerName())
                .status(0)
                .actualManHours(request.getActualManHours())
                .actualMachineHours(request.getActualMachineHours())
                .remark(request.getRemark())
                .build();

        ProOperationRecord saved = operationRecordRepository.save(record);
        log.info("创建工序执行记录成功: id={}, productionOrderId={}, sequenceNo={}, processCode={}",
                saved.getId(), request.getProductionOrderId(), request.getSequenceNo(), request.getProcessCode());

        // 发布工序创建事件
        eventPublisher.publishEvent(new OperationStartedEvent(
                saved.getId(),
                saved.getProductionOrderId(),
                saved.getSequenceNo(),
                saved.getProcessCode(),
                saved.getWorkCenterId(),
                saved.getWorkerId(),
                order.getTenantId()
        ));

        return saved.getId();
    }

    /**
     * 更新工序执行记录
     *
     * @param id      记录ID
     * @param request 更新请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOperationRecord(Long id, UpdateOperationRecordRequest request) {
        ProOperationRecord record = operationRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工序执行记录不存在"));

        // 更新字段
        if (request.getCompletedQty() != null) {
            record.setCompletedQty(request.getCompletedQty());
        }
        if (request.getQualifiedQty() != null) {
            record.setQualifiedQty(request.getQualifiedQty());
        }
        if (request.getScrappedQty() != null) {
            record.setScrappedQty(request.getScrappedQty());
        }
        if (request.getActualStartTime() != null) {
            record.setActualStartTime(request.getActualStartTime());
        }
        if (request.getActualEndTime() != null) {
            record.setActualEndTime(request.getActualEndTime());
        }
        if (request.getActualManHours() != null) {
            record.setActualManHours(request.getActualManHours());
        }
        if (request.getActualMachineHours() != null) {
            record.setActualMachineHours(request.getActualMachineHours());
        }
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }

        // 如果状态变更为已完成(2), 执行完工逻辑
        if (request.getStatus() != null) {
            Integer oldStatus = record.getStatus();
            record.setStatus(request.getStatus());

            if (request.getStatus() == 2 && oldStatus != 2) {
                handleOperationCompletion(record);
            }
        }

        operationRecordRepository.save(record);
        log.info("更新工序执行记录成功: id={}", id);
    }

    /**
     * 根据ID获取工序执行记录
     *
     * @param id 记录ID
     * @return 工序执行记录DTO
     */
    public ProOperationRecordDTO getOperationRecordById(Long id) {
        ProOperationRecord record = operationRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工序执行记录不存在"));
        return convertToDTO(record);
    }

    /**
     * 获取指定生产订单的所有工序执行记录
     *
     * @param productionOrderId 生产订单ID
     * @return 工序执行记录列表
     */
    public List<ProOperationRecordDTO> getOrderOperationRecords(Long productionOrderId) {
        List<ProOperationRecord> records = operationRecordRepository
                .findByProductionOrderIdOrderBySequenceNo(productionOrderId);
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询工序执行记录
     *
     * @param tenantId     租户ID
     * @param status       状态 (可选)
     * @param workCenterId 工作中心ID (可选)
     * @param workerId     报工人员ID (可选)
     * @param current      当前页
     * @param size         每页大小
     * @return 分页结果
     */
    public PageResult<ProOperationRecordDTO> listOperationRecords(Long tenantId, Integer status,
                                                                   Long workCenterId, Long workerId,
                                                                   int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);

        Specification<ProOperationRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (workCenterId != null) {
                predicates.add(cb.equal(root.get("workCenterId"), workCenterId));
            }
            if (workerId != null) {
                predicates.add(cb.equal(root.get("workerId"), workerId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProOperationRecord> page = operationRecordRepository.findAll(spec, pageRequest);

        List<ProOperationRecordDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<ProOperationRecordDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 获取生产进度
     *
     * @param productionOrderId 生产订单ID
     * @return 生产进度DTO
     */
    public ProductionProgressDTO getProductionProgress(Long productionOrderId) {
        ProProductionOrder order = productionOrderRepository.findById(productionOrderId)
                .orElseThrow(() -> new BusinessException("生产订单不存在"));

        List<ProOperationRecord> records = operationRecordRepository
                .findByProductionOrderIdOrderBySequenceNo(productionOrderId);

        int totalOperations = records.size();
        int completedOperations = (int) records.stream()
                .filter(r -> r.getStatus() == 2)
                .count();

        // 计算总体完工率: 所有工序完工率的平均值
        BigDecimal overallCompletionRate = BigDecimal.ZERO;
        if (totalOperations > 0) {
            BigDecimal totalRate = records.stream()
                    .map(ProOperationRecord::getCompletionRate)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            overallCompletionRate = totalRate.divide(
                    new BigDecimal(totalOperations), 2, RoundingMode.HALF_UP);
        }

        List<ProductionProgressDTO.OperationProgressItem> operationItems = records.stream()
                .map(r -> ProductionProgressDTO.OperationProgressItem.builder()
                        .sequenceNo(r.getSequenceNo())
                        .processName(r.getProcessName())
                        .status(r.getStatus())
                        .statusName(r.getStatusName())
                        .completionRate(r.getCompletionRate())
                        .qualifiedRate(r.getQualifiedRate())
                        .build())
                .collect(Collectors.toList());

        return ProductionProgressDTO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .totalOperations(totalOperations)
                .completedOperations(completedOperations)
                .overallCompletionRate(overallCompletionRate)
                .operations(operationItems)
                .build();
    }

    /**
     * 开工
     *
     * @param id         记录ID
     * @param workerId   报工人员ID
     * @param workerName 报工人员姓名
     */
    @Transactional(rollbackFor = Exception.class)
    public void startOperation(Long id, Long workerId, String workerName) {
        ProOperationRecord record = operationRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工序执行记录不存在"));

        if (record.getStatus() != 0) {
            throw new BusinessException("仅待开工状态的工序允许开工");
        }

        record.setStatus(1);
        record.setActualStartTime(LocalDateTime.now());
        record.setWorkerId(workerId);
        record.setWorkerName(workerName);

        operationRecordRepository.save(record);
        log.info("工序开工成功: id={}, workerId={}, workerName={}", id, workerId, workerName);

        // 查找生产订单以获取租户ID
        ProProductionOrder order = productionOrderRepository.findById(record.getProductionOrderId())
                .orElseThrow(() -> new BusinessException("生产订单不存在"));

        // 如果订单状态为已审核(1), 更新为生产中(2)
        if (order.canStart()) {
            order.setStatus(2);
            order.setActualStartDate(LocalDateTime.now().toLocalDate());
            productionOrderRepository.save(order);
            log.info("生产订单状态更新为生产中: orderId={}", order.getId());
        }

        // 发布工序开工事件
        eventPublisher.publishEvent(new OperationStartedEvent(
                record.getId(),
                record.getProductionOrderId(),
                record.getSequenceNo(),
                record.getProcessCode(),
                record.getWorkCenterId(),
                workerId,
                order.getTenantId()
        ));
    }

    /**
     * 完工
     *
     * @param id                 记录ID
     * @param completedQty       完工数量
     * @param qualifiedQty       合格数量
     * @param scrappedQty        报废数量
     * @param actualManHours     实际工时
     * @param actualMachineHours 实际机时
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeOperation(Long id, BigDecimal completedQty, BigDecimal qualifiedQty,
                                   BigDecimal scrappedQty, BigDecimal actualManHours,
                                   BigDecimal actualMachineHours) {
        ProOperationRecord record = operationRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工序执行记录不存在"));

        if (record.getStatus() != 1) {
            throw new BusinessException("仅进行中状态的工序允许完工");
        }

        record.setStatus(2);
        record.setActualEndTime(LocalDateTime.now());
        record.setCompletedQty(completedQty);
        record.setQualifiedQty(qualifiedQty);
        record.setScrappedQty(scrappedQty);
        record.setActualManHours(actualManHours);
        record.setActualMachineHours(actualMachineHours);

        operationRecordRepository.save(record);
        log.info("工序完工成功: id={}, completedQty={}, qualifiedQty={}", id, completedQty, qualifiedQty);

        // 处理工序完工后续逻辑
        handleOperationCompletion(record);

        // 查找生产订单以获取租户ID
        ProProductionOrder order = productionOrderRepository.findById(record.getProductionOrderId())
                .orElseThrow(() -> new BusinessException("生产订单不存在"));

        // 发布工序完工事件
        eventPublisher.publishEvent(new OperationCompletedEvent(
                record.getId(),
                record.getProductionOrderId(),
                record.getSequenceNo(),
                record.getProcessCode(),
                completedQty,
                qualifiedQty,
                scrappedQty,
                order.getTenantId()
        ));
    }

    /**
     * 处理工序完工后续逻辑
     * 检查该订单的所有工序是否都已完工, 若全部完工则更新订单状态
     *
     * @param record 工序记录
     */
    private void handleOperationCompletion(ProOperationRecord record) {
        List<ProOperationRecord> allRecords = operationRecordRepository
                .findByProductionOrderIdOrderBySequenceNo(record.getProductionOrderId());

        boolean allCompleted = allRecords.stream()
                .allMatch(r -> r.getStatus() == 2);

        if (allCompleted && !allRecords.isEmpty()) {
            ProProductionOrder order = productionOrderRepository.findById(record.getProductionOrderId())
                    .orElseThrow(() -> new BusinessException("生产订单不存在"));

            if (order.canComplete()) {
                // 使用最后一道工序的完工数量作为订单的完工数量
                ProOperationRecord lastOperation = allRecords.get(allRecords.size() - 1);
                BigDecimal orderCompletedQty = lastOperation.getCompletedQty() != null
                        ? lastOperation.getCompletedQty() : BigDecimal.ZERO;
                BigDecimal orderScrappedQty = lastOperation.getScrappedQty() != null
                        ? lastOperation.getScrappedQty() : BigDecimal.ZERO;

                order.setStatus(3);
                order.setActualEndDate(LocalDateTime.now().toLocalDate());
                order.setCompletedQty(orderCompletedQty);
                order.setScrappedQty(orderScrappedQty);

                productionOrderRepository.save(order);
                log.info("所有工序已完工, 生产订单状态更新为已完工: orderId={}", order.getId());
            }
        }
    }

    /**
     * 转换为DTO
     *
     * @param record 工序执行记录实体
     * @return 工序执行记录DTO
     */
    private ProOperationRecordDTO convertToDTO(ProOperationRecord record) {
        return ProOperationRecordDTO.builder()
                .id(record.getId())
                .productionOrderId(record.getProductionOrderId())
                .sequenceNo(record.getSequenceNo())
                .processId(record.getProcessId())
                .processCode(record.getProcessCode())
                .processName(record.getProcessName())
                .workCenterId(record.getWorkCenterId())
                .workCenterName(record.getWorkCenterName())
                .plannedQty(record.getPlannedQty())
                .completedQty(record.getCompletedQty())
                .qualifiedQty(record.getQualifiedQty())
                .scrappedQty(record.getScrappedQty())
                .planStartTime(record.getPlanStartTime())
                .planEndTime(record.getPlanEndTime())
                .actualStartTime(record.getActualStartTime())
                .actualEndTime(record.getActualEndTime())
                .workerId(record.getWorkerId())
                .workerName(record.getWorkerName())
                .status(record.getStatus())
                .statusName(record.getStatusName())
                .actualManHours(record.getActualManHours())
                .actualMachineHours(record.getActualMachineHours())
                .remark(record.getRemark())
                .completionRate(record.getCompletionRate())
                .qualifiedRate(record.getQualifiedRate())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
