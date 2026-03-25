package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrLeaveQuota;
import com.nexterp.business.hrm.domain.repository.HrLeaveQuotaRepository;
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
 * 假期额度 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrLeaveQuotaService {

    private final HrLeaveQuotaRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrLeaveQuota entity) {
        // 刷新计算
        entity.refresh();
        HrLeaveQuota saved = repository.save(entity);
        log.info("创建假期额度: employeeId={}, leaveTypeId={}, year={}",
                saved.getEmployeeId(), saved.getLeaveTypeId(), saved.getQuotaYear());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrLeaveQuota update(Long id, HrLeaveQuota entity) {
        HrLeaveQuota existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期额度不存在"));
        existing.setEntitledDays(entity.getEntitledDays());
        existing.setCarriedOverDays(entity.getCarriedOverDays());
        existing.setAdjustedDays(entity.getAdjustedDays());
        existing.setCarryOverExpireDate(entity.getCarryOverExpireDate());
        existing.setRemark(entity.getRemark());
        existing.refresh();
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrLeaveQuota entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期额度不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrLeaveQuota getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期额度不存在"));
    }

    public List<HrLeaveQuota> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    public Optional<HrLeaveQuota> getByEmployeeAndTypeAndYear(Long employeeId, Long leaveTypeId, Integer year) {
        return repository.findByEmployeeIdAndLeaveTypeIdAndQuotaYearAndIsDeletedFalse(
                employeeId, leaveTypeId, year);
    }

    public List<HrLeaveQuota> getByEmployeeAndYear(Long employeeId, Integer year) {
        return repository.findByEmployeeIdAndQuotaYearAndIsDeletedFalse(employeeId, year);
    }

    public List<HrLeaveQuota> getByTenantAndYear(Long tenantId, Integer year) {
        return repository.findByTenantIdAndQuotaYearAndIsDeletedFalse(tenantId, year);
    }

    @Transactional(rollbackFor = Exception.class)
    public void useQuota(Long id, BigDecimal days) {
        HrLeaveQuota quota = repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期额度不存在"));
        if (!quota.hasEnoughQuota(days)) {
            throw new BusinessException("假期额度不足");
        }
        quota.setUsedDays(quota.getUsedDays().add(days));
        quota.refresh();
        repository.save(quota);
        log.info("使用假期额度: quotaId={}, days={}", id, days);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addPendingDays(Long id, BigDecimal days) {
        HrLeaveQuota quota = repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期额度不存在"));
        quota.setPendingDays(quota.getPendingDays().add(days));
        quota.refresh();
        repository.save(quota);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmPendingDays(Long id, BigDecimal days) {
        HrLeaveQuota quota = repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期额度不存在"));
        quota.setPendingDays(quota.getPendingDays().subtract(days));
        quota.setUsedDays(quota.getUsedDays().add(days));
        quota.refresh();
        repository.save(quota);
    }

    @Transactional(rollbackFor = Exception.class)
    public void rollbackPendingDays(Long id, BigDecimal days) {
        HrLeaveQuota quota = repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期额度不存在"));
        quota.setPendingDays(quota.getPendingDays().subtract(days));
        quota.refresh();
        repository.save(quota);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adjustQuota(Long id, BigDecimal days, String reason) {
        HrLeaveQuota quota = repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期额度不存在"));
        quota.setAdjustedDays(quota.getAdjustedDays().add(days));
        quota.setRemark(reason);
        quota.refresh();
        repository.save(quota);
        log.info("调整假期额度: quotaId={}, days={}, reason={}", id, days, reason);
    }

    @Transactional(rollbackFor = Exception.class)
    public void carryOver(Long employeeId, Integer fromYear, Integer toYear) {
        List<HrLeaveQuota> fromQuotas = repository.findByEmployeeIdAndQuotaYearAndIsDeletedFalse(
                employeeId, fromYear);
        for (HrLeaveQuota fromQuota : fromQuotas) {
            if (fromQuota.getRemainingDays().compareTo(BigDecimal.ZERO) > 0) {
                Optional<HrLeaveQuota> toQuotaOpt = repository
                        .findByEmployeeIdAndLeaveTypeIdAndQuotaYearAndIsDeletedFalse(
                                employeeId, fromQuota.getLeaveTypeId(), toYear);
                if (toQuotaOpt.isPresent()) {
                    HrLeaveQuota toQuota = toQuotaOpt.get();
                    toQuota.setCarriedOverDays(fromQuota.getRemainingDays());
                    toQuota.refresh();
                    repository.save(toQuota);
                }
            }
        }
        log.info("结转假期额度: employeeId={}, fromYear={}, toYear={}", employeeId, fromYear, toYear);
    }
}
