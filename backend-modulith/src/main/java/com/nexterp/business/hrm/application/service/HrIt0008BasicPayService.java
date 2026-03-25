package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt0008BasicPay;
import com.nexterp.business.hrm.domain.repository.HrIt0008BasicPayRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0008 - 基本工资 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt0008BasicPayService {

    private final HrIt0008BasicPayRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrIt0008BasicPay entity) {
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDate.now());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(LocalDate.of(9999, 12, 31));
        }
        // 计算月薪合计
        entity.setMonthlyTotal(entity.calculateMonthlyTotal());
        if (entity.getMonthlyTotal() != null) {
            entity.setAnnualTotal(entity.getMonthlyTotal().multiply(BigDecimal.valueOf(12)));
        }
        HrIt0008BasicPay saved = repository.save(entity);
        log.info("创建薪资记录: employeeId={}", saved.getEmployeeId());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt0008BasicPay update(Long id, HrIt0008BasicPay entity) {
        HrIt0008BasicPay existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        existing.setPayType(entity.getPayType());
        existing.setPayGrade(entity.getPayGrade());
        existing.setPayArea(entity.getPayArea());
        existing.setPayGroup(entity.getPayGroup());
        existing.setPayScale(entity.getPayScale());
        existing.setCurrency(entity.getCurrency());
        existing.setBasicSalary(entity.getBasicSalary());
        existing.setPositionSalary(entity.getPositionSalary());
        existing.setPerformanceBase(entity.getPerformanceBase());
        existing.setPerformanceRatio(entity.getPerformanceRatio());
        existing.setSeniorityPay(entity.getSeniorityPay());
        existing.setJobAllowance(entity.getJobAllowance());
        existing.setTransportAllowance(entity.getTransportAllowance());
        existing.setMealAllowance(entity.getMealAllowance());
        existing.setCommunicationAllowance(entity.getCommunicationAllowance());
        existing.setHousingAllowance(entity.getHousingAllowance());
        existing.setOtherAllowance(entity.getOtherAllowance());
        existing.setReason(entity.getReason());
        existing.setRemark(entity.getRemark());
        // 重新计算
        existing.setMonthlyTotal(existing.calculateMonthlyTotal());
        if (existing.getMonthlyTotal() != null) {
            existing.setAnnualTotal(existing.getMonthlyTotal().multiply(BigDecimal.valueOf(12)));
        }
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt0008BasicPay entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt0008BasicPay getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
    }

    public Optional<HrIt0008BasicPay> getCurrentPay(Long employeeId, LocalDate keyDate) {
        return repository.findCurrentPay(employeeId, keyDate);
    }

    public List<HrIt0008BasicPay> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    public List<HrIt0008BasicPay> getPendingApproval(Long tenantId) {
        return repository.findPendingApproval(tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, String approvedBy) {
        HrIt0008BasicPay entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setApprovalStatus("1");
        entity.setApprovedBy(approvedBy);
        entity.setApprovedAt(LocalDate.now());
        repository.save(entity);
        log.info("审批薪资记录: id={}, approvedBy={}", id, approvedBy);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String approvedBy) {
        HrIt0008BasicPay entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setApprovalStatus("2");
        entity.setApprovedBy(approvedBy);
        entity.setApprovedAt(LocalDate.now());
        repository.save(entity);
        log.info("拒绝薪资记录: id={}, approvedBy={}", id, approvedBy);
    }

    public List<HrIt0008BasicPay> getHistory(Long employeeId) {
        return repository.findHistoryByEmployee(employeeId);
    }
}
