package com.nexterp.business.controlling.application.service;

import com.nexterp.business.controlling.domain.model.CoCostComponent;
import com.nexterp.business.controlling.domain.model.CoCostEstimate;
import com.nexterp.business.controlling.domain.repository.CoCostComponentRepository;
import com.nexterp.business.controlling.domain.repository.CoCostEstimateRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成本估算服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoCostEstimateService {

    private final CoCostEstimateRepository costEstimateRepository;
    private final CoCostComponentRepository costComponentRepository;

    /**
     * 创建成本估算
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCostEstimate(CoCostEstimate estimate) {
        // 生成估算号
        if (estimate.getEstimateNumber() == null || estimate.getEstimateNumber().isEmpty()) {
            estimate.setEstimateNumber(generateEstimateNumber());
        }

        // 设置默认值
        if (estimate.getEstimateStatus() == null) {
            estimate.setEstimateStatus("01");
        }
        if (estimate.getMaterialCost() == null) {
            estimate.setMaterialCost(BigDecimal.ZERO);
        }
        if (estimate.getLaborCost() == null) {
            estimate.setLaborCost(BigDecimal.ZERO);
        }
        if (estimate.getMachineCost() == null) {
            estimate.setMachineCost(BigDecimal.ZERO);
        }
        if (estimate.getOverheadCost() == null) {
            estimate.setOverheadCost(BigDecimal.ZERO);
        }
        if (estimate.getSubcontractingCost() == null) {
            estimate.setSubcontractingCost(BigDecimal.ZERO);
        }
        if (estimate.getTotalCost() == null) {
            estimate.setTotalCost(BigDecimal.ZERO);
        }

        estimate.setCreatedAt(LocalDateTime.now());
        estimate.setEstimateDate(LocalDate.now());

        CoCostEstimate saved = costEstimateRepository.save(estimate);
        log.info("创建成本估算成功: estimateNumber={}, material={}", estimate.getEstimateNumber(), estimate.getMaterialCode());
        return saved.getId();
    }

    /**
     * 生成估算号
     */
    private String generateEstimateNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        return "CE" + timestamp;
    }

    /**
     * 发布成本估算
     */
    @Transactional(rollbackFor = Exception.class)
    public CoCostEstimate releaseCostEstimate(Long id) {
        CoCostEstimate estimate = costEstimateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成本估算不存在"));

        if (!"01".equals(estimate.getEstimateStatus()) && !"02".equals(estimate.getEstimateStatus())) {
            throw new BusinessException("估算状态不允许发布");
        }

        // 计算总成本
        estimate.calculateTotalCost();

        estimate.setEstimateStatus("04");
        estimate.setReleasedAt(LocalDateTime.now());
        return costEstimateRepository.save(estimate);
    }

    /**
     * 标记成本估算
     */
    @Transactional(rollbackFor = Exception.class)
    public CoCostEstimate markCostEstimate(Long id) {
        CoCostEstimate estimate = costEstimateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成本估算不存在"));

        if (!"02".equals(estimate.getEstimateStatus())) {
            throw new BusinessException("只有已下达的估算可以标记");
        }

        estimate.setEstimateStatus("03");
        return costEstimateRepository.save(estimate);
    }

    /**
     * 更新成本估算
     */
    @Transactional(rollbackFor = Exception.class)
    public CoCostEstimate updateCostEstimate(Long id, CoCostEstimate estimate) {
        CoCostEstimate existing = costEstimateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成本估算不存在"));

        if (!"01".equals(existing.getEstimateStatus())) {
            throw new BusinessException("只有草稿状态的估算可以修改");
        }

        existing.setMaterialCost(estimate.getMaterialCost());
        existing.setLaborCost(estimate.getLaborCost());
        existing.setMachineCost(estimate.getMachineCost());
        existing.setOverheadCost(estimate.getOverheadCost());
        existing.setSubcontractingCost(estimate.getSubcontractingCost());
        existing.setValidFrom(estimate.getValidFrom());
        existing.setValidTo(estimate.getValidTo());

        existing.calculateTotalCost();

        return costEstimateRepository.save(existing);
    }

    /**
     * 删除成本估算
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCostEstimate(Long id) {
        CoCostEstimate estimate = costEstimateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成本估算不存在"));

        if (!"01".equals(estimate.getEstimateStatus())) {
            throw new BusinessException("只有草稿状态的估算可以删除");
        }

        estimate.setIsDeleted(true);
        costEstimateRepository.save(estimate);
        log.info("删除成本估算成功: id={}", id);
    }

    /**
     * 获取估算详情
     */
    public CoCostEstimate getCostEstimateById(Long id) {
        return costEstimateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成本估算不存在"));
    }

    /**
     * 根据估算号获取
     */
    public CoCostEstimate getCostEstimateByNumber(String estimateNumber, Long tenantId) {
        return costEstimateRepository.findByEstimateNumberAndTenantIdAndIsDeletedFalse(estimateNumber, tenantId)
                .orElseThrow(() -> new BusinessException("成本估算不存在: " + estimateNumber));
    }

    /**
     * 获取物料的有效成本估算
     */
    public CoCostEstimate getValidCostEstimate(Long materialId, String estimateType, Long tenantId) {
        LocalDate today = LocalDate.now();
        return costEstimateRepository.findByMaterialIdAndEstimateTypeAndEstimateStatusAndTenantIdAndIsDeletedFalseAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                materialId, estimateType, "04", tenantId, today, today)
                .orElse(null);
    }

    /**
     * 按物料查询
     */
    public List<CoCostEstimate> listByMaterial(Long materialId, Long tenantId) {
        return costEstimateRepository.findByMaterialIdAndTenantIdAndIsDeletedFalseOrderByEstimateDateDesc(materialId, tenantId);
    }

    /**
     * 分页查询
     */
    public Page<CoCostEstimate> listCostEstimates(Long tenantId, Pageable pageable) {
        return costEstimateRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }

    /**
     * 添加成本构成
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addCostComponent(Long estimateId, CoCostComponent component) {
        CoCostEstimate estimate = costEstimateRepository.findById(estimateId)
                .orElseThrow(() -> new BusinessException("成本估算不存在"));

        component.setCostEstimateId(estimateId);
        component.setTenantId(estimate.getTenantId());

        // 获取下一个行号
        List<CoCostComponent> existingComponents = costComponentRepository
                .findByCostEstimateIdAndTenantIdAndIsDeletedFalseOrderByLineNoAsc(estimateId, estimate.getTenantId());
        int nextLineNo = existingComponents.isEmpty() ? 1 : existingComponents.get(existingComponents.size() - 1).getLineNo() + 10;
        component.setLineNo(nextLineNo);

        component.calculateTotalCost();

        CoCostComponent saved = costComponentRepository.save(component);
        log.info("添加成本构成成功: estimateId={}, lineNo={}", estimateId, nextLineNo);
        return saved.getId();
    }

    /**
     * 获取成本构成明细
     */
    public List<CoCostComponent> getCostComponents(Long estimateId, Long tenantId) {
        return costComponentRepository.findByCostEstimateIdAndTenantIdAndIsDeletedFalseOrderByLineNoAsc(estimateId, tenantId);
    }
}
