package com.nexterp.business.sales.application.service;

import com.nexterp.business.sales.domain.model.SdCondition;
import com.nexterp.business.sales.domain.repository.SdConditionRepository;
import com.nexterp.business.sales.dto.ConditionDTO;
import com.nexterp.business.sales.dto.CreateConditionRequest;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定价条件服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdConditionService {

    private final SdConditionRepository conditionRepository;

    /**
     * 创建定价条件记录
     *
     * @param request 创建条件请求
     * @return 条件记录ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCondition(CreateConditionRequest request) {
        // 生成条件记录号
        String conditionRecord = generateConditionRecord();

        SdCondition entity = SdCondition.builder()
                .conditionType(request.getConditionType())
                .conditionRecord(conditionRecord)
                .conditionItem(1)
                .amount(request.getAmount())
                .rate(request.getRate())
                .priceUnit(request.getPriceUnit() != null ? request.getPriceUnit() : 1)
                .calculationType(request.getCalculationType())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .salesOrgId(request.getSalesOrgId())
                .distributionChannel(request.getDistributionChannel())
                .customerId(request.getCustomerId())
                .materialId(request.getMaterialId())
                .tenantId(request.getTenantId())
                .build();

        SdCondition saved = conditionRepository.save(entity);
        log.info("创建定价条件成功: conditionRecord={}, type={}", conditionRecord, request.getConditionType());
        return saved.getId();
    }

    /**
     * 更新定价条件记录
     *
     * @param id      条件记录ID
     * @param request 创建条件请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCondition(Long id, CreateConditionRequest request) {
        SdCondition entity = conditionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("定价条件记录不存在"));

        entity.setConditionType(request.getConditionType());
        entity.setAmount(request.getAmount());
        entity.setRate(request.getRate());
        entity.setPriceUnit(request.getPriceUnit());
        entity.setCalculationType(request.getCalculationType());
        entity.setValidFrom(request.getValidFrom());
        entity.setValidTo(request.getValidTo());
        entity.setSalesOrgId(request.getSalesOrgId());
        entity.setDistributionChannel(request.getDistributionChannel());
        entity.setCustomerId(request.getCustomerId());
        entity.setMaterialId(request.getMaterialId());

        conditionRepository.save(entity);
        log.info("更新定价条件成功: id={}", id);
    }

    /**
     * 删除定价条件记录 (软删除)
     *
     * @param id 条件记录ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCondition(Long id) {
        SdCondition entity = conditionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("定价条件记录不存在"));

        entity.setIsDeleted(true);
        conditionRepository.save(entity);
        log.info("删除定价条件成功: id={}", id);
    }

    /**
     * 获取定价条件详情
     *
     * @param id 条件记录ID
     * @return 条件DTO
     */
    public ConditionDTO getConditionById(Long id) {
        SdCondition entity = conditionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("定价条件记录不存在"));
        return convertToDTO(entity);
    }

    /**
     * 分页查询定价条件
     *
     * @param tenantId      租户ID
     * @param conditionType 条件类型 (可选)
     * @param current       当前页
     * @param size          每页大小
     * @return 分页结果
     */
    public PageResult<ConditionDTO> listConditions(Long tenantId, String conditionType, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<SdCondition> page;

        if (conditionType != null && !conditionType.isEmpty()) {
            page = conditionRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("tenantId"), tenantId),
                            cb.equal(root.get("conditionType"), conditionType),
                            cb.equal(root.get("isDeleted"), false)
                    ),
                    pageRequest);
        } else {
            page = conditionRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("tenantId"), tenantId),
                            cb.equal(root.get("isDeleted"), false)
                    ),
                    pageRequest);
        }

        List<ConditionDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<ConditionDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 查找有效价格
     * 根据条件类型、客户、物料和有效日期查找适用的条件记录
     *
     * @param tenantId      租户ID
     * @param conditionType 条件类型
     * @param customerId    客户ID
     * @param materialId    物料ID
     * @param validDate     有效日期
     * @return 价格金额
     */
    public BigDecimal findPrice(Long tenantId, String conditionType, Long customerId,
                                Long materialId, LocalDate validDate) {
        List<SdCondition> conditions = conditionRepository.findValidConditions(
                conditionType, customerId, materialId,
                validDate != null ? validDate : LocalDate.now(),
                tenantId);

        if (conditions.isEmpty()) {
            return null;
        }

        // 取最精确匹配的第一条记录
        SdCondition bestMatch = conditions.get(0);

        // 根据计算类型返回价格
        switch (bestMatch.getCalculationType()) {
            case "B": // 固定金额
                return bestMatch.getAmount();
            case "A": // 百分比
                // 百分比类型需要基准价格, 此处直接返回比率
                return bestMatch.getRate();
            case "C": // 数量
                return bestMatch.getAmount();
            default:
                return bestMatch.getAmount();
        }
    }

    /**
     * 预拟定价 (模拟定价过程)
     *
     * @param tenantId     租户ID
     * @param customerId   客户ID
     * @param materialId   物料ID
     * @param qty          数量
     * @param pricingDate  定价日期
     * @return 定价模拟结果
     */
    public Map<String, Object> previewPricing(Long tenantId, Long customerId, Long materialId,
                                              BigDecimal qty, LocalDate pricingDate) {
        LocalDate pricingDateUsed = pricingDate != null ? pricingDate : LocalDate.now();
        Map<String, Object> result = new HashMap<>();
        result.put("tenantId", tenantId);
        result.put("customerId", customerId);
        result.put("materialId", materialId);
        result.put("qty", qty);
        result.put("pricingDate", pricingDateUsed);

        // 查找基础价格 PR00
        BigDecimal basePrice = findPrice(tenantId, "PR00", customerId, materialId, pricingDateUsed);
        if (basePrice == null) {
            basePrice = BigDecimal.ZERO;
        }
        result.put("basePrice", basePrice);

        // 计算净值
        BigDecimal netValue = basePrice.multiply(qty != null ? qty : BigDecimal.ONE);
        result.put("netValue", netValue);

        // 查找折扣 K004 (客户折扣)
        BigDecimal discountRate = findPrice(tenantId, "K004", customerId, materialId, pricingDateUsed);
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discountRate != null) {
            discountAmount = netValue.multiply(discountRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        result.put("discountRate", discountRate);
        result.put("discountAmount", discountAmount);

        // 查找运费 KF00
        BigDecimal freightRate = findPrice(tenantId, "KF00", customerId, materialId, pricingDateUsed);
        BigDecimal freightAmount = BigDecimal.ZERO;
        if (freightRate != null) {
            freightAmount = freightRate.multiply(qty != null ? qty : BigDecimal.ONE);
        }
        result.put("freightAmount", freightAmount);

        // 计算税额 (13%)
        BigDecimal taxableAmount = netValue.subtract(discountAmount).add(freightAmount);
        BigDecimal taxAmount = taxableAmount.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
        result.put("taxAmount", taxAmount);

        // 计算含税金额
        BigDecimal grossValue = taxableAmount.add(taxAmount);
        result.put("grossValue", grossValue);

        return result;
    }

    /**
     * 生成条件记录号 (格式: "C" + 9位顺序号)
     *
     * @return 条件记录号
     */
    private String generateConditionRecord() {
        long count = conditionRepository.count() + 1;
        return "C" + String.format("%09d", count);
    }

    /**
     * 转换为DTO
     *
     * @param entity 定价条件实体
     * @return 条件DTO
     */
    private ConditionDTO convertToDTO(SdCondition entity) {
        return ConditionDTO.builder()
                .id(entity.getId())
                .conditionType(entity.getConditionType())
                .conditionRecord(entity.getConditionRecord())
                .conditionItem(entity.getConditionItem())
                .amount(entity.getAmount())
                .rate(entity.getRate())
                .priceUnit(entity.getPriceUnit())
                .calculationType(entity.getCalculationType())
                .validFrom(entity.getValidFrom())
                .validTo(entity.getValidTo())
                .salesOrgId(entity.getSalesOrgId())
                .distributionChannel(entity.getDistributionChannel())
                .customerId(entity.getCustomerId())
                .materialId(entity.getMaterialId())
                .build();
    }
}
