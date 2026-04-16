package com.nexterp.business.supply.application.service;

import com.nexterp.business.supply.domain.model.MmPurchaseReqHdr;
import com.nexterp.business.supply.domain.model.MmPurchaseReqItm;
import com.nexterp.business.supply.domain.repository.MmPurchaseReqHdrRepository;
import com.nexterp.business.supply.dto.*;
import com.nexterp.business.supply.event.PurchaseReqApprovedEvent;
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
 * 采购申请服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MmPurchaseReqService {

    private final MmPurchaseReqHdrRepository purchaseReqHdrRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建采购申请
     *
     * @param request 创建采购申请请求
     * @return 采购申请ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseReq(CreatePurchaseReqRequest request) {
        String prNumber = generatePrNumber();

        MmPurchaseReqHdr hdr = MmPurchaseReqHdr.builder()
                .prNumber(prNumber)
                .prType(request.getPrType() != null ? request.getPrType() : "NB")
                .purchasingGroup(request.getPurchasingGroup())
                .purchasingOrg(request.getPurchasingOrg())
                .plantId(request.getPlantId())
                .plantCode(request.getPlantCode())
                .documentDate(request.getDocumentDate() != null ? request.getDocumentDate() : LocalDate.now())
                .deliveryDate(request.getDeliveryDate())
                .totalValue(BigDecimal.ZERO)
                .currency("CNY")
                .headerText(request.getHeaderText())
                .status("0")
                .approvalStatus("0")
                .tenantId(request.getTenantId())
                .build();

        // 构建行项目
        BigDecimal totalValue = BigDecimal.ZERO;
        List<MmPurchaseReqItm> items = new ArrayList<>();
        if (request.getItems() != null) {
            for (CreatePurchaseReqRequest.CreatePurchaseReqItemRequest itemReq : request.getItems()) {
                BigDecimal itemValue = itemReq.getPrice() != null && itemReq.getQuantity() != null
                        ? itemReq.getPrice().multiply(itemReq.getQuantity())
                        : BigDecimal.ZERO;

                MmPurchaseReqItm itm = MmPurchaseReqItm.builder()
                        .prItem(itemReq.getPrItem())
                        .materialId(itemReq.getMaterialId())
                        .materialCode(itemReq.getMaterialCode())
                        .shortText(itemReq.getShortText())
                        .materialGroup(itemReq.getMaterialGroup())
                        .quantity(itemReq.getQuantity())
                        .unit(itemReq.getUnit())
                        .price(itemReq.getPrice())
                        .priceUnit(itemReq.getPriceUnit() != null ? itemReq.getPriceUnit() : 1)
                        .deliveryDate(itemReq.getDeliveryDate())
                        .plantId(itemReq.getPlantId())
                        .plantCode(itemReq.getPlantCode())
                        .slocId(itemReq.getSlocId())
                        .slocCode(itemReq.getSlocCode())
                        .costCenter(itemReq.getCostCenter())
                        .itemCategory(itemReq.getItemCategory())
                        .status("0")
                        .reqHdr(hdr)
                        .build();
                items.add(itm);
                totalValue = totalValue.add(itemValue);
            }
        }
        hdr.setItems(items);
        hdr.setTotalValue(totalValue);

        MmPurchaseReqHdr saved = purchaseReqHdrRepository.save(hdr);
        log.info("创建采购申请成功: prNumber={}, totalValue={}", prNumber, totalValue);

        return saved.getId();
    }

    /**
     * 获取采购申请详情
     *
     * @param id 采购申请ID
     * @return 采购申请DTO
     */
    public PurchaseReqDTO getPurchaseReqById(Long id) {
        MmPurchaseReqHdr hdr = purchaseReqHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购申请不存在"));
        return convertToDTO(hdr);
    }

    /**
     * 分页查询采购申请
     *
     * @param tenantId 租户ID
     * @param status   状态 (可选)
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    public PageResult<PurchaseReqDTO> listPurchaseReqs(Long tenantId, String status, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<MmPurchaseReqHdr> page;

        if (status != null && !status.isEmpty()) {
            page = purchaseReqHdrRepository.findByTenantIdAndStatusAndIsDeletedFalse(tenantId, status, pageRequest);
        } else {
            page = purchaseReqHdrRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageRequest);
        }

        List<PurchaseReqDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<PurchaseReqDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 提交采购申请 (状态 0 -> 1)
     *
     * @param id 采购申请ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitPurchaseReq(Long id) {
        MmPurchaseReqHdr hdr = purchaseReqHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购申请不存在"));

        if (!"0".equals(hdr.getStatus())) {
            throw new BusinessException("仅草稿状态的采购申请允许提交");
        }

        hdr.setStatus("1");
        hdr.setApprovalStatus("1");
        purchaseReqHdrRepository.save(hdr);
        log.info("提交采购申请成功: id={}, prNumber={}", id, hdr.getPrNumber());
    }

    /**
     * 审批通过采购申请 (状态 1 -> 2)
     *
     * @param id         采购申请ID
     * @param approvedBy 审批人
     */
    @Transactional(rollbackFor = Exception.class)
    public void approvePurchaseReq(Long id, String approvedBy) {
        MmPurchaseReqHdr hdr = purchaseReqHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购申请不存在"));

        if (!"1".equals(hdr.getStatus())) {
            throw new BusinessException("仅已提交状态的采购申请允许审批");
        }

        hdr.setStatus("2");
        hdr.setApprovalStatus("2");
        hdr.setApprovedBy(approvedBy);
        hdr.setApprovedDate(LocalDate.now());
        purchaseReqHdrRepository.save(hdr);
        log.info("审批通过采购申请: id={}, prNumber={}, approvedBy={}", id, hdr.getPrNumber(), approvedBy);

        // 发布审批通过事件
        eventPublisher.publishEvent(new PurchaseReqApprovedEvent(
                hdr.getId(),
                hdr.getPrNumber(),
                hdr.getTenantId(),
                approvedBy
        ));
    }

    /**
     * 审批拒绝采购申请 (状态 1 -> 0)
     *
     * @param id          采购申请ID
     * @param rejectedBy  拒绝人
     * @param reason      拒绝原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectPurchaseReq(Long id, String rejectedBy, String reason) {
        MmPurchaseReqHdr hdr = purchaseReqHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购申请不存在"));

        if (!"1".equals(hdr.getStatus())) {
            throw new BusinessException("仅已提交状态的采购申请允许拒绝");
        }

        hdr.setStatus("0");
        hdr.setApprovalStatus("3");
        purchaseReqHdrRepository.save(hdr);
        log.info("审批拒绝采购申请: id={}, prNumber={}, rejectedBy={}, reason={}",
                id, hdr.getPrNumber(), rejectedBy, reason);
    }

    /**
     * 将采购申请转为采购订单 (状态 -> 4)
     *
     * @param id 采购申请ID
     * @return 采购订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long convertToPO(Long id) {
        MmPurchaseReqHdr hdr = purchaseReqHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购申请不存在"));

        if (!"2".equals(hdr.getStatus())) {
            throw new BusinessException("仅已审批状态的采购申请允许转采购订单");
        }

        // 标记为已转订单
        hdr.setStatus("4");
        purchaseReqHdrRepository.save(hdr);
        log.info("采购申请转采购订单: id={}, prNumber={}", id, hdr.getPrNumber());

        // 实际创建采购订单的逻辑由事件监听器或调用MmPurchaseOrderService完成
        // 此处返回0L作为占位符
        return 0L;
    }

    /**
     * 生成采购申请号 (格式: "PR" + 8位顺序号)
     *
     * @return 采购申请号
     */
    private String generatePrNumber() {
        long count = purchaseReqHdrRepository.count() + 1;
        return "PR" + String.format("%08d", count);
    }

    /**
     * 转换为DTO
     *
     * @param hdr 采购申请头实体
     * @return 采购申请DTO
     */
    private PurchaseReqDTO convertToDTO(MmPurchaseReqHdr hdr) {
        List<PurchaseReqItemDTO> itemDTOs = hdr.getItems().stream()
                .map(itm -> PurchaseReqItemDTO.builder()
                        .id(itm.getId())
                        .prItem(itm.getPrItem())
                        .materialId(itm.getMaterialId())
                        .materialCode(itm.getMaterialCode())
                        .shortText(itm.getShortText())
                        .materialGroup(itm.getMaterialGroup())
                        .quantity(itm.getQuantity())
                        .unit(itm.getUnit())
                        .price(itm.getPrice())
                        .priceUnit(itm.getPriceUnit())
                        .deliveryDate(itm.getDeliveryDate())
                        .plantId(itm.getPlantId())
                        .plantCode(itm.getPlantCode())
                        .slocId(itm.getSlocId())
                        .slocCode(itm.getSlocCode())
                        .costCenter(itm.getCostCenter())
                        .itemCategory(itm.getItemCategory())
                        .status(itm.getStatus())
                        .build())
                .collect(Collectors.toList());

        return PurchaseReqDTO.builder()
                .id(hdr.getId())
                .prNumber(hdr.getPrNumber())
                .prType(hdr.getPrType())
                .purchasingGroup(hdr.getPurchasingGroup())
                .purchasingOrg(hdr.getPurchasingOrg())
                .plantId(hdr.getPlantId())
                .plantCode(hdr.getPlantCode())
                .documentDate(hdr.getDocumentDate())
                .deliveryDate(hdr.getDeliveryDate())
                .totalValue(hdr.getTotalValue())
                .currency(hdr.getCurrency())
                .headerText(hdr.getHeaderText())
                .status(hdr.getStatus())
                .approvalStatus(hdr.getApprovalStatus())
                .approvedBy(hdr.getApprovedBy())
                .approvedDate(hdr.getApprovedDate())
                .items(itemDTOs)
                .build();
    }
}
