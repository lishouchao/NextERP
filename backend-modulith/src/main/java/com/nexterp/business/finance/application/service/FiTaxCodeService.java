package com.nexterp.business.finance.application.service;

import com.nexterp.business.finance.domain.model.FiTaxCode;
import com.nexterp.business.finance.domain.repository.FiTaxCodeRepository;
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
 * 税码服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FiTaxCodeService {

    private final FiTaxCodeRepository taxCodeRepository;

    /**
     * 创建税码
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTaxCode(FiTaxCode taxCode) {
        // 检查代码是否已存在
        if (taxCodeRepository.existsByTaxCodeAndTenantIdAndIsDeletedFalse(
                taxCode.getTaxCode(), taxCode.getTenantId())) {
            throw new BusinessException("税码已存在: " + taxCode.getTaxCode());
        }

        // 设置默认值
        if (taxCode.getStatus() == null) {
            taxCode.setStatus(1);
        }
        if (taxCode.getValidFrom() == null) {
            taxCode.setValidFrom(LocalDate.now());
        }
        if (taxCode.getValidTo() == null) {
            taxCode.setValidTo(LocalDate.of(9999, 12, 31));
        }

        FiTaxCode saved = taxCodeRepository.save(taxCode);
        log.info("创建税码成功: taxCode={}, name={}, rate={}%",
                saved.getTaxCode(), saved.getTaxName(), saved.getTaxRate());
        return saved.getId();
    }

    /**
     * 更新税码
     */
    @Transactional(rollbackFor = Exception.class)
    public FiTaxCode updateTaxCode(Long id, FiTaxCode taxCode) {
        FiTaxCode existing = taxCodeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("税码不存在"));

        existing.setTaxName(taxCode.getTaxName());
        existing.setTaxNameEn(taxCode.getTaxNameEn());
        existing.setTaxRate(taxCode.getTaxRate());
        existing.setInputTaxAccountCode(taxCode.getInputTaxAccountCode());
        existing.setOutputTaxAccountCode(taxCode.getOutputTaxAccountCode());
        existing.setRemark(taxCode.getRemark());

        return taxCodeRepository.save(existing);
    }

    /**
     * 删除税码
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaxCode(Long id) {
        FiTaxCode taxCode = taxCodeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("税码不存在"));

        taxCode.setIsDeleted(true);
        taxCodeRepository.save(taxCode);
        log.info("删除税码成功: id={}", id);
    }

    /**
     * 获取税码详情
     */
    public FiTaxCode getTaxCodeById(Long id) {
        return taxCodeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("税码不存在"));
    }

    /**
     * 根据代码获取
     */
    public FiTaxCode getTaxCodeByCode(String taxCode, Long tenantId) {
        return taxCodeRepository.findByTaxCodeAndTenantIdAndIsDeletedFalse(taxCode, tenantId)
                .orElseThrow(() -> new BusinessException("税码不存在: " + taxCode));
    }

    /**
     * 获取当前有效的税码
     */
    public List<FiTaxCode> getValidTaxCodes(Long tenantId) {
        return taxCodeRepository.findValidTaxCodesByTenantIdAndDate(tenantId, LocalDate.now());
    }

    /**
     * 根据税类型查询
     */
    public List<FiTaxCode> listByTaxType(String taxType, Long tenantId) {
        return taxCodeRepository.findByTaxTypeAndTenantIdAndIsDeletedFalseOrderBySortOrder(taxType, tenantId);
    }

    /**
     * 获取所有启用的税码
     */
    public List<FiTaxCode> listEnabledTaxCodes(Long tenantId) {
        return taxCodeRepository.findByStatusAndTenantIdAndIsDeletedFalseOrderBySortOrder(1, tenantId);
    }

    /**
     * 分页查询
     */
    public Page<FiTaxCode> listTaxCodes(Long tenantId, Pageable pageable) {
        return taxCodeRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }
}
