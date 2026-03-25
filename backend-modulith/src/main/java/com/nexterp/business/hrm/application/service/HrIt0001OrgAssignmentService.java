package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt0001OrgAssignment;
import com.nexterp.business.hrm.domain.repository.HrIt0001OrgAssignmentRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0001 - 组织分配 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt0001OrgAssignmentService {

    private final HrIt0001OrgAssignmentRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrIt0001OrgAssignment entity) {
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDate.now());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(LocalDate.of(9999, 12, 31));
        }
        HrIt0001OrgAssignment saved = repository.save(entity);
        log.info("创建组织分配记录: employeeId={}, orgId={}", saved.getEmployeeId(), saved.getOrgId());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt0001OrgAssignment update(Long id, HrIt0001OrgAssignment entity) {
        HrIt0001OrgAssignment existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        // 更新字段
        existing.setCompanyCode(entity.getCompanyCode());
        existing.setPersonnelArea(entity.getPersonnelArea());
        existing.setPersonnelSubarea(entity.getPersonnelSubarea());
        existing.setEmployeeGroup(entity.getEmployeeGroup());
        existing.setEmployeeSubgroup(entity.getEmployeeSubgroup());
        existing.setOrgPk(entity.getOrgPk());
        existing.setOrgId(entity.getOrgId());
        existing.setOrgName(entity.getOrgName());
        existing.setPositionPk(entity.getPositionPk());
        existing.setPositionId(entity.getPositionId());
        existing.setPositionName(entity.getPositionName());
        existing.setJobPk(entity.getJobPk());
        existing.setJobId(entity.getJobId());
        existing.setJobName(entity.getJobName());
        existing.setCostCenterCode(entity.getCostCenterCode());
        existing.setManagerEmployeeNo(entity.getManagerEmployeeNo());
        existing.setManagerName(entity.getManagerName());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt0001OrgAssignment entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt0001OrgAssignment getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
    }

    public List<HrIt0001OrgAssignment> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    public Optional<HrIt0001OrgAssignment> getValidOnDate(Long employeeId, LocalDate keyDate) {
        return repository.findValidOnDate(employeeId, keyDate);
    }

    public Optional<HrIt0001OrgAssignment> getPrimaryAssignment(Long employeeId, LocalDate keyDate) {
        return repository.findPrimaryAssignment(employeeId, keyDate);
    }

    public List<HrIt0001OrgAssignment> getByOrg(Long orgPk, Long tenantId, LocalDate keyDate) {
        return repository.findByOrg(orgPk, tenantId, keyDate);
    }

    public List<HrIt0001OrgAssignment> getByPosition(Long positionPk, Long tenantId, LocalDate keyDate) {
        return repository.findByPosition(positionPk, tenantId, keyDate);
    }

    public List<HrIt0001OrgAssignment> getHistory(Long employeeId) {
        return repository.findHistoryByEmployee(employeeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delimit(Long id, LocalDate validTo) {
        HrIt0001OrgAssignment entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        if (validTo.isBefore(entity.getValidFrom())) {
            throw new BusinessException("截止日期不能早于生效日期");
        }
        entity.setValidTo(validTo);
        repository.save(entity);
    }
}
