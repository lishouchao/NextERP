package com.nexterp.business.supply.application.service;

import com.nexterp.business.supply.domain.model.MmMaterialDocHdr;
import com.nexterp.business.supply.domain.model.MmMaterialDocItm;
import com.nexterp.business.supply.domain.model.MmStock;
import com.nexterp.business.supply.domain.repository.MmMaterialDocHdrRepository;
import com.nexterp.business.supply.domain.repository.MmStockRepository;
import com.nexterp.business.supply.dto.*;
import com.nexterp.business.supply.event.GoodsReceiptPostedEvent;
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
 * 库存管理服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MmInventoryService {

    private final MmMaterialDocHdrRepository materialDocHdrRepository;
    private final MmStockRepository stockRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 过账货物移动 (创建物料凭证, 更新库存)
     *
     * @param params 过账参数
     * @return 物料凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Long postGoodsMovement(Map<String, Object> params) {
        Long tenantId = (Long) params.get("tenantId");
        String movementType = (String) params.get("movementType");
        String headerText = (String) params.get("headerText");
        String refDocumentNo = (String) params.get("refDocumentNo");
        List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("items");

        String materialDocNumber = generateMaterialDocNumber();
        LocalDate today = LocalDate.now();

        MmMaterialDocHdr docHdr = MmMaterialDocHdr.builder()
                .materialDocument(materialDocNumber)
                .fiscalYear(today.getYear())
                .postingDate(today)
                .documentDate(today)
                .movementType(movementType)
                .headerText(headerText)
                .refDocumentNo(refDocumentNo)
                .tenantId(tenantId)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<MmMaterialDocItm> docItems = new ArrayList<>();

        if (items != null) {
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
                String stockType = (String) itemMap.getOrDefault("stockType", "1");

                MmMaterialDocItm docItm = MmMaterialDocItm.builder()
                        .lineItem(i + 1)
                        .materialId(materialId)
                        .materialCode(materialCode)
                        .plantId(plantId)
                        .plantCode(plantCode)
                        .slocId(slocId)
                        .slocCode(slocCode)
                        .movementType(movementType)
                        .stockType(stockType)
                        .quantity(quantity)
                        .unit(unit)
                        .amount(amount)
                        .docHdr(docHdr)
                        .build();
                docItems.add(docItm);

                if (amount != null) {
                    totalAmount = totalAmount.add(amount);
                }

                // 更新库存 (收货类移动增加, 发货类移动减少)
                updateStockForMovement(materialId, materialCode, plantId, plantCode,
                        slocId, slocCode, quantity, amount, tenantId, movementType, stockType);
            }
        }

        docHdr.setItems(docItems);
        MmMaterialDocHdr savedDoc = materialDocHdrRepository.save(docHdr);
        log.info("货物移动过账成功: materialDoc={}, movementType={}, totalAmount={}",
                materialDocNumber, movementType, totalAmount);

        // 发布收货过账事件
        eventPublisher.publishEvent(new GoodsReceiptPostedEvent(
                savedDoc.getId(),
                savedDoc.getMaterialDocument(),
                movementType,
                savedDoc.getTenantId(),
                refDocumentNo,
                totalAmount
        ));

        return savedDoc.getId();
    }

    /**
     * 获取物料凭证详情
     *
     * @param id 物料凭证ID
     * @return 物料凭证DTO
     */
    public MaterialDocDTO getMaterialDocById(Long id) {
        MmMaterialDocHdr hdr = materialDocHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("物料凭证不存在"));
        return convertToDTO(hdr);
    }

    /**
     * 分页查询物料凭证
     *
     * @param tenantId 租户ID
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    public PageResult<MaterialDocDTO> listMaterialDocs(Long tenantId, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<MmMaterialDocHdr> page = materialDocHdrRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageRequest);

        List<MaterialDocDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<MaterialDocDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 查询库存 (按物料/工厂/库位)
     *
     * @param materialId 物料ID
     * @param plantId    工厂ID
     * @param slocId     库存地点ID
     * @param tenantId   租户ID
     * @return 库存DTO
     */
    public StockDTO getStock(Long materialId, Long plantId, Long slocId, Long tenantId) {
        MmStock stock = stockRepository
                .findByMaterialIdAndPlantIdAndSlocIdAndTenantId(materialId, plantId, slocId, tenantId)
                .orElseThrow(() -> new BusinessException("库存记录不存在"));
        return convertStockToDTO(stock);
    }

    /**
     * 按物料查询库存
     *
     * @param materialId 物料ID
     * @param tenantId   租户ID
     * @return 库存列表
     */
    public List<StockDTO> listStockByMaterial(Long materialId, Long tenantId) {
        return stockRepository.findByMaterialIdAndTenantId(materialId, tenantId).stream()
                .map(this::convertStockToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 按工厂分页查询库存
     *
     * @param plantId  工厂ID
     * @param tenantId 租户ID
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    public PageResult<StockDTO> listStockByPlant(Long plantId, Long tenantId, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<MmStock> page = stockRepository.findByPlantIdAndTenantId(plantId, tenantId, pageRequest);

        List<StockDTO> records = page.getContent().stream()
                .map(this::convertStockToDTO)
                .collect(Collectors.toList());

        return PageResult.<StockDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 根据移动类型更新库存
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
     * @param movementType 移动类型
     * @param stockType    库存类型
     */
    private void updateStockForMovement(Long materialId, String materialCode, Long plantId, String plantCode,
                                         Long slocId, String slocCode, BigDecimal quantity, BigDecimal amount,
                                         Long tenantId, String movementType, String stockType) {
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
                        .currency("CNY")
                        .tenantId(tenantId)
                        .build());

        // 收货类移动(101等)增加库存, 发货类移动(201/261等)减少库存
        boolean isReceipt = "101".equals(movementType) || "102".equals(movementType) || "105".equals(movementType);
        if (isReceipt) {
            if ("1".equals(stockType)) {
                stock.setUnrestrictedStock(stock.getUnrestrictedStock().add(quantity));
                if (amount != null) {
                    stock.setUnrestrictedValue(stock.getUnrestrictedValue().add(amount));
                }
            } else if ("2".equals(stockType)) {
                stock.setQualityStock(stock.getQualityStock().add(quantity));
                if (amount != null) {
                    stock.setQualityValue(stock.getQualityValue().add(amount));
                }
            }
        } else {
            if ("1".equals(stockType)) {
                stock.setUnrestrictedStock(stock.getUnrestrictedStock().subtract(quantity));
                if (amount != null) {
                    stock.setUnrestrictedValue(stock.getUnrestrictedValue().subtract(amount));
                }
            }
        }

        stockRepository.save(stock);
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
     * 转换物料凭证为DTO
     *
     * @param hdr 物料凭证头实体
     * @return 物料凭证DTO
     */
    private MaterialDocDTO convertToDTO(MmMaterialDocHdr hdr) {
        List<MaterialDocItemDTO> itemDTOs = hdr.getItems().stream()
                .map(itm -> MaterialDocItemDTO.builder()
                        .id(itm.getId())
                        .lineItem(itm.getLineItem())
                        .materialId(itm.getMaterialId())
                        .materialCode(itm.getMaterialCode())
                        .plantId(itm.getPlantId())
                        .plantCode(itm.getPlantCode())
                        .slocId(itm.getSlocId())
                        .slocCode(itm.getSlocCode())
                        .batch(itm.getBatch())
                        .movementType(itm.getMovementType())
                        .stockType(itm.getStockType())
                        .quantity(itm.getQuantity())
                        .unit(itm.getUnit())
                        .amount(itm.getAmount())
                        .purchaseOrder(itm.getPurchaseOrder())
                        .poItem(itm.getPoItem())
                        .vendorId(itm.getVendorId())
                        .customerId(itm.getCustomerId())
                        .salesOrder(itm.getSalesOrder())
                        .costCenter(itm.getCostCenter())
                        .reasonForMovement(itm.getReasonForMovement())
                        .itemText(itm.getItemText())
                        .build())
                .collect(Collectors.toList());

        return MaterialDocDTO.builder()
                .id(hdr.getId())
                .materialDocument(hdr.getMaterialDocument())
                .fiscalYear(hdr.getFiscalYear())
                .postingDate(hdr.getPostingDate())
                .documentDate(hdr.getDocumentDate())
                .movementType(hdr.getMovementType())
                .transactionCode(hdr.getTransactionCode())
                .headerText(hdr.getHeaderText())
                .refDocumentNo(hdr.getRefDocumentNo())
                .items(itemDTOs)
                .build();
    }

    /**
     * 转换库存为DTO
     *
     * @param stock 库存实体
     * @return 库存DTO
     */
    private StockDTO convertStockToDTO(MmStock stock) {
        return StockDTO.builder()
                .id(stock.getId())
                .materialId(stock.getMaterialId())
                .materialCode(stock.getMaterialCode())
                .plantId(stock.getPlantId())
                .plantCode(stock.getPlantCode())
                .slocId(stock.getSlocId())
                .slocCode(stock.getSlocCode())
                .unrestrictedStock(stock.getUnrestrictedStock())
                .qualityStock(stock.getQualityStock())
                .blockedStock(stock.getBlockedStock())
                .unrestrictedValue(stock.getUnrestrictedValue())
                .qualityValue(stock.getQualityValue())
                .blockedValue(stock.getBlockedValue())
                .currency(stock.getCurrency())
                .build();
    }
}
