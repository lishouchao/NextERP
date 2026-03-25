package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt0009BankDetails;
import com.nexterp.business.hrm.domain.repository.HrIt0009BankDetailsRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0009 - 银行信息 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt0009BankDetailsService {

    private final HrIt0009BankDetailsRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrIt0009BankDetails entity) {
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDate.now());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(LocalDate.of(9999, 12, 31));
        }
        HrIt0009BankDetails saved = repository.save(entity);
        log.info("创建银行信息记录: employeeId={}, bankType={}", saved.getEmployeeId(), saved.getBankType());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt0009BankDetails update(Long id, HrIt0009BankDetails entity) {
        HrIt0009BankDetails existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        existing.setBankCode(entity.getBankCode());
        existing.setBankName(entity.getBankName());
        existing.setBankCnaps(entity.getBankCnaps());
        existing.setBranchName(entity.getBranchName());
        existing.setBranchProvince(entity.getBranchProvince());
        existing.setBranchCity(entity.getBranchCity());
        existing.setAccountType(entity.getAccountType());
        existing.setBankAccount(entity.getBankAccount());
        existing.setAccountHolder(entity.getAccountHolder());
        existing.setCurrency(entity.getCurrency());
        existing.setIsPrimary(entity.getIsPrimary());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt0009BankDetails entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt0009BankDetails getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
    }

    public List<HrIt0009BankDetails> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    public Optional<HrIt0009BankDetails> getPrimaryAccount(Long employeeId, LocalDate keyDate) {
        return repository.findPrimaryAccount(employeeId, keyDate);
    }

    public Optional<HrIt0009BankDetails> getValidOnDateByType(Long employeeId, String bankType, LocalDate keyDate) {
        return repository.findValidOnDateByType(employeeId, bankType, keyDate);
    }

    public List<HrIt0009BankDetails> getAllValidOnDate(Long employeeId, LocalDate keyDate) {
        return repository.findAllValidOnDate(employeeId, keyDate);
    }

    @Transactional(rollbackFor = Exception.class)
    public void verifyBankAccount(Long id, boolean success) {
        HrIt0009BankDetails entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setVerifyStatus(success ? "1" : "2");
        entity.setVerifyTime(LocalDateTime.now());
        repository.save(entity);
        log.info("银行账户验证: id={}, success={}", id, success);
    }

    public List<HrIt0009BankDetails> getHistory(Long employeeId) {
        return repository.findHistoryByEmployee(employeeId);
    }
}
