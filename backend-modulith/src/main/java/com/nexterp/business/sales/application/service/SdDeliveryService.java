package com.nexterp.business.sales.application.service;

import com.nexterp.business.sales.domain.model.SdDeliveryHdr;
import com.nexterp.business.sales.domain.model.SdDeliveryItm;
import com.nexterp.business.sales.domain.repository.SdDeliveryHdrRepository;
import com.nexterp.business.sales.dto.CreateDeliveryRequest;
import com.nexterp.business.sales.dto.DeliveryDTO;
import com.nexterp.business.sales.dto.DeliveryItemDTO;
import com.nexterp.business.sales.event.DeliveryCreatedEvent;
import com.nexterp.business.sales.event.GoodsIssuePostedEvent;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 交货单服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdDeliveryService {

    private final SdDeliveryHdrRepository deliveryHdrRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建交货单
     *
     * @param request 创建交货单请求
     * @return 交货单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createDelivery(CreateDeliveryRequest request) {
        // 生成交货单号: "8" + 7位顺序号
        String deliveryNumber = generateDeliveryNumber();

        // 构建交货单头
        SdDeliveryHdr hdr = SdDeliveryHdr.builder()
                .deliveryNumber(deliveryNumber)
                .deliveryType(request.getDeliveryType())
                .salesOrgId(request.getSalesOrgId())
                .distributionChannel(request.getDistributionChannel())
                .division(request.getDivision())
                .soldToParty(request.getSoldToParty())
                .shipToParty(request.getShipToParty())
                .documentDate(request.getDocumentDate() != null ? request.getDocumentDate() : LocalDate.now())
                .plannedGiDate(request.getPlannedGiDate())
                .shippingPoint(request.getShippingPoint())
                .orderId(request.getOrderId())
                .remark(request.getRemark())
                .deliveryStatus("01")
                .pickingStatus("A")
                .giStatus("A")
                .tenantId(request.getTenantId())
                .build();

        // 构建交货单项
        if (request.getItems() != null) {
            for (CreateDeliveryRequest.CreateDeliveryItemRequest itemReq : request.getItems()) {
                SdDeliveryItm itm = SdDeliveryItm.builder()
                        .itemNumber(itemReq.getItemNumber())
                        .materialId(itemReq.getMaterialId())
                        .materialCode(itemReq.getMaterialCode())
                        .description(itemReq.getDescription())
                        .deliveryQty(itemReq.getDeliveryQty())
                        .pickedQty(BigDecimal.ZERO)
                        .salesUnit(itemReq.getSalesUnit())
                        .plantId(itemReq.getPlantId())
                        .slocId(itemReq.getSlocId())
                        .orderId(request.getOrderId())
                        .orderItemId(itemReq.getOrderItemId())
                        .deliveryHdr(hdr)
                        .build();
                hdr.getItems().add(itm);
            }
        }

        SdDeliveryHdr saved = deliveryHdrRepository.save(hdr);
        log.info("创建交货单成功: deliveryNumber={}", deliveryNumber);

        // 发布交货单创建事件
        eventPublisher.publishEvent(new DeliveryCreatedEvent(
                saved.getId(),
                saved.getDeliveryNumber(),
                saved.getDeliveryType(),
                saved.getTenantId(),
                saved.getOrderId()
        ));

        return saved.getId();
    }

    /**
     * 更新交货单 (仅状态"01")
     *
     * @param id      交货单ID
     * @param request 创建交货单请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDelivery(Long id, CreateDeliveryRequest request) {
        SdDeliveryHdr hdr = deliveryHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("交货单不存在"));

        if (!"01".equals(hdr.getDeliveryStatus())) {
            throw new BusinessException("仅未处理状态的交货单允许修改");
        }

        // 更新交货单头基本信息
        hdr.setDeliveryType(request.getDeliveryType());
        hdr.setSalesOrgId(request.getSalesOrgId());
        hdr.setDistributionChannel(request.getDistributionChannel());
        hdr.setDivision(request.getDivision());
        hdr.setSoldToParty(request.getSoldToParty());
        hdr.setShipToParty(request.getShipToParty());
        hdr.setDocumentDate(request.getDocumentDate());
        hdr.setPlannedGiDate(request.getPlannedGiDate());
        hdr.setShippingPoint(request.getShippingPoint());
        hdr.setRemark(request.getRemark());

        // 清除旧行项目并重建
        hdr.getItems().clear();
        if (request.getItems() != null) {
            for (CreateDeliveryRequest.CreateDeliveryItemRequest itemReq : request.getItems()) {
                SdDeliveryItm itm = SdDeliveryItm.builder()
                        .itemNumber(itemReq.getItemNumber())
                        .materialId(itemReq.getMaterialId())
                        .materialCode(itemReq.getMaterialCode())
                        .description(itemReq.getDescription())
                        .deliveryQty(itemReq.getDeliveryQty())
                        .pickedQty(BigDecimal.ZERO)
                        .salesUnit(itemReq.getSalesUnit())
                        .plantId(itemReq.getPlantId())
                        .slocId(itemReq.getSlocId())
                        .orderId(request.getOrderId())
                        .orderItemId(itemReq.getOrderItemId())
                        .deliveryHdr(hdr)
                        .build();
                hdr.getItems().add(itm);
            }
        }

        deliveryHdrRepository.save(hdr);
        log.info("更新交货单成功: id={}", id);
    }

    /**
     * 获取交货单详情
     *
     * @param id 交货单ID
     * @return 交货单DTO
     */
    public DeliveryDTO getDeliveryById(Long id) {
        SdDeliveryHdr hdr = deliveryHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("交货单不存在"));
        return convertToDTO(hdr);
    }

    /**
     * 分页查询交货单
     *
     * @param tenantId       租户ID
     * @param deliveryStatus 交货状态 (可选)
     * @param current        当前页
     * @param size           每页大小
     * @return 分页结果
     */
    public PageResult<DeliveryDTO> listDeliveries(Long tenantId, String deliveryStatus, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<SdDeliveryHdr> page;

        if (deliveryStatus != null && !deliveryStatus.isEmpty()) {
            page = deliveryHdrRepository.findByTenantIdAndDeliveryStatusAndIsDeletedFalse(
                    tenantId, deliveryStatus, pageRequest);
        } else {
            page = deliveryHdrRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageRequest);
        }

        List<DeliveryDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<DeliveryDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 拣配交货单
     * 更新拣配数量, 如完全拣配则更新状态为"03"
     *
     * @param id        交货单ID
     * @param pickItems 拣配项列表 (每项包含 itemNumber 和 pickedQty)
     */
    @Transactional(rollbackFor = Exception.class)
    public void pickDelivery(Long id, List<Map<String, Object>> pickItems) {
        SdDeliveryHdr hdr = deliveryHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("交货单不存在"));

        boolean allFullyPicked = true;
        for (Map<String, Object> pickItem : pickItems) {
            Integer itemNumber = (Integer) pickItem.get("itemNumber");
            BigDecimal pickedQty = new BigDecimal(pickItem.get("pickedQty").toString());

            SdDeliveryItm targetItem = hdr.getItems().stream()
                    .filter(itm -> itm.getItemNumber().equals(itemNumber))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("交货单项不存在: itemNumber=" + itemNumber));

            targetItem.setPickedQty(pickedQty);
            if (pickedQty.compareTo(targetItem.getDeliveryQty()) < 0) {
                allFullyPicked = false;
            }
        }

        // 更新拣配状态
        if (allFullyPicked) {
            hdr.setPickingStatus("C");
            hdr.setDeliveryStatus("03");
            log.info("交货单完全拣配: id={}", id);
        } else {
            hdr.setPickingStatus("B");
            log.info("交货单部分拣配: id={}", id);
        }

        deliveryHdrRepository.save(hdr);
    }

    /**
     * 发货过账 (状态 "03" -> "04", gi_status -> "B")
     *
     * @param id           交货单ID
     * @param actualGiDate 实际发货日期
     */
    @Transactional(rollbackFor = Exception.class)
    public void postGoodsIssue(Long id, LocalDate actualGiDate) {
        SdDeliveryHdr hdr = deliveryHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("交货单不存在"));

        if (!"03".equals(hdr.getDeliveryStatus())) {
            throw new BusinessException("仅已拣配状态的交货单允许发货过账");
        }

        hdr.setDeliveryStatus("04");
        hdr.setGiStatus("B");
        hdr.setActualGiDate(actualGiDate != null ? actualGiDate : LocalDate.now());
        deliveryHdrRepository.save(hdr);
        log.info("发货过账成功: id={}, deliveryNumber={}", id, hdr.getDeliveryNumber());

        // 发布发货过账事件
        eventPublisher.publishEvent(new GoodsIssuePostedEvent(
                hdr.getId(),
                hdr.getDeliveryNumber(),
                hdr.getTenantId(),
                hdr.getActualGiDate()
        ));
    }

    /**
     * 取消交货单 (仅状态"01")
     *
     * @param id 交货单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelDelivery(Long id) {
        SdDeliveryHdr hdr = deliveryHdrRepository.findById(id)
                .orElseThrow(() -> new BusinessException("交货单不存在"));

        if (!"01".equals(hdr.getDeliveryStatus())) {
            throw new BusinessException("仅未处理状态的交货单允许取消");
        }

        hdr.setIsDeleted(true);
        deliveryHdrRepository.save(hdr);
        log.info("取消交货单成功: id={}", id);
    }

    /**
     * 生成交货单号 (格式: "8" + 7位顺序号)
     *
     * @return 交货单号
     */
    private String generateDeliveryNumber() {
        long count = deliveryHdrRepository.count() + 1;
        return "8" + String.format("%07d", count);
    }

    /**
     * 转换为DTO
     *
     * @param hdr 交货单头实体
     * @return 交货单DTO
     */
    private DeliveryDTO convertToDTO(SdDeliveryHdr hdr) {
        List<DeliveryItemDTO> itemDTOs = hdr.getItems().stream()
                .map(itm -> DeliveryItemDTO.builder()
                        .id(itm.getId())
                        .itemNumber(itm.getItemNumber())
                        .materialId(itm.getMaterialId())
                        .materialCode(itm.getMaterialCode())
                        .description(itm.getDescription())
                        .deliveryQty(itm.getDeliveryQty())
                        .pickedQty(itm.getPickedQty())
                        .salesUnit(itm.getSalesUnit())
                        .baseUnit(itm.getBaseUnit())
                        .batchNumber(itm.getBatchNumber())
                        .plantId(itm.getPlantId())
                        .slocId(itm.getSlocId())
                        .orderId(itm.getOrderId())
                        .orderItemId(itm.getOrderItemId())
                        .build())
                .collect(Collectors.toList());

        return DeliveryDTO.builder()
                .id(hdr.getId())
                .deliveryNumber(hdr.getDeliveryNumber())
                .deliveryType(hdr.getDeliveryType())
                .salesOrgId(hdr.getSalesOrgId())
                .distributionChannel(hdr.getDistributionChannel())
                .division(hdr.getDivision())
                .soldToParty(hdr.getSoldToParty())
                .shipToParty(hdr.getShipToParty())
                .documentDate(hdr.getDocumentDate())
                .plannedGiDate(hdr.getPlannedGiDate())
                .actualGiDate(hdr.getActualGiDate())
                .shippingPoint(hdr.getShippingPoint())
                .deliveryStatus(hdr.getDeliveryStatus())
                .pickingStatus(hdr.getPickingStatus())
                .giStatus(hdr.getGiStatus())
                .orderId(hdr.getOrderId())
                .remark(hdr.getRemark())
                .items(itemDTOs)
                .build();
    }
}
