package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt0022Education;
import com.nexterp.business.hrm.domain.repository.HrIt0022EducationRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0022 - 教育经历 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt0022EducationService {

    private final HrIt0022EducationRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrIt0022Education entity) {
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDate.now());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(LocalDate.of(9999, 12, 31));
        }
        HrIt0022Education saved = repository.save(entity);
        log.info("创建教育经历记录: employeeId={}, school={}", saved.getEmployeeId(), saved.getSchoolName());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt0022Education update(Long id, HrIt0022Education entity) {
        HrIt0022Education existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        existing.setEducationType(entity.getEducationType());
        existing.setEducationLevel(entity.getEducationLevel());
        existing.setDegree(entity.getDegree());
        existing.setSchoolName(entity.getSchoolName());
        existing.setSchoolType(entity.getSchoolType());
        existing.setSchoolLocation(entity.getSchoolLocation());
        existing.setDepartment(entity.getDepartment());
        existing.setMajor(entity.getMajor());
        existing.setMajorCategory(entity.getMajorCategory());
        existing.setStartDate(entity.getStartDate());
        existing.setEndDate(entity.getEndDate());
        existing.setIsHighestEducation(entity.getIsHighestEducation());
        existing.setIsHighestDegree(entity.getIsHighestDegree());
        existing.setIsFirstEducation(entity.getIsFirstEducation());
        existing.setStudyMode(entity.getStudyMode());
        existing.setDiplomaNo(entity.getDiplomaNo());
        existing.setDegreeNo(entity.getDegreeNo());
        existing.setCertificateAttachment(entity.getCertificateAttachment());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt0022Education entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt0022Education getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
    }

    public List<HrIt0022Education> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    public List<HrIt0022Education> getValidOnDate(Long employeeId, LocalDate keyDate) {
        return repository.findValidOnDate(employeeId, keyDate);
    }

    public Optional<HrIt0022Education> getHighestEducation(Long employeeId, LocalDate keyDate) {
        return repository.findHighestEducation(employeeId, keyDate);
    }

    public Optional<HrIt0022Education> getHighestDegree(Long employeeId, LocalDate keyDate) {
        return repository.findHighestDegree(employeeId, keyDate);
    }

    public Optional<HrIt0022Education> getFirstEducation(Long employeeId, LocalDate keyDate) {
        return repository.findFirstEducation(employeeId, keyDate);
    }

    public List<HrIt0022Education> getByEducationLevel(String educationLevel, Long tenantId) {
        return repository.findByEducationLevelAndTenantIdAndIsDeletedFalse(educationLevel, tenantId);
    }

    public List<HrIt0022Education> getBySchoolName(String schoolName, Long tenantId) {
        return repository.findBySchoolNameContainingAndTenantIdAndIsDeletedFalse(schoolName, tenantId);
    }

    public List<HrIt0022Education> getByMajor(String major, Long tenantId) {
        return repository.findByMajorContainingAndTenantIdAndIsDeletedFalse(major, tenantId);
    }

    public List<HrIt0022Education> getPendingVerification(Long tenantId) {
        return repository.findPendingVerification(tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void verify(Long id, boolean success) {
        HrIt0022Education entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setVerifyStatus(success ? "1" : "2");
        entity.setVerifyTime(LocalDate.now());
        repository.save(entity);
        log.info("验证学历: id={}, success={}", id, success);
    }

    public List<HrIt0022Education> getHistory(Long employeeId) {
        return repository.findHistoryByEmployee(employeeId);
    }
}
