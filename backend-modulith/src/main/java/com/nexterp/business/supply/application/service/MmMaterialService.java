package com.nexterp.business.supply.application.service;

import com.nexterp.business.supply.domain.model.*;
import com.nexterp.business.supply.domain.repository.MmMaterialRepository;
import com.nexterp.business.supply.dto.*;
import com.nexterp.business.supply.event.MaterialCreatedEvent;
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
import java.util.stream.Collectors;

/**
 * 物料主数据服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MmMaterialService {

    private final MmMaterialRepository materialRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建物料主数据
     *
     * @param request 创建物料请求
     * @return 物料ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createMaterial(CreateMaterialRequest request) {
        String materialNumber = generateMaterialNumber();

        MmMaterial material = MmMaterial.builder()
                .materialNumber(materialNumber)
                .materialType(request.getMaterialType())
                .industrySector(request.getIndustrySector())
                .materialGroup(request.getMaterialGroup())
                .description(request.getDescription())
                .descriptionEn(request.getDescriptionEn())
                .baseUom(request.getBaseUom())
                .orderUom(request.getOrderUom())
                .grossWeight(request.getGrossWeight())
                .netWeight(request.getNetWeight())
                .weightUnit(request.getWeightUnit())
                .volume(request.getVolume())
                .volumeUnit(request.getVolumeUnit())
                .eanUpc(request.getEanUpc())
                .oldMatNo(request.getOldMatNo())
                .division(request.getDivision())
                .productHierarchy(request.getProductHierarchy())
                .crossPlantStatus("A")
                .validFrom(LocalDate.now())
                .tenantId(request.getTenantId())
                .build();

        // 工厂视图数据
        List<MmMaterialPlant> plantData = new ArrayList<>();
        if (request.getPlantViews() != null) {
            for (CreateMaterialPlantRequest plantReq : request.getPlantViews()) {
                MmMaterialPlant plant = MmMaterialPlant.builder()
                        .materialNumber(materialNumber)
                        .plantId(plantReq.getPlantId())
                        .plantCode(plantReq.getPlantCode())
                        .statusPlant("A")
                        .abcIndicator(plantReq.getAbcIndicator())
                        .mrpType(plantReq.getMrpType())
                        .mrpController(plantReq.getMrpController())
                        .lotSizeProcedure(plantReq.getLotSizeProcedure())
                        .minLotSize(plantReq.getMinLotSize())
                        .maxLotSize(plantReq.getMaxLotSize())
                        .safetyStock(plantReq.getSafetyStock())
                        .reorderPoint(plantReq.getReorderPoint())
                        .plannedDelivTime(plantReq.getPlannedDelivTime())
                        .procurementType(plantReq.getProcurementType())
                        .storageLocation(plantReq.getStorageLocation())
                        .availabilityCheck(plantReq.getAvailabilityCheck())
                        .batchManagement(plantReq.getBatchManagement() != null ? plantReq.getBatchManagement() : "0")
                        .profitCenter(plantReq.getProfitCenter())
                        .tenantId(request.getTenantId())
                        .material(material)
                        .build();
                plantData.add(plant);
            }
        }
        material.setPlantData(plantData);

        // 销售视图数据
        List<MmMaterialSales> salesData = new ArrayList<>();
        if (request.getSalesViews() != null) {
            for (CreateMaterialSalesRequest salesReq : request.getSalesViews()) {
                MmMaterialSales sales = MmMaterialSales.builder()
                        .materialNumber(materialNumber)
                        .salesOrgId(salesReq.getSalesOrgId())
                        .salesOrgCode(salesReq.getSalesOrgCode())
                        .distrChannel(salesReq.getDistrChannel())
                        .statusSales("A")
                        .deliveringPlant(salesReq.getDeliveringPlant())
                        .salesUnit(salesReq.getSalesUnit())
                        .minOrderQty(salesReq.getMinOrderQty())
                        .minDelivQty(salesReq.getMinDelivQty())
                        .pricingGroup(salesReq.getPricingGroup())
                        .itemCategoryGroup(salesReq.getItemCategoryGroup())
                        .accountAssignmentGroup(salesReq.getAccountAssignmentGroup())
                        .productHierarchy(salesReq.getProductHierarchy())
                        .materialPricingGroup(salesReq.getMaterialPricingGroup())
                        .tenantId(request.getTenantId())
                        .material(material)
                        .build();
                salesData.add(sales);
            }
        }
        material.setSalesData(salesData);

        // 评估数据
        List<MmMaterialValuation> valuationData = new ArrayList<>();
        if (request.getValuationViews() != null) {
            for (CreateMaterialValuationRequest valReq : request.getValuationViews()) {
                MmMaterialValuation valuation = MmMaterialValuation.builder()
                        .materialNumber(materialNumber)
                        .valuationArea(valReq.getValuationArea())
                        .valuationType(valReq.getValuationType())
                        .priceControl(valReq.getPriceControl())
                        .movingPrice(valReq.getMovingPrice())
                        .standardPrice(valReq.getStandardPrice())
                        .valuationClass(valReq.getValuationClass())
                        .priceUnit(valReq.getPriceUnit() != null ? valReq.getPriceUnit() : 1)
                        .futurePrice(valReq.getFuturePrice())
                        .futurePriceValidFrom(valReq.getFuturePriceValidFrom())
                        .validFrom(LocalDate.now())
                        .currency(valReq.getCurrency())
                        .tenantId(request.getTenantId())
                        .material(material)
                        .build();
                valuationData.add(valuation);
            }
        }
        material.setValuationData(valuationData);

        MmMaterial saved = materialRepository.save(material);
        log.info("创建物料成功: materialNumber={}, materialType={}", materialNumber, request.getMaterialType());

        // 发布物料创建事件
        eventPublisher.publishEvent(new MaterialCreatedEvent(
                saved.getId(),
                saved.getMaterialNumber(),
                saved.getMaterialType(),
                saved.getTenantId(),
                saved.getDescription()
        ));

        return saved.getId();
    }

    /**
     * 更新物料主数据
     *
     * @param id      物料ID
     * @param request 创建物料请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMaterial(Long id, CreateMaterialRequest request) {
        MmMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new BusinessException("物料不存在"));

        if (!"A".equals(material.getCrossPlantStatus())) {
            throw new BusinessException("仅活跃状态的物料允许修改");
        }

        material.setMaterialType(request.getMaterialType());
        material.setIndustrySector(request.getIndustrySector());
        material.setMaterialGroup(request.getMaterialGroup());
        material.setDescription(request.getDescription());
        material.setDescriptionEn(request.getDescriptionEn());
        material.setBaseUom(request.getBaseUom());
        material.setOrderUom(request.getOrderUom());
        material.setGrossWeight(request.getGrossWeight());
        material.setNetWeight(request.getNetWeight());
        material.setWeightUnit(request.getWeightUnit());
        material.setVolume(request.getVolume());
        material.setVolumeUnit(request.getVolumeUnit());
        material.setEanUpc(request.getEanUpc());
        material.setOldMatNo(request.getOldMatNo());
        material.setDivision(request.getDivision());
        material.setProductHierarchy(request.getProductHierarchy());

        materialRepository.save(material);
        log.info("更新物料成功: id={}, materialNumber={}", id, material.getMaterialNumber());
    }

    /**
     * 删除物料 (软删除)
     *
     * @param id 物料ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaterial(Long id) {
        MmMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new BusinessException("物料不存在"));

        material.setIsDeleted(true);
        materialRepository.save(material);
        log.info("删除物料成功: id={}, materialNumber={}", id, material.getMaterialNumber());
    }

    /**
     * 获取物料详情 (含工厂/销售/评估视图)
     *
     * @param id 物料ID
     * @return 物料DTO
     */
    public MaterialDTO getMaterialById(Long id) {
        MmMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new BusinessException("物料不存在"));
        return convertToDTO(material);
    }

    /**
     * 根据物料编码查询
     *
     * @param number   物料编码
     * @param tenantId 租户ID
     * @return 物料DTO
     */
    public MaterialDTO getMaterialByNumber(String number, Long tenantId) {
        MmMaterial material = materialRepository.findByMaterialNumberAndTenantIdAndIsDeletedFalse(number, tenantId)
                .orElseThrow(() -> new BusinessException("物料不存在: " + number));
        return convertToDTO(material);
    }

    /**
     * 分页查询物料
     *
     * @param tenantId     租户ID
     * @param materialType 物料类型 (可选)
     * @param current      当前页
     * @param size         每页大小
     * @return 分页结果
     */
    public PageResult<MaterialDTO> listMaterials(Long tenantId, String materialType, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<MmMaterial> page;

        if (materialType != null && !materialType.isEmpty()) {
            page = materialRepository.findByTenantIdAndMaterialTypeAndIsDeletedFalse(tenantId, materialType, pageRequest);
        } else {
            page = materialRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageRequest);
        }

        List<MaterialDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<MaterialDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 搜索物料
     *
     * @param tenantId 租户ID
     * @param keyword  关键词
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    public PageResult<MaterialDTO> searchMaterials(Long tenantId, String keyword, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<MmMaterial> page = materialRepository.searchMaterials(tenantId, keyword, pageRequest);

        List<MaterialDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<MaterialDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 扩展物料到工厂视图
     *
     * @param materialId 物料ID
     * @param request    工厂视图请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void extendToPlant(Long materialId, CreateMaterialPlantRequest request) {
        MmMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BusinessException("物料不存在"));

        MmMaterialPlant plant = MmMaterialPlant.builder()
                .materialNumber(material.getMaterialNumber())
                .plantId(request.getPlantId())
                .plantCode(request.getPlantCode())
                .statusPlant("A")
                .abcIndicator(request.getAbcIndicator())
                .mrpType(request.getMrpType())
                .mrpController(request.getMrpController())
                .lotSizeProcedure(request.getLotSizeProcedure())
                .minLotSize(request.getMinLotSize())
                .maxLotSize(request.getMaxLotSize())
                .safetyStock(request.getSafetyStock())
                .reorderPoint(request.getReorderPoint())
                .plannedDelivTime(request.getPlannedDelivTime())
                .procurementType(request.getProcurementType())
                .storageLocation(request.getStorageLocation())
                .availabilityCheck(request.getAvailabilityCheck())
                .batchManagement(request.getBatchManagement() != null ? request.getBatchManagement() : "0")
                .profitCenter(request.getProfitCenter())
                .tenantId(material.getTenantId())
                .material(material)
                .build();

        material.getPlantData().add(plant);
        materialRepository.save(material);
        log.info("扩展物料到工厂: materialId={}, plantCode={}", materialId, request.getPlantCode());
    }

    /**
     * 扩展物料到销售视图
     *
     * @param materialId 物料ID
     * @param request    销售视图请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void extendToSales(Long materialId, CreateMaterialSalesRequest request) {
        MmMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BusinessException("物料不存在"));

        MmMaterialSales sales = MmMaterialSales.builder()
                .materialNumber(material.getMaterialNumber())
                .salesOrgId(request.getSalesOrgId())
                .salesOrgCode(request.getSalesOrgCode())
                .distrChannel(request.getDistrChannel())
                .statusSales("A")
                .deliveringPlant(request.getDeliveringPlant())
                .salesUnit(request.getSalesUnit())
                .minOrderQty(request.getMinOrderQty())
                .minDelivQty(request.getMinDelivQty())
                .pricingGroup(request.getPricingGroup())
                .itemCategoryGroup(request.getItemCategoryGroup())
                .accountAssignmentGroup(request.getAccountAssignmentGroup())
                .productHierarchy(request.getProductHierarchy())
                .materialPricingGroup(request.getMaterialPricingGroup())
                .tenantId(material.getTenantId())
                .material(material)
                .build();

        material.getSalesData().add(sales);
        materialRepository.save(material);
        log.info("扩展物料到销售视图: materialId={}, salesOrgCode={}", materialId, request.getSalesOrgCode());
    }

    /**
     * 生成物料编码 (格式: 18位内部编码)
     *
     * @return 物料编码
     */
    private String generateMaterialNumber() {
        long count = materialRepository.count() + 1;
        return String.format("%018d", count);
    }

    /**
     * 转换为DTO
     *
     * @param entity 物料实体
     * @return 物料DTO
     */
    private MaterialDTO convertToDTO(MmMaterial entity) {
        List<MaterialPlantDTO> plantDTOs = entity.getPlantData().stream()
                .map(p -> MaterialPlantDTO.builder()
                        .id(p.getId())
                        .plantId(p.getPlantId())
                        .plantCode(p.getPlantCode())
                        .statusPlant(p.getStatusPlant())
                        .abcIndicator(p.getAbcIndicator())
                        .mrpType(p.getMrpType())
                        .mrpController(p.getMrpController())
                        .procurementType(p.getProcurementType())
                        .safetyStock(p.getSafetyStock())
                        .reorderPoint(p.getReorderPoint())
                        .batchManagement(p.getBatchManagement())
                        .build())
                .collect(Collectors.toList());

        List<MaterialSalesDTO> salesDTOs = entity.getSalesData().stream()
                .map(s -> MaterialSalesDTO.builder()
                        .id(s.getId())
                        .salesOrgId(s.getSalesOrgId())
                        .salesOrgCode(s.getSalesOrgCode())
                        .distrChannel(s.getDistrChannel())
                        .statusSales(s.getStatusSales())
                        .deliveringPlant(s.getDeliveringPlant())
                        .salesUnit(s.getSalesUnit())
                        .pricingGroup(s.getPricingGroup())
                        .itemCategoryGroup(s.getItemCategoryGroup())
                        .build())
                .collect(Collectors.toList());

        List<MaterialValuationDTO> valuationDTOs = entity.getValuationData().stream()
                .map(v -> MaterialValuationDTO.builder()
                        .id(v.getId())
                        .valuationArea(v.getValuationArea())
                        .valuationType(v.getValuationType())
                        .priceControl(v.getPriceControl())
                        .movingPrice(v.getMovingPrice())
                        .standardPrice(v.getStandardPrice())
                        .valuationClass(v.getValuationClass())
                        .currency(v.getCurrency())
                        .build())
                .collect(Collectors.toList());

        return MaterialDTO.builder()
                .id(entity.getId())
                .materialNumber(entity.getMaterialNumber())
                .materialType(entity.getMaterialType())
                .industrySector(entity.getIndustrySector())
                .materialGroup(entity.getMaterialGroup())
                .description(entity.getDescription())
                .descriptionEn(entity.getDescriptionEn())
                .baseUom(entity.getBaseUom())
                .orderUom(entity.getOrderUom())
                .grossWeight(entity.getGrossWeight())
                .netWeight(entity.getNetWeight())
                .weightUnit(entity.getWeightUnit())
                .volume(entity.getVolume())
                .volumeUnit(entity.getVolumeUnit())
                .eanUpc(entity.getEanUpc())
                .oldMatNo(entity.getOldMatNo())
                .division(entity.getDivision())
                .productHierarchy(entity.getProductHierarchy())
                .crossPlantStatus(entity.getCrossPlantStatus())
                .validFrom(entity.getValidFrom())
                .validTo(entity.getValidTo())
                .plantViews(plantDTOs)
                .salesViews(salesDTOs)
                .valuationViews(valuationDTOs)
                .build();
    }
}
