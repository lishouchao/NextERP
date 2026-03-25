package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrPayrollResult;
import com.nexterp.business.hrm.domain.repository.HrPayrollResultRepository;
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
import java.util.Optional;

/**
 * 薪酬结果 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrPayrollResultService {

    private final HrPayrollResultRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrPayrollResult entity) {
        // 检查是否已存在
        if (repository.existsByEmployeeIdAndPayrollPeriodAndIsDeletedFalse(
                entity.getEmployeeId(), entity.getPayrollPeriod())) {
            throw new BusinessException("该员工在此薪酬期间已有记录");
        }
        // 计算实发
        entity.setNetPay(entity.calculateNetPay());
        entity.setCalculatedAt(LocalDateTime.now());
        HrPayrollResult saved = repository.save(entity);
        log.info("创建薪酬结果: employeeId={}, period={}", saved.getEmployeeId(), saved.getPayrollPeriod());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrPayrollResult update(Long id, HrPayrollResult entity) {
        HrPayrollResult existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("薪酬结果不存在"));
        if (!existing.isModifiable()) {
            throw new BusinessException("当前状态不允许修改");
        }
        existing.setGrossPay(entity.getGrossPay());
        existing.setTotalDeduction(entity.getTotalDeduction());
        existing.setSocialPersonal(entity.getSocialPersonal());
        existing.setFundPersonal(entity.getFundPersonal());
        existing.setIncomeTax(entity.getIncomeTax());
        existing.setNetPay(existing.calculateNetPay());
        existing.setPaymentMethod(entity.getPaymentMethod());
        existing.setBankAccount(entity.getBankAccount());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrPayrollResult entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("薪酬结果不存在"));
        if (!entity.isModifiable()) {
            throw new BusinessException("当前状态不允许删除");
        }
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrPayrollResult getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("薪酬结果不存在"));
    }

    public Optional<HrPayrollResult> getByEmployeeAndPeriod(Long employeeId, String payrollPeriod) {
        return repository.findByEmployeeIdAndPayrollPeriodAndIsDeletedFalse(employeeId, payrollPeriod);
    }

    public List<HrPayrollResult> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalseOrderByPayrollPeriodDesc(employeeId);
    }

    public List<HrPayrollResult> getByPeriod(String payrollPeriod, Long tenantId) {
        return repository.findByPayrollPeriodAndTenantIdAndIsDeletedFalse(payrollPeriod, tenantId);
    }

    public List<HrPayrollResult> getByBatchNo(String batchNo, Long tenantId) {
        return repository.findByBatchNoAndTenantIdAndIsDeletedFalse(batchNo, tenantId);
    }

    public List<HrPayrollResult> getByYear(Integer year, Long tenantId) {
        return repository.findByPayrollYearAndTenantIdAndIsDeletedFalse(year, tenantId);
    }

    public List<HrPayrollResult> getByOrgUnit(Long orgUnitId, String payrollPeriod) {
        return repository.findByOrgUnitIdAndPayrollPeriodAndIsDeletedFalse(orgUnitId, payrollPeriod);
    }

    public List<HrPayrollResult> getByStatus(Long tenantId, String status) {
        return repository.findByPayrollStatusAndTenantIdAndIsDeletedFalse(status, tenantId);
    }

    public Page<HrPayrollResult> search(Long tenantId, String payrollPeriod, Long orgUnitId,
                                         String status, Pageable pageable) {
        return repository.search(tenantId, payrollPeriod, orgUnitId, status, pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, String approvedBy) {
        HrPayrollResult result = repository.findById(id)
                .orElseThrow(() -> new BusinessException("薪酬结果不存在"));
        if (!"1".equals(result.getPayrollStatus())) {
            throw new BusinessException("当前状态不允许审批");
        }
        repository.approve(id, approvedBy);
        log.info("审批薪酬: id={}, approvedBy={}", id, approvedBy);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchApprove(String payrollPeriod, Long tenantId, String approvedBy) {
        int count = repository.updateStatusByPeriod(tenantId, payrollPeriod, "2");
        log.info("批量审批薪酬: period={}, count={}", payrollPeriod, count);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markPaid(Long id) {
        HrPayrollResult result = repository.findById(id)
                .orElseThrow(() -> new BusinessException("薪酬结果不存在"));
        if (!"2".equals(result.getPayrollStatus())) {
            throw new BusinessException("当前状态不允许发放");
        }
        repository.markPaid(id);
        log.info("标记发放: id={}", id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long id) {
        HrPayrollResult result = repository.findById(id)
                .orElseThrow(() -> new BusinessException("薪酬结果不存在"));
        if ("3".equals(result.getPayrollStatus())) {
            throw new BusinessException("已发放的薪酬不允许撤销");
        }
        repository.revoke(id);
        log.info("撤销薪酬: id={}", id);
    }

    public Object[] sumByPeriod(Long tenantId, String payrollPeriod) {
        return repository.sumByPeriod(tenantId, payrollPeriod).orElse(null);
    }

    public List<Object[]> statsByOrg(Long tenantId, String payrollPeriod) {
        return repository.statsByOrg(tenantId, payrollPeriod);
    }
}
