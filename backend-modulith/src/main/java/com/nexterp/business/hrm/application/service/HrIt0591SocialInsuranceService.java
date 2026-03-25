package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt0591SocialInsurance;
import com.nexterp.business.hrm.domain.repository.HrIt0591SocialInsuranceRepository;
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
 * InfoType 0591 - 社保信息 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt0591SocialInsuranceService {

    private final HrIt0591SocialInsuranceRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrIt0591SocialInsurance entity) {
        // 设置默认时间有效性
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDate.now());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(LocalDate.of(9999, 12, 31));
        }
        // 计算合计
        entity.setTotalPersonal(entity.calculateTotalPersonal());
        entity.setTotalCompany(entity.calculateTotalCompany());
        HrIt0591SocialInsurance saved = repository.save(entity);
        log.info("创建社保记录: employeeId={}, city={}", saved.getEmployeeId(), saved.getCityCode());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt0591SocialInsurance update(Long id, HrIt0591SocialInsurance entity) {
        HrIt0591SocialInsurance existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("社保记录不存在"));
        existing.setConfigId(entity.getConfigId());
        existing.setCityCode(entity.getCityCode());
        existing.setCityName(entity.getCityName());
        existing.setSocialBase(entity.getSocialBase());
        existing.setPensionPersonal(entity.getPensionPersonal());
        existing.setPensionCompany(entity.getPensionCompany());
        existing.setMedicalPersonal(entity.getMedicalPersonal());
        existing.setMedicalCompany(entity.getMedicalCompany());
        existing.setUnemploymentPersonal(entity.getUnemploymentPersonal());
        existing.setUnemploymentCompany(entity.getUnemploymentCompany());
        existing.setInjuryCompany(entity.getInjuryCompany());
        existing.setMaternityCompany(entity.getMaternityCompany());
        existing.setCriticalIllnessPersonal(entity.getCriticalIllnessPersonal());
        existing.setCriticalIllnessCompany(entity.getCriticalIllnessCompany());
        existing.setSocialNo(entity.getSocialNo());
        existing.setInsuranceStatus(entity.getInsuranceStatus());
        existing.setFirstInsuranceDate(entity.getFirstInsuranceDate());
        existing.setRemark(entity.getRemark());
        // 重新计算合计
        existing.setTotalPersonal(existing.calculateTotalPersonal());
        existing.setTotalCompany(existing.calculateTotalCompany());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt0591SocialInsurance entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("社保记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt0591SocialInsurance getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("社保记录不存在"));
    }

    public List<HrIt0591SocialInsurance> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    public List<HrIt0591SocialInsurance> getByEmployeeNo(String employeeNo, Long tenantId) {
        return repository.findByEmployeeNoAndTenantIdAndIsDeletedFalse(employeeNo, tenantId);
    }

    public Optional<HrIt0591SocialInsurance> getValidOnDate(Long employeeId, LocalDate date) {
        return repository.findValidOnDate(employeeId, date);
    }

    public List<HrIt0591SocialInsurance> getByCityCode(String cityCode, Long tenantId) {
        return repository.findByCityCodeAndTenantIdAndIsDeletedFalse(cityCode, tenantId);
    }

    public List<HrIt0591SocialInsurance> getByStatus(Long tenantId, String status) {
        return repository.findByInsuranceStatusAndTenantIdAndIsDeletedFalse(status, tenantId);
    }

    public List<HrIt0591SocialInsurance> getByConfigId(Long configId, Long tenantId) {
        return repository.findByConfigIdAndTenantIdAndIsDeletedFalse(configId, tenantId);
    }

    public List<HrIt0591SocialInsurance> getHistory(Long employeeId) {
        return repository.findHistoryByEmployee(employeeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        HrIt0591SocialInsurance entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("社保记录不存在"));
        entity.setInsuranceStatus(status);
        repository.save(entity);
    }

    public BigDecimal getTotalPersonal(Long employeeId, LocalDate date) {
        return getValidOnDate(employeeId, date)
                .map(HrIt0591SocialInsurance::getTotalPersonal)
                .orElse(BigDecimal.ZERO);
    }
}
