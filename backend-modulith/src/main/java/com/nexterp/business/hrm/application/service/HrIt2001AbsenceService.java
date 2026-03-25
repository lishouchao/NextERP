package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt2001Absence;
import com.nexterp.business.hrm.domain.model.HrLeaveQuota;
import com.nexterp.business.hrm.domain.repository.HrIt2001AbsenceRepository;
import com.nexterp.business.hrm.domain.repository.HrLeaveQuotaRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 2001 - 请假/缺勤 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt2001AbsenceService {

    private final HrIt2001AbsenceRepository repository;
    private final HrLeaveQuotaRepository quotaRepository;

    @Transactional(rollbackFor = Exception.class)
    public Long submit(HrIt2001Absence entity) {
        // 验证请假天数
        if (entity.getAbsenceDays() == null || entity.getAbsenceDays().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("请假天数必须大于0");
        }
        // 检查额度
        if (entity.getQuotaId() != null) {
            HrLeaveQuota quota = quotaRepository.findById(entity.getQuotaId())
                    .orElseThrow(() -> new BusinessException("假期额度不存在"));
            if (!quota.hasEnoughQuota(entity.getAbsenceDays())) {
                throw new BusinessException("假期额度不足");
            }
        }
        entity.setApprovalStatus("1"); // 待审批
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(entity.getStartDate());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(entity.getEndDate());
        }
        HrIt2001Absence saved = repository.save(entity);
        log.info("提交请假申请: employeeId={}, days={}", saved.getEmployeeId(), saved.getAbsenceDays());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt2001Absence update(Long id, HrIt2001Absence entity) {
        HrIt2001Absence existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("请假记录不存在"));
        if (!existing.isEditable()) {
            throw new BusinessException("当前状态不允许修改");
        }
        existing.setLeaveTypeId(entity.getLeaveTypeId());
        existing.setLeaveTypeCode(entity.getLeaveTypeCode());
        existing.setLeaveTypeName(entity.getLeaveTypeName());
        existing.setStartDate(entity.getStartDate());
        existing.setEndDate(entity.getEndDate());
        existing.setStartTime(entity.getStartTime());
        existing.setEndTime(entity.getEndTime());
        existing.setAbsenceDays(entity.getAbsenceDays());
        existing.setAbsenceHours(entity.getAbsenceHours());
        existing.setReason(entity.getReason());
        existing.setContactInfo(entity.getContactInfo());
        existing.setAttachments(entity.getAttachments());
        existing.setValidFrom(entity.getStartDate());
        existing.setValidTo(entity.getEndDate());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt2001Absence entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("请假记录不存在"));
        if (!entity.isEditable()) {
            throw new BusinessException("当前状态不允许删除");
        }
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt2001Absence getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("请假记录不存在"));
    }

    public List<HrIt2001Absence> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalseOrderByStartDateDesc(employeeId);
    }

    public Optional<HrIt2001Absence> getByRequestNo(String requestNo, Long tenantId) {
        return repository.findByRequestNoAndTenantIdAndIsDeletedFalse(requestNo, tenantId);
    }

    public List<HrIt2001Absence> getByEmployeeAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return repository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
    }

    public List<HrIt2001Absence> getByApprovalStatus(Long tenantId, String status) {
        return repository.findByApprovalStatusAndTenantIdAndIsDeletedFalse(status, tenantId);
    }

    public List<HrIt2001Absence> getByApprover(Long approverId) {
        return repository.findByCurrentApproverIdAndApprovalStatusAndIsDeletedFalse(approverId, "1");
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, Long approverId, String approverName, String comment) {
        HrIt2001Absence absence = repository.findById(id)
                .orElseThrow(() -> new BusinessException("请假记录不存在"));
        if (!"1".equals(absence.getApprovalStatus())) {
            throw new BusinessException("当前状态不允许审批");
        }
        absence.setApprovalStatus("2"); // 已通过
        absence.setApprovedAt(LocalDateTime.now());
        absence.setApprovalComment(comment);
        repository.save(absence);

        // 更新额度
        if (absence.getQuotaId() != null) {
            HrLeaveQuota quota = quotaRepository.findById(absence.getQuotaId())
                    .orElseThrow(() -> new BusinessException("假期额度不存在"));
            quota.setUsedDays(quota.getUsedDays().add(absence.getAbsenceDays()));
            quota.refresh();
            quotaRepository.save(quota);
        }
        log.info("审批通过请假: id={}, approver={}", id, approverName);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, Long approverId, String approverName, String comment) {
        HrIt2001Absence absence = repository.findById(id)
                .orElseThrow(() -> new BusinessException("请假记录不存在"));
        if (!"1".equals(absence.getApprovalStatus())) {
            throw new BusinessException("当前状态不允许审批");
        }
        absence.setApprovalStatus("3"); // 已拒绝
        absence.setApprovedAt(LocalDateTime.now());
        absence.setApprovalComment(comment);
        repository.save(absence);

        // 回滚待审批额度
        if (absence.getQuotaId() != null) {
            HrLeaveQuota quota = quotaRepository.findById(absence.getQuotaId())
                    .orElseThrow(() -> new BusinessException("假期额度不存在"));
            quota.setPendingDays(quota.getPendingDays().subtract(absence.getAbsenceDays()));
            quota.refresh();
            quotaRepository.save(quota);
        }
        log.info("审批拒绝请假: id={}, approver={}", id, approverName);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, String reason) {
        HrIt2001Absence absence = repository.findById(id)
                .orElseThrow(() -> new BusinessException("请假记录不存在"));
        if (!absence.isCancelable()) {
            throw new BusinessException("当前状态不允许撤销");
        }
        String oldStatus = absence.getApprovalStatus();
        absence.setApprovalStatus("4"); // 已撤销
        absence.setCancelStatus("1");
        absence.setCanceledAt(LocalDateTime.now());
        repository.save(absence);

        // 回滚额度
        if (absence.getQuotaId() != null && "2".equals(oldStatus)) {
            HrLeaveQuota quota = quotaRepository.findById(absence.getQuotaId())
                    .orElseThrow(() -> new BusinessException("假期额度不存在"));
            quota.setUsedDays(quota.getUsedDays().subtract(absence.getAbsenceDays()));
            quota.refresh();
            quotaRepository.save(quota);
        }
        log.info("撤销请假: id={}, reason={}", id, reason);
    }

    public BigDecimal sumAbsenceDays(Long employeeId, Long leaveTypeId, Integer year) {
        return repository.sumAbsenceDaysByEmployeeAndTypeAndYear(employeeId, leaveTypeId, year)
                .orElse(BigDecimal.ZERO);
    }
}
