package com.nexterp.business.production.application.service;

import com.nexterp.business.production.domain.model.ProProductionOrder;
import com.nexterp.business.production.domain.model.ProProductionOrderDetail;
import com.nexterp.business.production.domain.repository.ProProductionOrderDetailRepository;
import com.nexterp.business.production.domain.repository.ProProductionOrderRepository;
import com.nexterp.business.production.dto.CreateProductionOrderRequest;
import com.nexterp.business.production.dto.ProProductionOrderDTO;
import com.nexterp.business.production.dto.ProProductionOrderDetailDTO;
import com.nexterp.business.production.event.ProductionOrderCreatedEvent;
import com.nexterp.business.production.event.ProductionOrderStatusChangedEvent;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生产订单服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProProductionOrderService {

    private final ProProductionOrderRepository productionOrderRepository;
    private final ProProductionOrderDetailRepository productionOrderDetailRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建生产订单
     *
     * @param request 创建生产订单请求
     * @return 生产订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(CreateProductionOrderRequest request) {
        // 检查订单号唯一性
        productionOrderRepository.findByOrderNoAndTenantId(request.getOrderNo(), request.getTenantId())
                .ifPresent(existing -> {
                    throw new BusinessException("生产订单号已存在: " + request.getOrderNo());
                });

        ProProductionOrder order = ProProductionOrder.builder()
                .orderNo(request.getOrderNo())
                .orderType(request.getOrderType())
                .productId(request.getProductId())
                .productCode(request.getProductCode())
                .productName(request.getProductName())
                .specification(request.getSpecification())
                .unit(request.getUnit())
                .plannedQty(request.getPlannedQty())
                .completedQty(BigDecimal.ZERO)
                .scrappedQty(BigDecimal.ZERO)
                .bomId(request.getBomId())
                .bomVersion(request.getBomVersion())
                .routingId(request.getRoutingId())
                .planStartDate(request.getPlanStartDate())
                .planEndDate(request.getPlanEndDate())
                .workshopId(request.getWorkshopId())
                .workshopName(request.getWorkshopName())
                .productionLineId(request.getProductionLineId())
                .productionLineName(request.getProductionLineName())
                .status(0) // 草稿
                .priority(request.getPriority())
                .sourceType(request.getSourceType())
                .sourceId(request.getSourceId())
                .sourceNo(request.getSourceNo())
                .demandUserId(request.getDemandUserId())
                .demandUserName(request.getDemandUserName())
                .remark(request.getRemark())
                .tenantId(request.getTenantId())
                .build();

        ProProductionOrder saved = productionOrderRepository.save(order);
        log.info("创建生产订单成功: id={}, orderNo={}", saved.getId(), saved.getOrderNo());

        // 发布生产订单创建事件
        eventPublisher.publishEvent(new ProductionOrderCreatedEvent(
                saved.getId(),
                saved.getOrderNo(),
                saved.getOrderType(),
                saved.getProductId(),
                saved.getTenantId(),
                saved.getPlannedQty()
        ));

        return saved.getId();
    }

    /**
     * 更新生产订单
     *
     * @param id      生产订单ID
     * @param request 更新生产订单请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(Long id, CreateProductionOrderRequest request) {
        ProProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("生产订单不存在"));

        if (!order.canEdit()) {
            throw new BusinessException("当前状态不允许编辑生产订单");
        }

        order.setOrderNo(request.getOrderNo());
        order.setOrderType(request.getOrderType());
        order.setProductId(request.getProductId());
        order.setProductCode(request.getProductCode());
        order.setProductName(request.getProductName());
        order.setSpecification(request.getSpecification());
        order.setUnit(request.getUnit());
        order.setPlannedQty(request.getPlannedQty());
        order.setBomId(request.getBomId());
        order.setBomVersion(request.getBomVersion());
        order.setRoutingId(request.getRoutingId());
        order.setPlanStartDate(request.getPlanStartDate());
        order.setPlanEndDate(request.getPlanEndDate());
        order.setWorkshopId(request.getWorkshopId());
        order.setWorkshopName(request.getWorkshopName());
        order.setProductionLineId(request.getProductionLineId());
        order.setProductionLineName(request.getProductionLineName());
        order.setPriority(request.getPriority());
        order.setSourceType(request.getSourceType());
        order.setSourceId(request.getSourceId());
        order.setSourceNo(request.getSourceNo());
        order.setDemandUserId(request.getDemandUserId());
        order.setDemandUserName(request.getDemandUserName());
        order.setRemark(request.getRemark());

        productionOrderRepository.save(order);
        log.info("更新生产订单成功: id={}", id);
    }

    /**
     * 删除生产订单 (仅草稿状态)
     *
     * @param id 生产订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long id) {
        ProProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("生产订单不存在"));

        if (order.getStatus() != 0) {
            throw new BusinessException("仅草稿状态的生产订单允许删除");
        }

        productionOrderRepository.delete(order);
        log.info("删除生产订单成功: id={}", id);
    }

    /**
     * 根据ID获取生产订单
     *
     * @param id 生产订单ID
     * @return 生产订单DTO
     */
    public ProProductionOrderDTO getOrderById(Long id) {
        ProProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("生产订单不存在"));
        return convertToDTO(order);
    }

    /**
     * 分页查询生产订单
     *
     * @param tenantId   租户ID
     * @param status     状态 (可选)
     * @param workshopId 车间ID (可选)
     * @param current    当前页
     * @param size       每页大小
     * @return 分页结果
     */
    public PageResult<ProProductionOrderDTO> listOrders(Long tenantId, Integer status,
                                                         Long workshopId, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);

        Specification<ProProductionOrder> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (workshopId != null) {
                predicates.add(cb.equal(root.get("workshopId"), workshopId));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<ProProductionOrder> page = productionOrderRepository.findAll(spec, pageRequest);

        List<ProProductionOrderDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<ProProductionOrderDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 审核生产订单 (状态 0 -> 1)
     *
     * @param id          生产订单ID
     * @param approvedBy  审核人姓名
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveOrder(Long id, String approvedBy) {
        ProProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("生产订单不存在"));

        if (!order.canApprove()) {
            throw new BusinessException("当前状态不允许审核生产订单");
        }

        Integer oldStatus = order.getStatus();
        order.setStatus(1);
        order.setApprovedByName(approvedBy);
        order.setApprovedAt(LocalDateTime.now());
        productionOrderRepository.save(order);
        log.info("审核生产订单成功: id={}, orderNo={}, approvedBy={}", id, order.getOrderNo(), approvedBy);

        // 发布状态变更事件
        eventPublisher.publishEvent(new ProductionOrderStatusChangedEvent(
                order.getId(),
                order.getOrderNo(),
                oldStatus,
                1,
                order.getTenantId()
        ));
    }

    /**
     * 开工生产 (状态 1 -> 2, 设置实际开始日期)
     *
     * @param id 生产订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void startProduction(Long id) {
        ProProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("生产订单不存在"));

        if (!order.canStart()) {
            throw new BusinessException("当前状态不允许开工");
        }

        Integer oldStatus = order.getStatus();
        order.setStatus(2);
        order.setActualStartDate(LocalDate.now());
        productionOrderRepository.save(order);
        log.info("生产订单开工成功: id={}, orderNo={}", id, order.getOrderNo());

        // 发布状态变更事件
        eventPublisher.publishEvent(new ProductionOrderStatusChangedEvent(
                order.getId(),
                order.getOrderNo(),
                oldStatus,
                2,
                order.getTenantId()
        ));
    }

    /**
     * 完工报工 (更新完工数量/报废数量, 完工数量>=计划数量时状态 -> 3)
     *
     * @param id           生产订单ID
     * @param completedQty 本次完工数量
     * @param scrappedQty  本次报废数量
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long id, BigDecimal completedQty, BigDecimal scrappedQty) {
        ProProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("生产订单不存在"));

        if (!order.canComplete()) {
            throw new BusinessException("当前状态不允许完工报工");
        }

        // 累加数量
        BigDecimal newCompletedQty = order.getCompletedQty()
                .add(completedQty != null ? completedQty : BigDecimal.ZERO);
        BigDecimal newScrappedQty = order.getScrappedQty()
                .add(scrappedQty != null ? scrappedQty : BigDecimal.ZERO);

        order.setCompletedQty(newCompletedQty);
        order.setScrappedQty(newScrappedQty);

        // 判断是否完工: 完工数量 >= 计划数量时自动完工
        if (newCompletedQty.compareTo(order.getPlannedQty()) >= 0) {
            Integer oldStatus = order.getStatus();
            order.setStatus(3); // 已完工
            order.setActualEndDate(LocalDate.now());
            log.info("生产订单自动完工: id={}, orderNo={}", id, order.getOrderNo());

            // 发布状态变更事件
            eventPublisher.publishEvent(new ProductionOrderStatusChangedEvent(
                    order.getId(),
                    order.getOrderNo(),
                    oldStatus,
                    3,
                    order.getTenantId()
            ));
        }

        productionOrderRepository.save(order);
        log.info("生产订单完工报工成功: id={}, completedQty={}, scrappedQty={}", id, completedQty, scrappedQty);
    }

    /**
     * 关闭生产订单 (状态 3 -> 4)
     *
     * @param id 生产订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeOrder(Long id) {
        ProProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("生产订单不存在"));

        if (!order.canClose()) {
            throw new BusinessException("当前状态不允许关闭生产订单");
        }

        Integer oldStatus = order.getStatus();
        order.setStatus(4);
        productionOrderRepository.save(order);
        log.info("关闭生产订单成功: id={}, orderNo={}", id, order.getOrderNo());

        // 发布状态变更事件
        eventPublisher.publishEvent(new ProductionOrderStatusChangedEvent(
                order.getId(),
                order.getOrderNo(),
                oldStatus,
                4,
                order.getTenantId()
        ));
    }

    /**
     * 取消生产订单 (状态 -> 5, 仅草稿和已审核状态可取消)
     *
     * @param id 生产订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        ProProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("生产订单不存在"));

        if (order.getStatus() >= 2) {
            throw new BusinessException("生产中及之后状态的订单不允许取消");
        }

        Integer oldStatus = order.getStatus();
        order.setStatus(5);
        productionOrderRepository.save(order);
        log.info("取消生产订单成功: id={}, orderNo={}", id, order.getOrderNo());

        // 发布状态变更事件
        eventPublisher.publishEvent(new ProductionOrderStatusChangedEvent(
                order.getId(),
                order.getOrderNo(),
                oldStatus,
                5,
                order.getTenantId()
        ));
    }

    /**
     * 转换为DTO
     *
     * @param order 生产订单实体
     * @return 生产订单DTO
     */
    private ProProductionOrderDTO convertToDTO(ProProductionOrder order) {
        List<ProProductionOrderDetailDTO> detailDTOs = order.getDetails().stream()
                .map(this::convertDetailToDTO)
                .collect(Collectors.toList());

        return ProProductionOrderDTO.builder()
                .id(order.getId())
                .tenantId(order.getTenantId())
                .orderNo(order.getOrderNo())
                .orderType(order.getOrderType())
                .orderTypeName(order.getOrderTypeName())
                .productId(order.getProductId())
                .productCode(order.getProductCode())
                .productName(order.getProductName())
                .specification(order.getSpecification())
                .unit(order.getUnit())
                .plannedQty(order.getPlannedQty())
                .completedQty(order.getCompletedQty())
                .scrappedQty(order.getScrappedQty())
                .completionRate(order.getCompletionRate())
                .bomId(order.getBomId())
                .bomVersion(order.getBomVersion())
                .routingId(order.getRoutingId())
                .planStartDate(order.getPlanStartDate())
                .planEndDate(order.getPlanEndDate())
                .actualStartDate(order.getActualStartDate())
                .actualEndDate(order.getActualEndDate())
                .workshopId(order.getWorkshopId())
                .workshopName(order.getWorkshopName())
                .productionLineId(order.getProductionLineId())
                .productionLineName(order.getProductionLineName())
                .status(order.getStatus())
                .statusName(order.getStatusName())
                .priority(order.getPriority())
                .priorityName(order.getPriorityName())
                .sourceType(order.getSourceType())
                .sourceId(order.getSourceId())
                .sourceNo(order.getSourceNo())
                .demandUserId(order.getDemandUserId())
                .demandUserName(order.getDemandUserName())
                .createdById(order.getCreatedById())
                .createdByName(order.getCreatedByName())
                .approvedById(order.getApprovedById())
                .approvedByName(order.getApprovedByName())
                .approvedAt(order.getApprovedAt())
                .remark(order.getRemark())
                .attachments(order.getAttachments())
                .details(detailDTOs)
                .build();
    }

    /**
     * 转换明细为DTO
     *
     * @param detail 生产订单明细实体
     * @return 生产订单明细DTO
     */
    private ProProductionOrderDetailDTO convertDetailToDTO(ProProductionOrderDetail detail) {
        return ProProductionOrderDetailDTO.builder()
                .id(detail.getId())
                .productionOrderId(detail.getProductionOrderId())
                .lineNo(detail.getLineNo())
                .detailType(detail.getDetailType())
                .detailTypeName(detail.getDetailTypeName())
                .materialId(detail.getMaterialId())
                .materialCode(detail.getMaterialCode())
                .materialName(detail.getMaterialName())
                .specification(detail.getSpecification())
                .unit(detail.getUnit())
                .requiredQty(detail.getRequiredQty())
                .issuedQty(detail.getIssuedQty())
                .receivedQty(detail.getReceivedQty())
                .unissuedQty(detail.getUnissuedQty())
                .unreceivedQty(detail.getUnreceivedQty())
                .warehouseId(detail.getWarehouseId())
                .warehouseName(detail.getWarehouseName())
                .location(detail.getLocation())
                .batchNo(detail.getBatchNo())
                .remark(detail.getRemark())
                .build();
    }
}
