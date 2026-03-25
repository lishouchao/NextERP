package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt0016Contract;
import com.nexterp.business.hrm.domain.repository.HrIt0016ContractRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0016 - 合同 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt0016ContractService {

    private final HrIt0016ContractRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrIt0016Contract entity) {
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDate.now());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(LocalDate.of(9999, 12, 31));
        }
        HrIt0016Contract saved = repository.save(entity);
        log.info("创建合同记录: employeeId={}, contractNo={}", saved.getEmployeeId(), saved.getContractNo());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt0016Contract update(Long id, HrIt0016Contract entity) {
        HrIt0016Contract existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        existing.setContractNo(entity.getContractNo());
        existing.setContractType(entity.getContractType());
        existing.setStartDate(entity.getStartDate());
        existing.setEndDate(entity.getEndDate());
        existing.setProbationStartDate(entity.getProbationStartDate());
        existing.setProbationEndDate(entity.getProbationEndDate());
        existing.setProbationMonths(entity.getProbationMonths());
        existing.setSignTimes(entity.getSignTimes());
        existing.setSignDate(entity.getSignDate());
        existing.setContractStatus(entity.getContractStatus());
        existing.setWorkLocation(entity.getWorkLocation());
        existing.setWorkPosition(entity.getWorkPosition());
        existing.setJobDescription(entity.getJobDescription());
        existing.setWorkHoursType(entity.getWorkHoursType());
        existing.setWeeklyHours(entity.getWeeklyHours());
        existing.setAttachments(entity.getAttachments());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt0016Contract entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt0016Contract getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
    }

    public Optional<HrIt0016Contract> getCurrentContract(Long employeeId, LocalDate keyDate) {
        return repository.findCurrentContract(employeeId, keyDate);
    }

    public List<HrIt0016Contract> getValidOnDate(Long employeeId, LocalDate keyDate) {
        return repository.findValidOnDate(employeeId, keyDate);
    }

    public List<HrIt0016Contract> getExpiringContracts(Long tenantId, int days) {
        LocalDate today = LocalDate.now();
        return repository.findExpiringContracts(tenantId, today, today.plusDays(days));
    }

    public List<HrIt0016Contract> getEndingProbation(Long tenantId, int days) {
        LocalDate today = LocalDate.now();
        return repository.findEndingProbation(tenantId, today, today.plusDays(days));
    }

    @Transactional(rollbackFor = Exception.class)
    public void terminate(Long id, LocalDate terminateDate) {
        HrIt0016Contract entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setContractStatus("3");
        entity.setEndDate(terminateDate);
        repository.save(entity);
        log.info("终止合同: id={}, terminateDate={}", id, terminateDate);
    }

    public List<HrIt0016Contract> getHistory(Long employeeId) {
        return repository.findHistoryByEmployee(employeeId);
    }
}
