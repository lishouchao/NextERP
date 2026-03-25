package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrWageType;
import com.nexterp.business.hrm.domain.repository.HrWageTypeRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 工资类型 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrWageTypeService {

    private final HrWageTypeRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrWageType entity) {
        // 检查编码唯一性
        if (repository.findByWageTypeCodeAndTenantIdAndIsDeletedFalse(
                entity.getWageTypeCode(), entity.getTenantId()).isPresent()) {
            throw new BusinessException("工资类型编码已存在");
        }
        HrWageType saved = repository.save(entity);
        log.info("创建工资类型: code={}, name={}", saved.getWageTypeCode(), saved.getWageTypeName());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrWageType update(Long id, HrWageType entity) {
        HrWageType existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("工资类型不存在"));
        existing.setWageTypeName(entity.getWageTypeName());
        existing.setWageCategory(entity.getWageCategory());
        existing.setCalcType(entity.getCalcType());
        existing.setFixedAmount(entity.getFixedAmount());
        existing.setRatio(entity.getRatio());
        existing.setCalcFormula(entity.getCalcFormula());
        existing.setBaseMax(entity.getBaseMax());
        existing.setBaseMin(entity.getBaseMin());
        existing.setIsTaxable(entity.getIsTaxable());
        existing.setIsSocialBase(entity.getIsSocialBase());
        existing.setIsFundBase(entity.getIsFundBase());
        existing.setDcIndicator(entity.getDcIndicator());
        existing.setSortOrder(entity.getSortOrder());
        existing.setStatus(entity.getStatus());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrWageType entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("工资类型不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
        log.info("删除工资类型: id={}", id);
    }

    public HrWageType getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("工资类型不存在"));
    }

    public Optional<HrWageType> getByCode(String code, Long tenantId) {
        return repository.findByWageTypeCodeAndTenantIdAndIsDeletedFalse(code, tenantId);
    }

    public List<HrWageType> getByTenantId(Long tenantId) {
        return repository.findByTenantIdAndIsDeletedFalseOrderBySortOrderAsc(tenantId);
    }

    public List<HrWageType> getEnabledByTenantId(Long tenantId) {
        return repository.findByTenantIdAndStatusAndIsDeletedFalseOrderBySortOrderAsc(tenantId, 1);
    }

    public List<HrWageType> getByCategory(Long tenantId, String category) {
        return repository.findByWageCategoryAndTenantIdAndIsDeletedFalse(category, tenantId);
    }

    public List<HrWageType> getAdditions(Long tenantId) {
        return repository.findByDcIndicatorAndTenantIdAndIsDeletedFalse("D", tenantId);
    }

    public List<HrWageType> getDeductions(Long tenantId) {
        return repository.findByDcIndicatorAndTenantIdAndIsDeletedFalse("C", tenantId);
    }

    public Page<HrWageType> search(Long tenantId, String keyword, Pageable pageable) {
        return repository.findByTenantIdAndKeyword(tenantId, keyword, pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        HrWageType entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("工资类型不存在"));
        entity.setStatus(status);
        repository.save(entity);
    }
}
