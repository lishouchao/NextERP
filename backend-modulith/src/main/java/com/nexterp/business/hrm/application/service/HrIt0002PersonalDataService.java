package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt0002PersonalData;
import com.nexterp.business.hrm.domain.repository.HrIt0002PersonalDataRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0002 - 个人数据 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt0002PersonalDataService {

    private final HrIt0002PersonalDataRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrIt0002PersonalData entity) {
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDate.now());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(LocalDate.of(9999, 12, 31));
        }
        HrIt0002PersonalData saved = repository.save(entity);
        log.info("创建个人数据记录: employeeId={}", saved.getEmployeeId());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt0002PersonalData update(Long id, HrIt0002PersonalData entity) {
        HrIt0002PersonalData existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        existing.setLastName(entity.getLastName());
        existing.setFirstName(entity.getFirstName());
        existing.setFullName(entity.getFullName());
        existing.setNamePinyin(entity.getNamePinyin());
        existing.setFormerName(entity.getFormerName());
        existing.setGender(entity.getGender());
        existing.setBirthDate(entity.getBirthDate());
        existing.setBirthPlace(entity.getBirthPlace());
        existing.setNationality(entity.getNationality());
        existing.setEthnicity(entity.getEthnicity());
        existing.setMaritalStatus(entity.getMaritalStatus());
        existing.setMaritalStatusDate(entity.getMaritalStatusDate());
        existing.setChildrenCount(entity.getChildrenCount());
        existing.setIdType(entity.getIdType());
        existing.setIdNumber(entity.getIdNumber());
        existing.setIdIssueDate(entity.getIdIssueDate());
        existing.setIdExpiryDate(entity.getIdExpiryDate());
        existing.setIdIssuePlace(entity.getIdIssuePlace());
        existing.setPoliticalStatus(entity.getPoliticalStatus());
        existing.setPoliticalJoinDate(entity.getPoliticalJoinDate());
        existing.setReligion(entity.getReligion());
        existing.setHealthStatus(entity.getHealthStatus());
        existing.setHighestEducation(entity.getHighestEducation());
        existing.setHighestDegree(entity.getHighestDegree());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt0002PersonalData entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt0002PersonalData getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
    }

    public List<HrIt0002PersonalData> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    public Optional<HrIt0002PersonalData> getValidOnDate(Long employeeId, LocalDate keyDate) {
        return repository.findValidOnDate(employeeId, keyDate);
    }

    public HrIt0002PersonalData getByIdNumber(String idNumber, Long tenantId) {
        return repository.findByIdNumberAndTenantIdAndIsDeletedFalse(idNumber, tenantId)
                .orElseThrow(() -> new BusinessException("证件号码不存在"));
    }

    public List<HrIt0002PersonalData> getHistory(Long employeeId) {
        return repository.findHistoryByEmployee(employeeId);
    }
}
