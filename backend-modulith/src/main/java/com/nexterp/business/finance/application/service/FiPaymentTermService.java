package com.nexterp.business.finance.application.service;

import com.nexterp.business.finance.domain.model.FiPaymentTerm;
import com.nexterp.business.finance.domain.repository.FiPaymentTermRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 付款条件服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FiPaymentTermService {

    private final FiPaymentTermRepository paymentTermRepository;

    /**
     * 创建付款条件
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPaymentTerm(FiPaymentTerm paymentTerm) {
        // 检查代码是否已存在
        if (paymentTermRepository.existsByPaymentTermCodeAndTenantIdAndIsDeletedFalse(
                paymentTerm.getPaymentTermCode(), paymentTerm.getTenantId())) {
            throw new BusinessException("付款条件代码已存在: " + paymentTerm.getPaymentTermCode());
        }

        // 设置默认值
        if (paymentTerm.getStatus() == null) {
            paymentTerm.setStatus(1);
        }

        // 如果设为默认，取消其他默认
        if (Boolean.TRUE.equals(paymentTerm.getIsDefault())) {
            clearDefaultPaymentTerm(paymentTerm.getTenantId());
        }

        FiPaymentTerm saved = paymentTermRepository.save(paymentTerm);
        log.info("创建付款条件成功: code={}, name={}", saved.getPaymentTermCode(), saved.getPaymentTermName());
        return saved.getId();
    }

    /**
     * 更新付款条件
     */
    @Transactional(rollbackFor = Exception.class)
    public FiPaymentTerm updatePaymentTerm(Long id, FiPaymentTerm paymentTerm) {
        FiPaymentTerm existing = paymentTermRepository.findById(id)
                .orElseThrow(() -> new BusinessException("付款条件不存在"));

        existing.setPaymentTermName(paymentTerm.getPaymentTermName());
        existing.setPaymentTermNameEn(paymentTerm.getPaymentTermNameEn());
        existing.setFixedDays(paymentTerm.getFixedDays());
        existing.setMonthEndDays(paymentTerm.getMonthEndDays());
        existing.setDiscountDays1(paymentTerm.getDiscountDays1());
        existing.setDiscountRate1(paymentTerm.getDiscountRate1());
        existing.setDiscountDays2(paymentTerm.getDiscountDays2());
        existing.setDiscountRate2(paymentTerm.getDiscountRate2());
        existing.setNetPaymentDays(paymentTerm.getNetPaymentDays());
        existing.setRemark(paymentTerm.getRemark());

        // 如果设为默认，取消其他默认
        if (Boolean.TRUE.equals(paymentTerm.getIsDefault()) && !Boolean.TRUE.equals(existing.getIsDefault())) {
            clearDefaultPaymentTerm(existing.getTenantId());
            existing.setIsDefault(true);
        }

        return paymentTermRepository.save(existing);
    }

    /**
     * 删除付款条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePaymentTerm(Long id) {
        FiPaymentTerm paymentTerm = paymentTermRepository.findById(id)
                .orElseThrow(() -> new BusinessException("付款条件不存在"));

        paymentTerm.setIsDeleted(true);
        paymentTermRepository.save(paymentTerm);
        log.info("删除付款条件成功: id={}", id);
    }

    /**
     * 获取付款条件详情
     */
    public FiPaymentTerm getPaymentTermById(Long id) {
        return paymentTermRepository.findById(id)
                .orElseThrow(() -> new BusinessException("付款条件不存在"));
    }

    /**
     * 根据代码获取
     */
    public FiPaymentTerm getPaymentTermByCode(String paymentTermCode, Long tenantId) {
        return paymentTermRepository.findByPaymentTermCodeAndTenantIdAndIsDeletedFalse(paymentTermCode, tenantId)
                .orElseThrow(() -> new BusinessException("付款条件不存在: " + paymentTermCode));
    }

    /**
     * 获取默认付款条件
     */
    public Optional<FiPaymentTerm> getDefaultPaymentTerm(Long tenantId) {
        return paymentTermRepository.findByIsDefaultTrueAndTenantIdAndIsDeletedFalse(tenantId);
    }

    /**
     * 获取所有启用的付款条件
     */
    public List<FiPaymentTerm> listEnabledPaymentTerms(Long tenantId) {
        return paymentTermRepository.findByStatusAndTenantIdAndIsDeletedFalseOrderBySortOrder(1, tenantId);
    }

    /**
     * 根据类型查询
     */
    public List<FiPaymentTerm> listByTermType(String termType, Long tenantId) {
        return paymentTermRepository.findByTermTypeAndTenantIdAndIsDeletedFalseOrderBySortOrder(termType, tenantId);
    }

    /**
     * 根据适用范围查询
     */
    public List<FiPaymentTerm> listByApplyScope(String applyScope, Long tenantId) {
        return paymentTermRepository.findByApplyScopeAndTenantIdAndIsDeletedFalseOrderBySortOrder(applyScope, tenantId);
    }

    /**
     * 计算到期日
     */
    public LocalDate calculateDueDate(String paymentTermCode, Long tenantId, LocalDate baselineDate) {
        FiPaymentTerm paymentTerm = getPaymentTermByCode(paymentTermCode, tenantId);
        return paymentTerm.calculateDueDate(baselineDate);
    }

    /**
     * 分页查询
     */
    public Page<FiPaymentTerm> listPaymentTerms(Long tenantId, Pageable pageable) {
        return paymentTermRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }

    /**
     * 清除默认付款条件
     */
    private void clearDefaultPaymentTerm(Long tenantId) {
        paymentTermRepository.findByIsDefaultTrueAndTenantIdAndIsDeletedFalse(tenantId)
                .ifPresent(pt -> {
                    pt.setIsDefault(false);
                    paymentTermRepository.save(pt);
                });
    }
}
