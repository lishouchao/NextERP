package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrPayrollItem;
import com.nexterp.business.hrm.domain.repository.HrPayrollItemRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 薪酬项目明细 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrPayrollItemService {

    private final HrPayrollItemRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrPayrollItem entity) {
        HrPayrollItem saved = repository.save(entity);
        log.info("创建薪酬项: resultId={}, wageType={}", saved.getPayrollResultId(), saved.getWageTypeCode());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchCreate(List<HrPayrollItem> items) {
        repository.saveAll(items);
        log.info("批量创建薪酬项: count={}", items.size());
    }

    @Transactional(rollbackFor = Exception.class)
    public HrPayrollItem update(Long id, HrPayrollItem entity) {
        HrPayrollItem existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("薪酬项不存在"));
        existing.setWageTypeId(entity.getWageTypeId());
        existing.setWageTypeCode(entity.getWageTypeCode());
        existing.setWageTypeName(entity.getWageTypeName());
        existing.setWageCategory(entity.getWageCategory());
        existing.setDcIndicator(entity.getDcIndicator());
        existing.setCalcBase(entity.getCalcBase());
        existing.setCalcRatio(entity.getCalcRatio());
        existing.setAmount(entity.getAmount());
        existing.setCalcFormula(entity.getCalcFormula());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrPayrollItem entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("薪酬项不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByResultId(Long resultId) {
        repository.softDeleteByResultId(resultId);
        log.info("删除薪酬结果的所有明细: resultId={}", resultId);
    }

    public HrPayrollItem getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("薪酬项不存在"));
    }

    public List<HrPayrollItem> getByResultId(Long resultId) {
        return repository.findByPayrollResultIdAndIsDeletedFalseOrderByWageCategoryAscSortOrderAsc(resultId);
    }

    public List<HrPayrollItem> getByEmployeeAndPeriod(Long employeeId, String payrollPeriod) {
        return repository.findByEmployeeIdAndPayrollPeriodAndIsDeletedFalse(employeeId, payrollPeriod);
    }

    public Optional<HrPayrollItem> getByResultAndWageType(Long resultId, Long wageTypeId) {
        return repository.findByPayrollResultIdAndWageTypeIdAndIsDeletedFalse(resultId, wageTypeId);
    }

    public List<HrPayrollItem> getByResultAndCategory(Long resultId, String category) {
        return repository.findByPayrollResultIdAndWageCategoryAndIsDeletedFalse(resultId, category);
    }

    public List<HrPayrollItem> getAdditions(Long resultId) {
        return repository.findAdditions(resultId);
    }

    public List<HrPayrollItem> getDeductions(Long resultId) {
        return repository.findDeductions(resultId);
    }

    public BigDecimal sumAdditions(Long resultId) {
        return repository.sumAdditions(resultId).orElse(BigDecimal.ZERO);
    }

    public BigDecimal sumDeductions(Long resultId) {
        return repository.sumDeductions(resultId).orElse(BigDecimal.ZERO);
    }

    public List<Object[]> sumByWageType(Long tenantId, String payrollPeriod) {
        return repository.sumByWageType(tenantId, payrollPeriod);
    }
}
