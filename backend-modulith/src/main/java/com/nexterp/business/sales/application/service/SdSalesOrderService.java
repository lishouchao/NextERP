package com.nexterp.business.sales.application.service;

import com.nexterp.business.sales.domain.model.SdSalesOrderHdr;
import com.nexterp.business.sales.domain.model.SdSalesOrderItm;
import com.nexterp.business.sales.domain.repository.SdSalesOrderHdrRepository;
import com.nexterp.business.sales.domain.repository.SdSalesOrderItmRepository;
import com.nexterp.business.sales.dto.*;
import com.nexterp.business.sales.event.SalesOrderApprovedEvent;
import com.nexterp.business.sales.event.SalesOrderCreatedEvent;
import com.nexterp.business.sales.event.SalesOrderRejectedEvent;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销售订单服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdSalesOrderService {

    private final SdSalesOrderHdrRepository orderHdrRepository;
    private final SdSalesOrderItmRepository orderItmRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Lazy
    private final SdCreditService creditService;

    /**
     * 创建销售订单
     *
     * @param request 创建订单请求
     * @return 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(CreateSalesOrderRequest request) {
        // 生成订单号: "6" + 7位顺序号
        String orderNumber = generateOrderNumber();

        // 构建订单头
        SdSalesOrderHdr hdr = SdSalesOrderHdr.builder()
                .orderNumber(orderNumber)
                .orderType(request.getOrderType())
                .salesOrgId(request.getSalesOrgId())
                .distributionChannel(request.getDistributionChannel())
                .division(request.getDivision())
                .soldToParty(request.getSoldToParty())
                .shipToParty(request.getShipToParty())
                .billToParty(request.getBillToParty())
                .payerParty(request.getPayerParty())
                .documentDate(request.getDocumentDate() != null ? request.getDocumentDate() : LocalDate.now())
                .requestedDeliveryDate(request.getRequestedDeliveryDate())
                .purchaseOrder(request.getPurchaseOrder())
                .remark(request.getRemark())
                .orderStatus("01")
                .deliveryStatus("A")
                .billingStatus("A")
                .netValue(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .grossValue(BigDecimal.ZERO)
                .tenantId(request.getTenantId())
                .build();

        // 构建订单项
        BigDecimal totalNetValue = BigDecimal.ZERO;
        List<SdSalesOrderItm> items = new ArrayList<>();
        if (request.getItems() != null) {
            for (CreateSalesOrderRequest.CreateSalesOrderItemRequest itemReq : request.getItems()) {
                BigDecimal netValue = itemReq.getNetPrice() != null && itemReq.getOrderedQty() != null
                        ? itemReq.getNetPrice().multiply(itemReq.getOrderedQty())
                        : BigDecimal.ZERO;

                SdSalesOrderItm itm = SdSalesOrderItm.builder()
                        .itemNumber(itemReq.getItemNumber())
                        .materialId(itemReq.getMaterialId())
                        .materialCode(itemReq.getMaterialCode())
                        .description(itemReq.getDescription())
                        .orderedQty(itemReq.getOrderedQty())
                        .deliveredQty(BigDecimal.ZERO)
                        .invoicedQty(BigDecimal.ZERO)
                        .salesUnit(itemReq.getSalesUnit())
                        .netPrice(itemReq.getNetPrice())
                        .netValue(netValue)
                        .plantId(itemReq.getPlantId())
                        .slocId(itemReq.getSlocId())
                        .itemCategory(itemReq.getItemCategory() != null ? itemReq.getItemCategory() : "TAN")
                        .orderHdr(hdr)
                        .build();
                items.add(itm);
                totalNetValue = totalNetValue.add(netValue);
            }
        }
        hdr.setItems(items);

        // 计算金额
        BigDecimal taxAmount = totalNetValue.multiply(new BigDecimal("0.13")); // 默认13%税率
        BigDecimal grossValue = totalNetValue.add(taxAmount);
        hdr.setNetValue(totalNetValue);
        hdr.setTaxAmount(taxAmount);
        hdr.setGrossValue(grossValue);

        SdSalesOrderHdr saved = orderHdrRepository.save(hdr);
        log.info("创建销售订单成功: orderNumber={}, netValue={}", orderNumber, totalNetValue);

        // 发布订单创建事件
        eventPublisher.publishEvent(new SalesOrderCreatedEvent(
                saved.getId(),
                saved.getOrderNumber(),
                saved.getOrderType(),
                saved.getTenantId(),
                saved.getSoldToParty(),
                saved.getNetValue()
        ));

        return saved.getId();
    }

    /**
     * 更新销售订单 (仅允许状态为"01"-创建)
     *
     * @param id      订单ID
     * @param request 创建订单请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(Long id, CreateSalesOrderRequest request) {
        SdSalesOrderHdr hdr = orderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售订单不存在"));

        if (!"01".equals(hdr.getOrderStatus())) {
            throw new BusinessException("仅创建状态的订单允许修改");
        }

        // 更新订单头基本信息
        hdr.setOrderType(request.getOrderType());
        hdr.setSalesOrgId(request.getSalesOrgId());
        hdr.setDistributionChannel(request.getDistributionChannel());
        hdr.setDivision(request.getDivision());
        hdr.setSoldToParty(request.getSoldToParty());
        hdr.setShipToParty(request.getShipToParty());
        hdr.setBillToParty(request.getBillToParty());
        hdr.setPayerParty(request.getPayerParty());
        hdr.setDocumentDate(request.getDocumentDate());
        hdr.setRequestedDeliveryDate(request.getRequestedDeliveryDate());
        hdr.setPurchaseOrder(request.getPurchaseOrder());
        hdr.setRemark(request.getRemark());

        // 清除旧行项目并重建
        hdr.getItems().clear();

        BigDecimal totalNetValue = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (CreateSalesOrderRequest.CreateSalesOrderItemRequest itemReq : request.getItems()) {
                BigDecimal netValue = itemReq.getNetPrice() != null && itemReq.getOrderedQty() != null
                        ? itemReq.getNetPrice().multiply(itemReq.getOrderedQty())
                        : BigDecimal.ZERO;

                SdSalesOrderItm itm = SdSalesOrderItm.builder()
                        .itemNumber(itemReq.getItemNumber())
                        .materialId(itemReq.getMaterialId())
                        .materialCode(itemReq.getMaterialCode())
                        .description(itemReq.getDescription())
                        .orderedQty(itemReq.getOrderedQty())
                        .deliveredQty(BigDecimal.ZERO)
                        .invoicedQty(BigDecimal.ZERO)
                        .salesUnit(itemReq.getSalesUnit())
                        .netPrice(itemReq.getNetPrice())
                        .netValue(netValue)
                        .plantId(itemReq.getPlantId())
                        .slocId(itemReq.getSlocId())
                        .itemCategory(itemReq.getItemCategory() != null ? itemReq.getItemCategory() : "TAN")
                        .orderHdr(hdr)
                        .build();
                hdr.getItems().add(itm);
                totalNetValue = totalNetValue.add(netValue);
            }
        }

        // 重新计算金额
        BigDecimal taxAmount = totalNetValue.multiply(new BigDecimal("0.13"));
        BigDecimal grossValue = totalNetValue.add(taxAmount);
        hdr.setNetValue(totalNetValue);
        hdr.setTaxAmount(taxAmount);
        hdr.setGrossValue(grossValue);

        orderHdrRepository.save(hdr);
        log.info("更新销售订单成功: id={}", id);
    }

    /**
     * 删除销售订单 (软删除, 仅状态"01")
     *
     * @param id 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long id) {
        SdSalesOrderHdr hdr = orderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售订单不存在"));

        if (!"01".equals(hdr.getOrderStatus())) {
            throw new BusinessException("仅创建状态的订单允许删除");
        }

        hdr.setIsDeleted(true);
        orderHdrRepository.save(hdr);
        log.info("删除销售订单成功: id={}", id);
    }

    /**
     * 获取销售订单详情 (含行项目)
     *
     * @param id 订单ID
     * @return 销售订单DTO
     */
    public SalesOrderDTO getOrderById(Long id) {
        SdSalesOrderHdr hdr = orderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售订单不存在"));
        return convertToDTO(hdr);
    }

    /**
     * 分页查询销售订单
     *
     * @param tenantId    租户ID
     * @param orderStatus 订单状态 (可选)
     * @param current     当前页
     * @param size        每页大小
     * @return 分页结果
     */
    public PageResult<SalesOrderDTO> listOrders(Long tenantId, String orderStatus, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<SdSalesOrderHdr> page;

        if (orderStatus != null && !orderStatus.isEmpty()) {
            page = orderHdrRepository.findByTenantIdAndOrderStatusAndIsDeletedFalse(tenantId, orderStatus, pageRequest);
        } else {
            page = orderHdrRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageRequest);
        }

        List<SalesOrderDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<SalesOrderDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 提交销售订单 (状态 "01" -> "02")
     *
     * @param id 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitOrder(Long id) {
        SdSalesOrderHdr hdr = orderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售订单不存在"));

        if (!"01".equals(hdr.getOrderStatus())) {
            throw new BusinessException("仅创建状态的订单允许提交");
        }

        hdr.setOrderStatus("02");
        orderHdrRepository.save(hdr);
        log.info("提交销售订单成功: id={}, orderNumber={}", id, hdr.getOrderNumber());
    }

    /**
     * 审批通过销售订单 (状态 "02" -> "03")
     *
     * @param id         订单ID
     * @param approvedBy 审批人
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveOrder(Long id, String approvedBy) {
        SdSalesOrderHdr hdr = orderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售订单不存在"));

        if (!"02".equals(hdr.getOrderStatus())) {
            throw new BusinessException("仅待审批状态的订单允许审批");
        }

        hdr.setOrderStatus("03");
        orderHdrRepository.save(hdr);
        log.info("审批通过销售订单: id={}, orderNumber={}, approvedBy={}", id, hdr.getOrderNumber(), approvedBy);

        // 发布审批通过事件
        eventPublisher.publishEvent(new SalesOrderApprovedEvent(
                hdr.getId(),
                hdr.getOrderNumber(),
                hdr.getTenantId(),
                approvedBy
        ));
    }

    /**
     * 审批拒绝销售订单 (状态 "02" -> "01")
     *
     * @param id         订单ID
     * @param rejectedBy 拒绝人
     * @param reason     拒绝原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectOrder(Long id, String rejectedBy, String reason) {
        SdSalesOrderHdr hdr = orderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售订单不存在"));

        if (!"02".equals(hdr.getOrderStatus())) {
            throw new BusinessException("仅待审批状态的订单允许拒绝");
        }

        hdr.setOrderStatus("01");
        orderHdrRepository.save(hdr);
        log.info("审批拒绝销售订单: id={}, orderNumber={}, rejectedBy={}, reason={}",
                id, hdr.getOrderNumber(), rejectedBy, reason);

        // 发布审批拒绝事件
        eventPublisher.publishEvent(new SalesOrderRejectedEvent(
                hdr.getId(),
                hdr.getOrderNumber(),
                hdr.getTenantId(),
                rejectedBy,
                reason
        ));
    }

    /**
     * 信用检查 (委托给SdCreditService)
     *
     * @param id 订单ID
     * @return 信用检查结果
     */
    @Transactional(rollbackFor = Exception.class)
    public CreditCheckResult creditCheck(Long id) {
        SdSalesOrderHdr hdr = orderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售订单不存在"));

        CreditCheckRequest checkRequest = CreditCheckRequest.builder()
                .tenantId(hdr.getTenantId())
                .customerId(hdr.getSoldToParty())
                .companyId(hdr.getSalesOrgId())
                .checkType("01")
                .documentType("SO")
                .documentId(hdr.getId())
                .documentNumber(hdr.getOrderNumber())
                .checkAmount(hdr.getGrossValue())
                .build();

        return creditService.performCreditCheck(checkRequest);
    }

    /**
     * 可用性检查 (简化版, 返回每项物料的可用数量)
     *
     * @param id 订单ID
     * @return 可用数量映射
     */
    public Map<String, Object> availabilityCheck(Long id) {
        SdSalesOrderHdr hdr = orderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("销售订单不存在"));

        List<Map<String, Object>> itemResults = new ArrayList<>();
        for (SdSalesOrderItm itm : hdr.getItems()) {
            Map<String, Object> itemResult = new HashMap<>();
            itemResult.put("itemNumber", itm.getItemNumber());
            itemResult.put("materialId", itm.getMaterialId());
            itemResult.put("materialCode", itm.getMaterialCode());
            itemResult.put("orderedQty", itm.getOrderedQty());
            // 简化: 假设可用数量为订购数量的90% (实际应查询库存)
            BigDecimal availableQty = itm.getOrderedQty().multiply(new BigDecimal("0.9"));
            itemResult.put("availableQty", availableQty);
            itemResult.put("isAvailable", availableQty.compareTo(itm.getOrderedQty()) >= 0);
            itemResults.add(itemResult);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", id);
        result.put("orderNumber", hdr.getOrderNumber());
        result.put("items", itemResults);
        return result;
    }

    /**
     * 生成订单号 (格式: "6" + 7位顺序号)
     *
     * @return 订单号
     */
    private String generateOrderNumber() {
        long count = orderHdrRepository.count() + 1;
        return "6" + String.format("%07d", count);
    }

    /**
     * 转换为DTO
     *
     * @param hdr 销售订单头实体
     * @return 销售订单DTO
     */
    private SalesOrderDTO convertToDTO(SdSalesOrderHdr hdr) {
        List<SalesOrderItemDTO> itemDTOs = hdr.getItems().stream()
                .map(itm -> SalesOrderItemDTO.builder()
                        .id(itm.getId())
                        .itemNumber(itm.getItemNumber())
                        .materialId(itm.getMaterialId())
                        .materialCode(itm.getMaterialCode())
                        .description(itm.getDescription())
                        .orderedQty(itm.getOrderedQty())
                        .deliveredQty(itm.getDeliveredQty())
                        .invoicedQty(itm.getInvoicedQty())
                        .salesUnit(itm.getSalesUnit())
                        .netPrice(itm.getNetPrice())
                        .netValue(itm.getNetValue())
                        .plantId(itm.getPlantId())
                        .slocId(itm.getSlocId())
                        .itemCategory(itm.getItemCategory())
                        .build())
                .collect(Collectors.toList());

        return SalesOrderDTO.builder()
                .id(hdr.getId())
                .orderNumber(hdr.getOrderNumber())
                .orderType(hdr.getOrderType())
                .salesOrgId(hdr.getSalesOrgId())
                .distributionChannel(hdr.getDistributionChannel())
                .division(hdr.getDivision())
                .soldToParty(hdr.getSoldToParty())
                .shipToParty(hdr.getShipToParty())
                .billToParty(hdr.getBillToParty())
                .payerParty(hdr.getPayerParty())
                .documentDate(hdr.getDocumentDate())
                .requestedDeliveryDate(hdr.getRequestedDeliveryDate())
                .netValue(hdr.getNetValue())
                .taxAmount(hdr.getTaxAmount())
                .grossValue(hdr.getGrossValue())
                .orderStatus(hdr.getOrderStatus())
                .deliveryStatus(hdr.getDeliveryStatus())
                .billingStatus(hdr.getBillingStatus())
                .purchaseOrder(hdr.getPurchaseOrder())
                .remark(hdr.getRemark())
                .items(itemDTOs)
                .build();
    }
}
