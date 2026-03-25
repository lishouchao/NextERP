package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt0021Family;
import com.nexterp.business.hrm.domain.repository.HrIt0021FamilyRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * InfoType 0021 - 家庭成员 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt0021FamilyService {

    private final HrIt0021FamilyRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrIt0021Family entity) {
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDate.now());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(LocalDate.of(9999, 12, 31));
        }
        HrIt0021Family saved = repository.save(entity);
        log.info("创建家庭成员记录: employeeId={}, type={}", saved.getEmployeeId(), saved.getFamilyType());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt0021Family update(Long id, HrIt0021Family entity) {
        HrIt0021Family existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        existing.setFamilyType(entity.getFamilyType());
        existing.setMemberName(entity.getMemberName());
        existing.setGender(entity.getGender());
        existing.setBirthDate(entity.getBirthDate());
        existing.setIdType(entity.getIdType());
        existing.setIdNumber(entity.getIdNumber());
        existing.setWorkUnit(entity.getWorkUnit());
        existing.setOccupation(entity.getOccupation());
        existing.setPhone(entity.getPhone());
        existing.setAddress(entity.getAddress());
        existing.setIsEmergencyContact(entity.getIsEmergencyContact());
        existing.setRelationship(entity.getRelationship());
        existing.setIsDependent(entity.getIsDependent());
        existing.setIsChildForTax(entity.getIsChildForTax());
        existing.setIsElderForTax(entity.getIsElderForTax());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt0021Family entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt0021Family getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
    }

    public List<HrIt0021Family> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    public List<HrIt0021Family> getByEmployeeIdAndType(Long employeeId, String familyType) {
        return repository.findByEmployeeIdAndFamilyTypeAndIsDeletedFalse(employeeId, familyType);
    }

    public List<HrIt0021Family> getValidOnDate(Long employeeId, LocalDate keyDate) {
        return repository.findValidOnDate(employeeId, keyDate);
    }

    public List<HrIt0021Family> getEmergencyContacts(Long employeeId, LocalDate keyDate) {
        return repository.findEmergencyContacts(employeeId, keyDate);
    }

    public List<HrIt0021Family> getDependents(Long employeeId, LocalDate keyDate) {
        return repository.findDependents(employeeId, keyDate);
    }

    public List<HrIt0021Family> getChildrenForTax(Long employeeId, LocalDate keyDate) {
        return repository.findChildrenForTax(employeeId, keyDate);
    }

    public List<HrIt0021Family> getEldersForTax(Long employeeId, LocalDate keyDate) {
        return repository.findEldersForTax(employeeId, keyDate);
    }

    public List<HrIt0021Family> getHistory(Long employeeId) {
        return repository.findHistoryByEmployee(employeeId);
    }
}
