package com.nexterp.business.supply.application.service;

import com.nexterp.business.supply.domain.model.*;
import com.nexterp.business.supply.domain.repository.*;
import com.nexterp.business.supply.dto.*;
import com.nexterp.business.supply.event.GoodsReceiptPostedEvent;
import com.nexterp.business.supply.event.PurchaseOrderApprovedEvent;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购订单服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MmPurchaseOrderService {

    private final MmPurchaseOrderHdrRepository purchaseOrderHdrRepository;
    private final MmPurchaseOrderItmRepository purchaseOrderItmRepository;
    private final MmMaterialDocHdrRepository materialDocHdrRepository;
    private final MmMaterialDocItmRepository materialDocItmRepository;
    private final MmStockRepository stockRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建采购订单
     *
     * @param request 创建采购订单请求
     * @return 采购订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseOrder(CreatePurchaseOrderRequest request) {
        String poNumber = generatePoNumber();

        MmPurchaseOrderHdr hdr = MmPurchaseOrderHdr.builder()
                .poNumber(poNumber)
                .poType(request.getPoType() != null ? request.getPoType() : "NB")
                .vendorId(request.getVendorId())
                .vendorCode(request.getVendorCode())
                .purchasingOrg(request.getPurchasingOrg())
                .purchasingGroup(request.getPurchasingGroup())
                .companyId(request.getCompanyId())
                .companyCode(request.getCompanyCode())
                .currency(request.getCurrency() != null ? request.getCurrency() : "CNY")
                .exchangeRate(request.getExchangeRate())
                .documentDate(request.getDocumentDate() != null ? request.getDocumentDate() : LocalDate.now())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .termsOfPayment(request.getTermsOfPayment())
                .incoterms1(request.getIncoterms1())
                .incoterms2(request.getIncoterms2())
                .headerText(request.getHeaderText())
                .status("0")
                .releaseStatus("0")
                .totalNetValue(BigDecimal.ZERO)
                .totalTaxAmount(BigDecimal.ZERO)
                .totalGrossValue(BigDecimal.ZERO)
                .tenantId(request.getTenantId())
                .build();

        // 构建行项目
        BigDecimal totalNetValue = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        List<MmPurchaseOrderItm> items = new ArrayList<>();
        if (request.getItems() != null) {
            for (CreatePurchaseOrderRequest.CreatePurchaseOrderItemRequest itemReq : request.getItems()) {
                BigDecimal netValue = itemReq.getPrice() != null && itemReq.getQuantity() != null
                        ? itemReq.getPrice().multiply(itemReq.getQuantity())
                        : BigDecimal.ZERO;
                BigDecimal taxAmount = BigDecimal.ZERO;

                MmPurchaseOrderItm itm = MmPurchaseOrderItm.builder()
                        .poItem(itemReq.getPoItem())
                        .materialId(itemReq.getMaterialId())
                        .materialCode(itemReq.getMaterialCode())
                        .shortText(itemReq.getShortText())
                        .materialGroup(itemReq.getMaterialGroup())
                        .quantity(itemReq.getQuantity())
                        .unit(itemReq.getUnit())
                        .price(itemReq.getPrice())
                        .priceUnit(itemReq.getPriceUnit() != null ? itemReq.getPriceUnit() : 1)
                        .netValue(netValue)
                        .taxCode(itemReq.getTaxCode())
                        .taxAmount(taxAmount)
                        .plantId(itemReq.getPlantId())
                        .plantCode(itemReq.getPlantCode())
                        .slocId(itemReq.getSlocId())
                        .slocCode(itemReq.getSlocCode())
                        .deliveryDate(itemReq.getDeliveryDate())
                        .quantityDelivered(BigDecimal.ZERO)
                        .quantityInvoiced(BigDecimal.ZERO)
                        .itemCategory(itemReq.getItemCategory())
                        .acctAssignmentCat(itemReq.getAcctAssignmentCat())
                        .costCenter(itemReq.getCostCenter())
                        .deletionFlag("0")
                        .poHdr(hdr)
                        .build();
                items.add(itm);
                totalNetValue = totalNetValue.add(netValue);
                totalTaxAmount = totalTaxAmount.add(taxAmount);
            }
        }
        hdr.setItems(items);
        hdr.setTotalNetValue(totalNetValue);
        hdr.setTotalTaxAmount(totalTaxAmount);
        hdr.setTotalGrossValue(totalNetValue.add(totalTaxAmount));

        MmPurchaseOrderHdr saved = purchaseOrderHdrRepository.save(hdr);
        log.info("创建采购订单成功: poNumber={}, totalNetValue={}", poNumber, totalNetValue);

        return saved.getId();
    }

    /**
     * 获取采购订单详情
     *
     * @param id 采购订单ID
     * @return 采购订单DTO
     */
    public PurchaseOrderDTO getPurchaseOrderById(Long id) {
        MmPurchaseOrderHdr hdr = purchaseOrderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购订单不存在"));
        return convertToDTO(hdr);
    }

    /**
     * 分页查询采购订单
     *
     * @param tenantId 租户ID
     * @param status   状态 (可选)
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    public PageResult<PurchaseOrderDTO> listPurchaseOrders(Long tenantId, String status, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<MmPurchaseOrderHdr> page;

        if (status != null && !status.isEmpty()) {
            page = purchaseOrderHdrRepository.findByTenantIdAndStatusAndIsDeletedFalse(tenantId, status, pageRequest);
        } else {
            page = purchaseOrderHdrRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageRequest);
        }

        List<PurchaseOrderDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<PurchaseOrderDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 提交采购订单 (状态 0 -> 1)
     *
     * @param id 采购订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitPurchaseOrder(Long id) {
        MmPurchaseOrderHdr hdr = purchaseOrderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购订单不存在"));

        if (!"0".equals(hdr.getStatus())) {
            throw new BusinessException("仅草稿状态的采购订单允许提交");
        }

        hdr.setStatus("1");
        hdr.setReleaseStatus("1");
        purchaseOrderHdrRepository.save(hdr);
        log.info("提交采购订单成功: id={}, poNumber={}", id, hdr.getPoNumber());
    }

    /**
     * 审批通过采购订单 (状态 1 -> 2)
     *
     * @param id         采购订单ID
     * @param approvedBy 审批人
     */
    @Transactional(rollbackFor = Exception.class)
    public void approvePurchaseOrder(Long id, String approvedBy) {
        MmPurchaseOrderHdr hdr = purchaseOrderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购订单不存在"));

        if (!"1".equals(hdr.getStatus())) {
            throw new BusinessException("仅已提交状态的采购订单允许审批");
        }

        hdr.setStatus("2");
        hdr.setReleaseStatus("2");
        purchaseOrderHdrRepository.save(hdr);
        log.info("审批通过采购订单: id={}, poNumber={}, approvedBy={}", id, hdr.getPoNumber(), approvedBy);

        // 发布审批通过事件
        eventPublisher.publishEvent(new PurchaseOrderApprovedEvent(
                hdr.getId(),
                hdr.getPoNumber(),
                hdr.getTenantId(),
                approvedBy
        ));
    }

    /**
     * 采购订单收货 (移动类型101)
     *
     * @param id    采购订单ID
     * @param items 收货明细列表
     * @return 物料凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long receiveGoods(Long id, List<Map<String, Object>> items) {
        MmPurchaseOrderHdr hdr = purchaseOrderHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购订单不存在"));

        if (!"2".equals(hdr.getStatus())) {
            throw new BusinessException("仅已审批状态的采购订单允许收货");
        }

        // 生成物料凭证号
        String materialDocNumber = generateMaterialDocNumber();
        LocalDate today = LocalDate.now();

        MmMaterialDocHdr docHdr = MmMaterialDocHdr.builder()
                .materialDocument(materialDocNumber)
                .fiscalYear(today.getYear())
                .postingDate(today)
                .documentDate(today)
                .movementType("101")
                .transactionCode("MIGO")
                .headerText("PO收货: " + hdr.getPoNumber())
                .refDocumentNo(hdr.getPoNumber())
                .tenantId(hdr.getTenantId())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<MmMaterialDocItm> docItems = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> itemMap = items.get(i);
            Long materialId = (Long) itemMap.get("materialId");
            String materialCode = (String) itemMap.get("materialCode");
            Long plantId = (Long) itemMap.get("plantId");
            String plantCode = (String) itemMap.get("plantCode");
            Long slocId = (Long) itemMap.get("slocId");
            String slocCode = (String) itemMap.get("slocCode");
            BigDecimal quantity = (BigDecimal) itemMap.get("quantity");
            String unit = (String) itemMap.get("unit");
            BigDecimal amount = (BigDecimal) itemMap.get("amount");

            MmMaterialDocItm docItm = MmMaterialDocItm.builder()
                    .lineItem(i + 1)
                    .materialId(materialId)
                    .materialCode(materialCode)
                    .plantId(plantId)
                    .plantCode(plantCode)
                    .slocId(slocId)
                    .slocCode(slocCode)
                    .movementType("101")
                    .stockType("1")
                    .quantity(quantity)
                    .unit(unit)
                    .amount(amount)
                    .purchaseOrder(hdr.getPoNumber())
                    .vendorId(hdr.getVendorId())
                    .docHdr(docHdr)
                    .build();
            docItems.add(docItm);

            if (amount != null) {
                totalAmount = totalAmount.add(amount);
            }

            // 更新采购订单行已交货数量
            Integer poItem = (Integer) itemMap.get("poItem");
            if (poItem != null) {
                for (MmPurchaseOrderItm poItm : hdr.getItems()) {
                    if (poItm.getPoItem().equals(poItem)) {
                        poItm.setQuantityDelivered(poItm.getQuantityDelivered().add(quantity));
                        break;
                    }
                }
            }

            // 更新库存
            updateStock(materialId, materialCode, plantId, plantCode, slocId, slocCode,
                    quantity, amount, hdr.getTenantId(), "CNY");
        }

        docHdr.setItems(docItems);
        MmMaterialDocHdr savedDoc = materialDocHdrRepository.save(docHdr);

        // 检查是否所有行项目都已完全交货
        boolean allDelivered = hdr.getItems().stream()
                .allMatch(itm -> itm.getQuantityDelivered().compareTo(itm.getQuantity()) >= 0);
        if (allDelivered) {
            hdr.setStatus("3");
        }

        purchaseOrderHdrRepository.save(hdr);
        log.info("采购订单收货成功: poId={}, materialDocId={}, totalAmount={}", id, savedDoc.getId(), totalAmount);

        // 发布收货过账事件
        eventPublisher.publishEvent(new GoodsReceiptPostedEvent(
                savedDoc.getId(),
                savedDoc.getMaterialDocument(),
                "101",
                savedDoc.getTenantId(),
                hdr.getPoNumber(),
                totalAmount
        ));

        return savedDoc.getId();
    }

    /**
     * 更新库存 (收货增加库存)
     *
     * @param materialId   物料ID
     * @param materialCode 物料编码
     * @param plantId      工厂ID
     * @param plantCode    工厂代码
     * @param slocId       库存地点ID
     * @param slocCode     库存地点代码
     * @param quantity     数量
     * @param amount       金额
     * @param tenantId     租户ID
     * @param currency     币种
     */
    private void updateStock(Long materialId, String materialCode, Long plantId, String plantCode,
                             Long slocId, String slocCode, BigDecimal quantity, BigDecimal amount,
                             Long tenantId, String currency) {
        MmStock stock = stockRepository
                .findByMaterialIdAndPlantIdAndSlocIdAndTenantId(materialId, plantId, slocId, tenantId)
                .orElseGet(() -> MmStock.builder()
                        .materialId(materialId)
                        .materialCode(materialCode)
                        .plantId(plantId)
                        .plantCode(plantCode)
                        .slocId(slocId)
                        .slocCode(slocCode)
                        .unrestrictedStock(BigDecimal.ZERO)
                        .qualityStock(BigDecimal.ZERO)
                        .blockedStock(BigDecimal.ZERO)
                        .unrestrictedValue(BigDecimal.ZERO)
                        .qualityValue(BigDecimal.ZERO)
                        .blockedValue(BigDecimal.ZERO)
                        .currency(currency)
                        .tenantId(tenantId)
                        .build());

        stock.setUnrestrictedStock(stock.getUnrestrictedStock().add(quantity));
        if (amount != null) {
            stock.setUnrestrictedValue(stock.getUnrestrictedValue().add(amount));
        }
        stockRepository.save(stock);
    }

    /**
     * 生成采购订单号 (格式: "45" + 8位顺序号)
     *
     * @return 采购订单号
     */
    private String generatePoNumber() {
        long count = purchaseOrderHdrRepository.count() + 1;
        return "45" + String.format("%08d", count);
    }

    /**
     * 生成物料凭证号
     *
     * @return 物料凭证号
     */
    private String generateMaterialDocNumber() {
        long count = materialDocHdrRepository.count() + 1;
        return String.format("%010d", count);
    }

    /**
     * 转换为DTO
     *
     * @param hdr 采购订单头实体
     * @return 采购订单DTO
     */
    private PurchaseOrderDTO convertToDTO(MmPurchaseOrderHdr hdr) {
        List<PurchaseOrderItemDTO> itemDTOs = hdr.getItems().stream()
                .map(itm -> PurchaseOrderItemDTO.builder()
                        .id(itm.getId())
                        .poItem(itm.getPoItem())
                        .materialId(itm.getMaterialId())
                        .materialCode(itm.getMaterialCode())
                        .shortText(itm.getShortText())
                        .materialGroup(itm.getMaterialGroup())
                        .quantity(itm.getQuantity())
                        .unit(itm.getUnit())
                        .price(itm.getPrice())
                        .priceUnit(itm.getPriceUnit())
                        .netValue(itm.getNetValue())
                        .taxCode(itm.getTaxCode())
                        .taxAmount(itm.getTaxAmount())
                        .plantId(itm.getPlantId())
                        .plantCode(itm.getPlantCode())
                        .slocId(itm.getSlocId())
                        .slocCode(itm.getSlocCode())
                        .deliveryDate(itm.getDeliveryDate())
                        .quantityDelivered(itm.getQuantityDelivered())
                        .quantityInvoiced(itm.getQuantityInvoiced())
                        .itemCategory(itm.getItemCategory())
                        .acctAssignmentCat(itm.getAcctAssignmentCat())
                        .costCenter(itm.getCostCenter())
                        .deletionFlag(itm.getDeletionFlag())
                        .build())
                .collect(Collectors.toList());

        return PurchaseOrderDTO.builder()
                .id(hdr.getId())
                .poNumber(hdr.getPoNumber())
                .poType(hdr.getPoType())
                .vendorId(hdr.getVendorId())
                .vendorCode(hdr.getVendorCode())
                .purchasingOrg(hdr.getPurchasingOrg())
                .purchasingGroup(hdr.getPurchasingGroup())
                .companyId(hdr.getCompanyId())
                .companyCode(hdr.getCompanyCode())
                .currency(hdr.getCurrency())
                .exchangeRate(hdr.getExchangeRate())
                .documentDate(hdr.getDocumentDate())
                .validFrom(hdr.getValidFrom())
                .validTo(hdr.getValidTo())
                .termsOfPayment(hdr.getTermsOfPayment())
                .incoterms1(hdr.getIncoterms1())
                .incoterms2(hdr.getIncoterms2())
                .status(hdr.getStatus())
                .releaseStatus(hdr.getReleaseStatus())
                .headerText(hdr.getHeaderText())
                .totalNetValue(hdr.getTotalNetValue())
                .totalTaxAmount(hdr.getTotalTaxAmount())
                .totalGrossValue(hdr.getTotalGrossValue())
                .items(itemDTOs)
                .build();
    }
}
