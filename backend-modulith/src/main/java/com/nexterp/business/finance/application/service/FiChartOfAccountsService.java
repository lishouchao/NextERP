package com.nexterp.business.finance.application.service;

import com.nexterp.business.finance.domain.model.FiChartOfAccounts;
import com.nexterp.business.finance.domain.repository.FiChartOfAccountsRepository;
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
 * 科目表服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FiChartOfAccountsService {

    private final FiChartOfAccountsRepository coaRepository;

    /**
     * 创建科目表
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCoa(FiChartOfAccounts coa) {
        // 检查代码是否已存在
        if (coaRepository.existsByCoaCodeAndTenantIdAndIsDeletedFalse(coa.getCoaCode(), coa.getTenantId())) {
            throw new BusinessException("科目表代码已存在: " + coa.getCoaCode());
        }

        // 设置默认值
        if (coa.getStatus() == null) {
            coa.setStatus(1);
        }
        if (coa.getValidFrom() == null) {
            coa.setValidFrom(LocalDate.now());
        }
        if (coa.getValidTo() == null) {
            coa.setValidTo(LocalDate.of(9999, 12, 31));
        }

        FiChartOfAccounts saved = coaRepository.save(coa);
        log.info("创建科目表成功: coaCode={}, name={}", saved.getCoaCode(), saved.getCoaName());
        return saved.getId();
    }

    /**
     * 更新科目表
     */
    @Transactional(rollbackFor = Exception.class)
    public FiChartOfAccounts updateCoa(Long id, FiChartOfAccounts coa) {
        FiChartOfAccounts existing = coaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目表不存在"));

        existing.setCoaName(coa.getCoaName());
        existing.setCoaNameEn(coa.getCoaNameEn());
        existing.setDescription(coa.getDescription());
        existing.setRemark(coa.getRemark());

        return coaRepository.save(existing);
    }

    /**
     * 删除科目表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCoa(Long id) {
        FiChartOfAccounts coa = coaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目表不存在"));

        coa.setIsDeleted(true);
        coaRepository.save(coa);
        log.info("删除科目表成功: id={}", id);
    }

    /**
     * 获取科目表详情
     */
    public FiChartOfAccounts getCoaById(Long id) {
        return coaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目表不存在"));
    }

    /**
     * 根据代码获取
     */
    public FiChartOfAccounts getCoaByCode(String coaCode, Long tenantId) {
        return coaRepository.findByCoaCodeAndTenantIdAndIsDeletedFalse(coaCode, tenantId)
                .orElseThrow(() -> new BusinessException("科目表不存在: " + coaCode));
    }

    /**
     * 获取当前有效的科目表列表
     */
    public List<FiChartOfAccounts> getValidCoaList(Long tenantId) {
        return coaRepository.findValidCoaByTenantId(tenantId);
    }

    /**
     * 分页查询
     */
    public Page<FiChartOfAccounts> listCoa(Long tenantId, Pageable pageable) {
        return coaRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }
}
