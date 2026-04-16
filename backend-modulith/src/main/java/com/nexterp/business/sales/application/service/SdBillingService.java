package com.nexterp.business.sales.application.service;

import com.nexterp.business.sales.domain.model.SdBillingHdr;
import com.nexterp.business.sales.domain.model.SdBillingItm;
import com.nexterp.business.sales.domain.model.SdDeliveryHdr;
import com.nexterp.business.sales.domain.model.SdDeliveryItm;
import com.nexterp.business.sales.domain.repository.SdBillingHdrRepository;
import com.nexterp.business.sales.domain.repository.SdDeliveryHdrRepository;
import com.nexterp.business.sales.dto.BillingDTO;
import com.nexterp.business.sales.dto.BillingItemDTO;
import com.nexterp.business.sales.dto.CreateBillingRequest;
import com.nexterp.business.sales.event.BillingPostedEvent;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 开票服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdBillingService {

    private final SdBillingHdrRepository billingHdrRepository;
    private final SdDeliveryHdrRepository deliveryHdrRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建开票凭证
     *
     * @param request 创建开票请求
     * @return 开票凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createBilling(CreateBillingRequest request) {
        // 生成开票号: "9" + 7位顺序号
        String billingNumber = generateBillingNumber();

        // 从交货单获取头部信息 (如提供了交货单ID)
        Long salesOrgId = null;
        String distributionChannel = null;
        String division = null;
        Long soldToParty = null;
        Long billToParty = null;
        Long payerParty = null;
        Long orderId = request.getOrderId();

        if (request.getDeliveryId() != null) {
            SdDeliveryHdr delivery = deliveryHdrRepository.findById(request.getDeliveryId())
                    .orElseThrow(() -> new BusinessException("关联交货单不存在"));
            salesOrgId = delivery.getSalesOrgId();
            distributionChannel = delivery.getDistributionChannel();
            division = delivery.getDivision();
            soldToParty = delivery.getSoldToParty();
            if (orderId == null) {
                orderId = delivery.getOrderId();
            }
        }

        // 构建开票头
        SdBillingHdr hdr = SdBillingHdr.builder()
                .billingNumber(billingNumber)
                .billingType(request.getBillingType())
                .salesOrgId(salesOrgId)
                .distributionChannel(distributionChannel)
                .division(division)
                .soldToParty(soldToParty)
                .billToParty(billToParty)
                .payerParty(payerParty)
                .documentDate(LocalDate.now())
                .billingDate(request.getBillingDate() != null ? request.getBillingDate() : LocalDate.now())
                .billingStatus("01")
                .deliveryId(request.getDeliveryId())
                .orderId(orderId)
                .remark(request.getRemark())
                .netValue(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .grossValue(BigDecimal.ZERO)
                .tenantId(request.getTenantId())
                .build();

        // 构建开票项
        BigDecimal totalNetValue = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (CreateBillingRequest.CreateBillingItemRequest itemReq : request.getItems()) {
                BigDecimal netValue = itemReq.getNetPrice() != null && itemReq.getBilledQty() != null
                        ? itemReq.getNetPrice().multiply(itemReq.getBilledQty())
                        : BigDecimal.ZERO;
                BigDecimal taxAmount = netValue.multiply(new BigDecimal("0.13"));
                BigDecimal grossValue = netValue.add(taxAmount);

                SdBillingItm itm = SdBillingItm.builder()
                        .itemNumber(itemReq.getItemNumber())
                        .materialId(itemReq.getMaterialId())
                        .materialCode(itemReq.getMaterialCode())
                        .description(itemReq.getDescription())
                        .billedQty(itemReq.getBilledQty())
                        .salesUnit(itemReq.getSalesUnit())
                        .netPrice(itemReq.getNetPrice())
                        .netValue(netValue)
                        .taxCode("X1")
                        .taxAmount(taxAmount)
                        .grossValue(grossValue)
                        .deliveryId(itemReq.getDeliveryId())
                        .deliveryItemId(itemReq.getDeliveryItemId())
                        .billingHdr(hdr)
                        .build();
                hdr.getItems().add(itm);
                totalNetValue = totalNetValue.add(netValue);
                totalTaxAmount = totalTaxAmount.add(taxAmount);
            }
        }

        // 计算汇总金额
        BigDecimal totalGrossValue = totalNetValue.add(totalTaxAmount);
        hdr.setNetValue(totalNetValue);
        hdr.setTaxAmount(totalTaxAmount);
        hdr.setGrossValue(totalGrossValue);

        SdBillingHdr saved = billingHdrRepository.save(hdr);
        log.info("创建开票凭证成功: billingNumber={}", billingNumber);

        return saved.getId();
    }

    /**
     * 更新开票凭证 (仅状态"01")
     *
     * @param id      开票凭证ID
     * @param request 创建开票请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBilling(Long id, CreateBillingRequest request) {
        SdBillingHdr hdr = billingHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("开票凭证不存在"));

        if (!"01".equals(hdr.getBillingStatus())) {
            throw new BusinessException("仅已创建状态的开票凭证允许修改");
        }

        // 更新基本信息
        hdr.setBillingType(request.getBillingType());
        hdr.setBillingDate(request.getBillingDate());
        hdr.setRemark(request.getRemark());

        // 清除旧行项目并重建
        hdr.getItems().clear();

        BigDecimal totalNetValue = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (CreateBillingRequest.CreateBillingItemRequest itemReq : request.getItems()) {
                BigDecimal netValue = itemReq.getNetPrice() != null && itemReq.getBilledQty() != null
                        ? itemReq.getNetPrice().multiply(itemReq.getBilledQty())
                        : BigDecimal.ZERO;
                BigDecimal taxAmount = netValue.multiply(new BigDecimal("0.13"));
                BigDecimal grossValue = netValue.add(taxAmount);

                SdBillingItm itm = SdBillingItm.builder()
                        .itemNumber(itemReq.getItemNumber())
                        .materialId(itemReq.getMaterialId())
                        .materialCode(itemReq.getMaterialCode())
                        .description(itemReq.getDescription())
                        .billedQty(itemReq.getBilledQty())
                        .salesUnit(itemReq.getSalesUnit())
                        .netPrice(itemReq.getNetPrice())
                        .netValue(netValue)
                        .taxCode("X1")
                        .taxAmount(taxAmount)
                        .grossValue(grossValue)
                        .deliveryId(itemReq.getDeliveryId())
                        .deliveryItemId(itemReq.getDeliveryItemId())
                        .billingHdr(hdr)
                        .build();
                hdr.getItems().add(itm);
                totalNetValue = totalNetValue.add(netValue);
                totalTaxAmount = totalTaxAmount.add(taxAmount);
            }
        }

        BigDecimal totalGrossValue = totalNetValue.add(totalTaxAmount);
        hdr.setNetValue(totalNetValue);
        hdr.setTaxAmount(totalTaxAmount);
        hdr.setGrossValue(totalGrossValue);

        billingHdrRepository.save(hdr);
        log.info("更新开票凭证成功: id={}", id);
    }

    /**
     * 获取开票凭证详情
     *
     * @param id 开票凭证ID
     * @return 开票凭证DTO
     */
    public BillingDTO getBillingById(Long id) {
        SdBillingHdr hdr = billingHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("开票凭证不存在"));
        return convertToDTO(hdr);
    }

    /**
     * 分页查询开票凭证
     *
     * @param tenantId      租户ID
     * @param billingStatus 开票状态 (可选)
     * @param current       当前页
     * @param size          每页大小
     * @return 分页结果
     */
    public PageResult<BillingDTO> listBillings(Long tenantId, String billingStatus, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<SdBillingHdr> page;

        if (billingStatus != null && !billingStatus.isEmpty()) {
            page = billingHdrRepository.findByTenantIdAndBillingStatusAndIsDeletedFalse(
                    tenantId, billingStatus, pageRequest);
        } else {
            page = billingHdrRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageRequest);
        }

        List<BillingDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<BillingDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 过账开票凭证 (状态 "01" -> "02")
     *
     * @param id 开票凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void postBilling(Long id) {
        SdBillingHdr hdr = billingHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("开票凭证不存在"));

        if (!"01".equals(hdr.getBillingStatus())) {
            throw new BusinessException("仅已创建状态的开票凭证允许过账");
        }

        hdr.setBillingStatus("02");
        billingHdrRepository.save(hdr);
        log.info("开票过账成功: id={}, billingNumber={}", id, hdr.getBillingNumber());

        // 发布开票过账事件
        eventPublisher.publishEvent(new BillingPostedEvent(
                hdr.getId(),
                hdr.getBillingNumber(),
                hdr.getTenantId(),
                hdr.getNetValue(),
                hdr.getTaxAmount()
        ));
    }

    /**
     * 取消开票凭证 (状态 "02" -> "03", 创建冲销凭证)
     *
     * @param id 开票凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelBilling(Long id) {
        SdBillingHdr hdr = billingHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("开票凭证不存在"));

        if (!"02".equals(hdr.getBillingStatus())) {
            throw new BusinessException("仅已过账状态的开票凭证允许取消");
        }

        // 更新原凭证状态为已取消
        hdr.setBillingStatus("03");
        billingHdrRepository.save(hdr);
        log.info("取消开票凭证: id={}, billingNumber={}", id, hdr.getBillingNumber());

        // 创建冲销凭证
        String reversalNumber = generateBillingNumber();
        SdBillingHdr reversal = SdBillingHdr.builder()
                .billingNumber(reversalNumber)
                .billingType("S1") // S1-取消发票
                .salesOrgId(hdr.getSalesOrgId())
                .distributionChannel(hdr.getDistributionChannel())
                .division(hdr.getDivision())
                .soldToParty(hdr.getSoldToParty())
                .billToParty(hdr.getBillToParty())
                .payerParty(hdr.getPayerParty())
                .documentDate(LocalDate.now())
                .billingDate(LocalDate.now())
                .billingStatus("02")
                .deliveryId(hdr.getDeliveryId())
                .orderId(hdr.getOrderId())
                .remark("冲销凭证, 原凭证号: " + hdr.getBillingNumber())
                .netValue(hdr.getNetValue().negate())
                .taxAmount(hdr.getTaxAmount().negate())
                .grossValue(hdr.getGrossValue().negate())
                .tenantId(hdr.getTenantId())
                .build();

        // 构建冲销行项目
        for (SdBillingItm origItm : hdr.getItems()) {
            SdBillingItm reversalItm = SdBillingItm.builder()
                    .itemNumber(origItm.getItemNumber())
                    .materialId(origItm.getMaterialId())
                    .materialCode(origItm.getMaterialCode())
                    .description(origItm.getDescription())
                    .billedQty(origItm.getBilledQty())
                    .salesUnit(origItm.getSalesUnit())
                    .netPrice(origItm.getNetPrice())
                    .netValue(origItm.getNetValue().negate())
                    .taxCode(origItm.getTaxCode())
                    .taxAmount(origItm.getTaxAmount().negate())
                    .grossValue(origItm.getGrossValue().negate())
                    .deliveryId(origItm.getDeliveryId())
                    .deliveryItemId(origItm.getDeliveryItemId())
                    .billingHdr(reversal)
                    .build();
            reversal.getItems().add(reversalItm);
        }

        billingHdrRepository.save(reversal);
        log.info("创建冲销开票凭证: reversalNumber={}, 原凭证号={}", reversalNumber, hdr.getBillingNumber());
    }

    /**
     * 预览开票 (基于交货单, 不保存)
     *
     * @param deliveryId 交货单ID
     * @return 开票凭证DTO
     */
    public BillingDTO previewBilling(Long deliveryId) {
        SdDeliveryHdr delivery = deliveryHdrRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException("交货单不存在"));

        List<BillingItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal totalNetValue = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;

        for (SdDeliveryItm deliveryItm : delivery.getItems()) {
            // 简化: 使用交货数量作为开票数量, 实际应根据交货数量和已开票数量的差额
            BigDecimal netValue = deliveryItm.getDeliveryQty();
            BigDecimal taxAmount = netValue.multiply(new BigDecimal("0.13"));
            BigDecimal grossValue = netValue.add(taxAmount);
            totalNetValue = totalNetValue.add(netValue);
            totalTaxAmount = totalTaxAmount.add(taxAmount);

            BillingItemDTO itemDTO = BillingItemDTO.builder()
                    .itemNumber(deliveryItm.getItemNumber())
                    .materialId(deliveryItm.getMaterialId())
                    .materialCode(deliveryItm.getMaterialCode())
                    .description(deliveryItm.getDescription())
                    .billedQty(deliveryItm.getDeliveryQty())
                    .salesUnit(deliveryItm.getSalesUnit())
                    .netValue(netValue)
                    .taxAmount(taxAmount)
                    .grossValue(grossValue)
                    .deliveryId(deliveryId)
                    .deliveryItemId(deliveryItm.getId())
                    .build();
            itemDTOs.add(itemDTO);
        }

        return BillingDTO.builder()
                .billingType("F2") // F2-发票(交货)
                .salesOrgId(delivery.getSalesOrgId())
                .distributionChannel(delivery.getDistributionChannel())
                .division(delivery.getDivision())
                .soldToParty(delivery.getSoldToParty())
                .documentDate(LocalDate.now())
                .billingDate(LocalDate.now())
                .netValue(totalNetValue)
                .taxAmount(totalTaxAmount)
                .grossValue(totalNetValue.add(totalTaxAmount))
                .billingStatus("01")
                .deliveryId(deliveryId)
                .orderId(delivery.getOrderId())
                .items(itemDTOs)
                .build();
    }

    /**
     * 生成开票号 (格式: "9" + 7位顺序号)
     *
     * @return 开票号
     */
    private String generateBillingNumber() {
        long count = billingHdrRepository.count() + 1;
        return "9" + String.format("%07d", count);
    }

    /**
     * 转换为DTO
     *
     * @param hdr 开票凭证头实体
     * @return 开票凭证DTO
     */
    private BillingDTO convertToDTO(SdBillingHdr hdr) {
        List<BillingItemDTO> itemDTOs = hdr.getItems().stream()
                .map(itm -> BillingItemDTO.builder()
                        .id(itm.getId())
                        .itemNumber(itm.getItemNumber())
                        .materialId(itm.getMaterialId())
                        .materialCode(itm.getMaterialCode())
                        .description(itm.getDescription())
                        .billedQty(itm.getBilledQty())
                        .salesUnit(itm.getSalesUnit())
                        .netPrice(itm.getNetPrice())
                        .netValue(itm.getNetValue())
                        .taxCode(itm.getTaxCode())
                        .taxAmount(itm.getTaxAmount())
                        .grossValue(itm.getGrossValue())
                        .costValue(itm.getCostValue())
                        .deliveryId(itm.getDeliveryId())
                        .deliveryItemId(itm.getDeliveryItemId())
                        .build())
                .collect(Collectors.toList());

        return BillingDTO.builder()
                .id(hdr.getId())
                .billingNumber(hdr.getBillingNumber())
                .billingType(hdr.getBillingType())
                .salesOrgId(hdr.getSalesOrgId())
                .distributionChannel(hdr.getDistributionChannel())
                .division(hdr.getDivision())
                .soldToParty(hdr.getSoldToParty())
                .billToParty(hdr.getBillToParty())
                .payerParty(hdr.getPayerParty())
                .documentDate(hdr.getDocumentDate())
                .billingDate(hdr.getBillingDate())
                .netValue(hdr.getNetValue())
                .taxAmount(hdr.getTaxAmount())
                .grossValue(hdr.getGrossValue())
                .paymentTerm(hdr.getPaymentTerm())
                .paymentDueDate(hdr.getPaymentDueDate())
                .billingStatus(hdr.getBillingStatus())
                .deliveryId(hdr.getDeliveryId())
                .orderId(hdr.getOrderId())
                .remark(hdr.getRemark())
                .items(itemDTOs)
                .build();
    }
}
