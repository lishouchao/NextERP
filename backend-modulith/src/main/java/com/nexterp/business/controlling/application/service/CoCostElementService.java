package com.nexterp.business.controlling.application.service;

import com.nexterp.business.controlling.domain.model.CoCostElement;
import com.nexterp.business.controlling.domain.repository.CoCostElementRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 成本要素服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoCostElementService {

    private final CoCostElementRepository costElementRepository;

    /**
     * 创建成本要素
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCostElement(CoCostElement costElement) {
        // 检查代码是否已存在
        if (costElementRepository.existsByElementCodeAndTenantIdAndIsDeletedFalse(
                costElement.getElementCode(), costElement.getTenantId())) {
            throw new BusinessException("成本要素代码已存在: " + costElement.getElementCode());
        }

        // 设置默认值
        if (costElement.getStatus() == null) {
            costElement.setStatus(1);
        }

        CoCostElement saved = costElementRepository.save(costElement);
        log.info("创建成本要素成功: code={}, name={}", costElement.getElementCode(), costElement.getElementName());
        return saved.getId();
    }

    /**
     * 更新成本要素
     */
    @Transactional(rollbackFor = Exception.class)
    public CoCostElement updateCostElement(Long id, CoCostElement costElement) {
        CoCostElement existing = costElementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成本要素不存在"));

        existing.setElementName(costElement.getElementName());
        existing.setDescription(costElement.getDescription());
        existing.setValidFrom(costElement.getValidFrom());
        existing.setValidTo(costElement.getValidTo());

        return costElementRepository.save(existing);
    }

    /**
     * 删除成本要素
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCostElement(Long id) {
        CoCostElement costElement = costElementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成本要素不存在"));

        costElement.setIsDeleted(true);
        costElementRepository.save(costElement);
        log.info("删除成本要素成功: id={}", id);
    }

    /**
     * 获取成本要素详情
     */
    public CoCostElement getCostElementById(Long id) {
        return costElementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成本要素不存在"));
    }

    /**
     * 根据代码获取成本要素
     */
    public CoCostElement getCostElementByCode(String elementCode, Long tenantId) {
        return costElementRepository.findByElementCodeAndTenantIdAndIsDeletedFalse(elementCode, tenantId)
                .orElseThrow(() -> new BusinessException("成本要素不存在: " + elementCode));
    }

    /**
     * 按类型查询成本要素
     */
    public List<CoCostElement> listByElementType(String elementType, Long tenantId) {
        return costElementRepository.findByElementTypeAndTenantIdAndIsDeletedFalseOrderByElementCodeAsc(elementType, tenantId);
    }

    /**
     * 按类别查询成本要素
     */
    public List<CoCostElement> listByElementCategory(String elementCategory, Long tenantId) {
        return costElementRepository.findByElementCategoryAndTenantIdAndIsDeletedFalseOrderByElementCodeAsc(elementCategory, tenantId);
    }

    /**
     * 查询有效成本要素
     */
    public List<CoCostElement> listValidCostElements(Long tenantId) {
        LocalDate today = LocalDate.now();
        return costElementRepository.findByTenantIdAndIsDeletedFalseAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByElementCodeAsc(
                tenantId, today, today);
    }

    /**
     * 查询启用的成本要素
     */
    public List<CoCostElement> listActiveCostElements(Long tenantId) {
        return costElementRepository.findByTenantIdAndIsDeletedFalseAndStatusOrderByElementCodeAsc(tenantId, 1);
    }

    /**
     * 分页查询
     */
    public Page<CoCostElement> listCostElements(Long tenantId, Pageable pageable) {
        return costElementRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }
}
