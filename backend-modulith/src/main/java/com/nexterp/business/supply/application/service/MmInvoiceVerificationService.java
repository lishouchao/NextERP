package com.nexterp.business.supply.application.service;

import com.nexterp.business.supply.domain.model.MmInvoiceHdr;
import com.nexterp.business.supply.domain.model.MmInvoiceItm;
import com.nexterp.business.supply.domain.repository.MmInvoiceHdrRepository;
import com.nexterp.business.supply.dto.*;
import com.nexterp.business.supply.event.InvoiceVerifiedEvent;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发票校验服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MmInvoiceVerificationService {

    private final MmInvoiceHdrRepository invoiceHdrRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建发票
     *
     * @param params 发票参数
     * @return 发票ID
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Long createInvoice(Map<String, Object> params) {
        Long tenantId = (Long) params.get("tenantId");
        String invoiceType = (String) params.get("invoiceType");
        Long vendorId = (Long) params.get("vendorId");
        String vendorCode = (String) params.get("vendorCode");
        Long companyId = (Long) params.get("companyId");
        String companyCode = (String) params.get("companyCode");
        String currency = (String) params.getOrDefault("currency", "CNY");
        String supplierInvoice = (String) params.get("supplierInvoice");
        LocalDate supplierInvoiceDate = (LocalDate) params.get("supplierInvoiceDate");
        String paymentTerms = (String) params.get("paymentTerms");
        List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("items");

        String invoiceNumber = generateInvoiceNumber();
        LocalDate today = LocalDate.now();

        MmInvoiceHdr hdr = MmInvoiceHdr.builder()
                .invoiceNumber(invoiceNumber)
                .fiscalYear(today.getYear())
                .invoiceType(invoiceType != null ? invoiceType : "RE")
                .documentDate(today)
                .postingDate(today)
                .vendorId(vendorId)
                .vendorCode(vendorCode)
                .companyId(companyId)
                .companyCode(companyCode)
                .currency(currency)
                .supplierInvoice(supplierInvoice)
                .supplierInvoiceDate(supplierInvoiceDate)
                .paymentTerms(paymentTerms)
                .status("0")
                .tenantId(tenantId)
                .build();

        // 构建行项目
        List<MmInvoiceItm> invoiceItems = new ArrayList<>();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> itemMap = items.get(i);
                MmInvoiceItm itm = MmInvoiceItm.builder()
                        .lineItem(i + 1)
                        .purchaseOrder((String) itemMap.get("purchaseOrder"))
                        .poItem((Integer) itemMap.get("poItem"))
                        .materialId((Long) itemMap.get("materialId"))
                        .materialCode((String) itemMap.get("materialCode"))
                        .shortText((String) itemMap.get("shortText"))
                        .quantity((java.math.BigDecimal) itemMap.get("quantity"))
                        .unit((String) itemMap.get("unit"))
                        .price((java.math.BigDecimal) itemMap.get("price"))
                        .netAmount((java.math.BigDecimal) itemMap.get("netAmount"))
                        .taxCode((String) itemMap.get("taxCode"))
                        .taxAmount((java.math.BigDecimal) itemMap.get("taxAmount"))
                        .plantId((Long) itemMap.get("plantId"))
                        .plantCode((String) itemMap.get("plantCode"))
                        .costCenter((String) itemMap.get("costCenter"))
                        .itemText((String) itemMap.get("itemText"))
                        .invoiceHdr(hdr)
                        .build();
                invoiceItems.add(itm);
            }
        }
        hdr.setItems(invoiceItems);

        MmInvoiceHdr saved = invoiceHdrRepository.save(hdr);
        log.info("创建发票成功: invoiceNumber={}, vendorId={}", invoiceNumber, vendorId);

        return saved.getId();
    }

    /**
     * 获取发票详情
     *
     * @param id 发票ID
     * @return 发票DTO
     */
    public InvoiceDTO getInvoiceById(Long id) {
        MmInvoiceHdr hdr = invoiceHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("发票不存在"));
        return convertToDTO(hdr);
    }

    /**
     * 分页查询发票
     *
     * @param tenantId 租户ID
     * @param status   状态 (可选)
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    public PageResult<InvoiceDTO> listInvoices(Long tenantId, String status, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<MmInvoiceHdr> page;

        if (status != null && !status.isEmpty()) {
            page = invoiceHdrRepository.findByTenantIdAndStatusAndIsDeletedFalse(tenantId, status, pageRequest);
        } else {
            page = invoiceHdrRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageRequest);
        }

        List<InvoiceDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<InvoiceDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 校验发票 (三单匹配, 状态 0 -> 1)
     *
     * @param id 发票ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void verifyInvoice(Long id) {
        MmInvoiceHdr hdr = invoiceHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("发票不存在"));

        if (!"0".equals(hdr.getStatus())) {
            throw new BusinessException("仅草稿状态的发票允许校验");
        }

        // 三单匹配校验 (简化: 检查行项目是否都关联了采购订单)
        for (MmInvoiceItm itm : hdr.getItems()) {
            if (itm.getPurchaseOrder() == null || itm.getPurchaseOrder().isEmpty()) {
                throw new BusinessException("发票行项目未关联采购订单, 无法完成三单匹配");
            }
        }

        hdr.setStatus("1");
        invoiceHdrRepository.save(hdr);
        log.info("发票校验通过: id={}, invoiceNumber={}", id, hdr.getInvoiceNumber());

        // 发布发票校验事件
        eventPublisher.publishEvent(new InvoiceVerifiedEvent(
                hdr.getId(),
                hdr.getInvoiceNumber(),
                hdr.getTenantId(),
                hdr.getVendorId(),
                hdr.getGrossAmount()
        ));
    }

    /**
     * 过账发票 (状态 1 -> 2)
     *
     * @param id 发票ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void postInvoice(Long id) {
        MmInvoiceHdr hdr = invoiceHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("发票不存在"));

        if (!"1".equals(hdr.getStatus())) {
            throw new BusinessException("仅已校验状态的发票允许过账");
        }

        hdr.setStatus("2");
        invoiceHdrRepository.save(hdr);
        log.info("发票过账成功: id={}, invoiceNumber={}", id, hdr.getInvoiceNumber());
    }

    /**
     * 冲销发票 (状态 2 -> 3)
     *
     * @param id 发票ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void reverseInvoice(Long id) {
        MmInvoiceHdr hdr = invoiceHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("发票不存在"));

        if (!"2".equals(hdr.getStatus())) {
            throw new BusinessException("仅已过账状态的发票允许冲销");
        }

        hdr.setStatus("3");
        invoiceHdrRepository.save(hdr);
        log.info("发票冲销成功: id={}, invoiceNumber={}", id, hdr.getInvoiceNumber());
    }

    /**
     * 生成发票号 (格式: "51" + 8位顺序号)
     *
     * @return 发票号
     */
    private String generateInvoiceNumber() {
        long count = invoiceHdrRepository.count() + 1;
        return "51" + String.format("%08d", count);
    }

    /**
     * 转换为DTO
     *
     * @param hdr 发票头实体
     * @return 发票DTO
     */
    private InvoiceDTO convertToDTO(MmInvoiceHdr hdr) {
        List<InvoiceItemDTO> itemDTOs = hdr.getItems().stream()
                .map(itm -> InvoiceItemDTO.builder()
                        .id(itm.getId())
                        .lineItem(itm.getLineItem())
                        .purchaseOrder(itm.getPurchaseOrder())
                        .poItem(itm.getPoItem())
                        .materialId(itm.getMaterialId())
                        .materialCode(itm.getMaterialCode())
                        .shortText(itm.getShortText())
                        .quantity(itm.getQuantity())
                        .unit(itm.getUnit())
                        .price(itm.getPrice())
                        .netAmount(itm.getNetAmount())
                        .taxCode(itm.getTaxCode())
                        .taxAmount(itm.getTaxAmount())
                        .plantId(itm.getPlantId())
                        .plantCode(itm.getPlantCode())
                        .costCenter(itm.getCostCenter())
                        .itemText(itm.getItemText())
                        .build())
                .collect(Collectors.toList());

        return InvoiceDTO.builder()
                .id(hdr.getId())
                .invoiceNumber(hdr.getInvoiceNumber())
                .fiscalYear(hdr.getFiscalYear())
                .invoiceType(hdr.getInvoiceType())
                .documentDate(hdr.getDocumentDate())
                .postingDate(hdr.getPostingDate())
                .vendorId(hdr.getVendorId())
                .vendorCode(hdr.getVendorCode())
                .companyId(hdr.getCompanyId())
                .companyCode(hdr.getCompanyCode())
                .currency(hdr.getCurrency())
                .supplierInvoice(hdr.getSupplierInvoice())
                .supplierInvoiceDate(hdr.getSupplierInvoiceDate())
                .grossAmount(hdr.getGrossAmount())
                .netAmount(hdr.getNetAmount())
                .taxAmount(hdr.getTaxAmount())
                .discountAmount(hdr.getDiscountAmount())
                .paymentTerms(hdr.getPaymentTerms())
                .dueDate(hdr.getDueDate())
                .status(hdr.getStatus())
                .blockingReason(hdr.getBlockingReason())
                .items(itemDTOs)
                .build();
    }
}
