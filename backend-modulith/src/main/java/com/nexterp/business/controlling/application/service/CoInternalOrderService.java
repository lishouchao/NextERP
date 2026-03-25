package com.nexterp.business.controlling.application.service;

import com.nexterp.business.controlling.domain.model.CoInternalOrder;
import com.nexterp.business.controlling.domain.model.CoSettlementRule;
import com.nexterp.business.controlling.domain.repository.CoInternalOrderRepository;
import com.nexterp.business.controlling.domain.repository.CoSettlementRuleRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 内部订单服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoInternalOrderService {

    private final CoInternalOrderRepository internalOrderRepository;
    private final CoSettlementRuleRepository settlementRuleRepository;

    /**
     * 创建内部订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createInternalOrder(CoInternalOrder order) {
        // 生成订单号
        if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
            order.setOrderNumber(generateOrderNumber(order.getOrderType()));
        }

        if (internalOrderRepository.existsByOrderNumberAndTenantIdAndIsDeletedFalse(
                order.getOrderNumber(), order.getTenantId())) {
            throw new BusinessException("订单号已存在: " + order.getOrderNumber());
        }

        // 设置默认值
        if (order.getOrderStatus() == null) {
            order.setOrderStatus("01");
        }
        if (order.getActualCost() == null) {
            order.setActualCost(BigDecimal.ZERO);
        }
        if (order.getAllocatedAmount() == null) {
            order.setAllocatedAmount(BigDecimal.ZERO);
        }
        order.setCreatedAt(LocalDateTime.now());

        CoInternalOrder saved = internalOrderRepository.save(order);
        log.info("创建内部订单成功: orderNumber={}, type={}", order.getOrderNumber(), order.getOrderType());
        return saved.getId();
    }

    /**
     * 生成订单号
     */
    private String generateOrderNumber(String orderType) {
        String prefix = switch (orderType) {
            case "01" -> "IO";
            case "02" -> "IV";
            case "03" -> "MT";
            case "04" -> "RD";
            default -> "OR";
        };
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        return prefix + timestamp;
    }

    /**
     * 下达订单
     */
    @Transactional(rollbackFor = Exception.class)
    public CoInternalOrder releaseOrder(Long id) {
        CoInternalOrder order = internalOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("内部订单不存在"));

        if (!order.canRelease()) {
            throw new BusinessException("订单状态不允许下达");
        }

        order.setOrderStatus("02");
        order.setReleasedAt(LocalDateTime.now());
        return internalOrderRepository.save(order);
    }

    /**
     * 技术完成订单
     */
    @Transactional(rollbackFor = Exception.class)
    public CoInternalOrder completeOrder(Long id) {
        CoInternalOrder order = internalOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("内部订单不存在"));

        if (!order.canClose()) {
            throw new BusinessException("订单状态不允许完成");
        }

        order.setOrderStatus("03");
        order.setCompletedAt(LocalDateTime.now());
        return internalOrderRepository.save(order);
    }

    /**
     * 关闭订单
     */
    @Transactional(rollbackFor = Exception.class)
    public CoInternalOrder closeOrder(Long id) {
        CoInternalOrder order = internalOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("内部订单不存在"));

        order.setOrderStatus("04");
        order.setCompletedAt(LocalDateTime.now());
        return internalOrderRepository.save(order);
    }

    /**
     * 更新订单
     */
    @Transactional(rollbackFor = Exception.class)
    public CoInternalOrder updateInternalOrder(Long id, CoInternalOrder order) {
        CoInternalOrder existing = internalOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("内部订单不存在"));

        if (!"01".equals(existing.getOrderStatus())) {
            throw new BusinessException("只有创建状态的订单可以修改");
        }

        existing.setOrderDescription(order.getOrderDescription());
        existing.setStartDate(order.getStartDate());
        existing.setEndDate(order.getEndDate());
        existing.setBudgetAmount(order.getBudgetAmount());
        existing.setDescription(order.getDescription());

        return internalOrderRepository.save(existing);
    }

    /**
     * 删除订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteInternalOrder(Long id) {
        CoInternalOrder order = internalOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("内部订单不存在"));

        if (!"01".equals(order.getOrderStatus())) {
            throw new BusinessException("只有创建状态的订单可以删除");
        }

        order.setIsDeleted(true);
        internalOrderRepository.save(order);
        log.info("删除内部订单成功: id={}", id);
    }

    /**
     * 获取订单详情
     */
    public CoInternalOrder getInternalOrderById(Long id) {
        return internalOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("内部订单不存在"));
    }

    /**
     * 根据订单号获取
     */
    public CoInternalOrder getInternalOrderByNumber(String orderNumber, Long tenantId) {
        return internalOrderRepository.findByOrderNumberAndTenantIdAndIsDeletedFalse(orderNumber, tenantId)
                .orElseThrow(() -> new BusinessException("内部订单不存在: " + orderNumber));
    }

    /**
     * 按类型查询订单
     */
    public List<CoInternalOrder> listByOrderType(String orderType, Long tenantId) {
        return internalOrderRepository.findByOrderTypeAndTenantIdAndIsDeletedFalseOrderByOrderNumberAsc(orderType, tenantId);
    }

    /**
     * 按状态查询订单
     */
    public List<CoInternalOrder> listByOrderStatus(String orderStatus, Long tenantId) {
        return internalOrderRepository.findByOrderStatusAndTenantIdAndIsDeletedFalseOrderByOrderNumberAsc(orderStatus, tenantId);
    }

    /**
     * 分页查询
     */
    public Page<CoInternalOrder> listInternalOrders(Long tenantId, Pageable pageable) {
        return internalOrderRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }

    /**
     * 添加结算规则
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addSettlementRule(Long orderId, CoSettlementRule rule) {
        CoInternalOrder order = internalOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("内部订单不存在"));

        rule.setInternalOrderId(orderId);
        rule.setTenantId(order.getTenantId());

        // 获取下一个序号
        List<CoSettlementRule> existingRules = settlementRuleRepository
                .findByInternalOrderIdAndTenantIdAndIsDeletedFalseOrderByRuleSequenceAsc(orderId, order.getTenantId());
        int nextSequence = existingRules.isEmpty() ? 1 : existingRules.get(existingRules.size() - 1).getRuleSequence() + 1;
        rule.setRuleSequence(nextSequence);

        CoSettlementRule saved = settlementRuleRepository.save(rule);
        log.info("添加结算规则成功: orderId={}, sequence={}", orderId, nextSequence);
        return saved.getId();
    }

    /**
     * 获取订单的结算规则
     */
    public List<CoSettlementRule> getSettlementRules(Long orderId, Long tenantId) {
        return settlementRuleRepository.findByInternalOrderIdAndTenantIdAndIsDeletedFalseOrderByRuleSequenceAsc(orderId, tenantId);
    }
}
