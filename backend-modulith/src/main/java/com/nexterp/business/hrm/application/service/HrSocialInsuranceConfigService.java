package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrSocialInsuranceConfig;
import com.nexterp.business.hrm.domain.repository.HrSocialInsuranceConfigRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 社保配置 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrSocialInsuranceConfigService {

    private final HrSocialInsuranceConfigRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrSocialInsuranceConfig entity) {
        // 检查配置名称唯一性
        if (repository.findByConfigNameAndTenantIdAndIsDeletedFalse(
                entity.getConfigName(), entity.getTenantId()).isPresent()) {
            throw new BusinessException("配置名称已存在");
        }
        HrSocialInsuranceConfig saved = repository.save(entity);
        log.info("创建社保配置: name={}, city={}", saved.getConfigName(), saved.getCityCode());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrSocialInsuranceConfig update(Long id, HrSocialInsuranceConfig entity) {
        HrSocialInsuranceConfig existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("社保配置不存在"));
        existing.setConfigName(entity.getConfigName());
        existing.setCityCode(entity.getCityCode());
        existing.setCityName(entity.getCityName());
        existing.setValidFrom(entity.getValidFrom());
        existing.setValidTo(entity.getValidTo());
        existing.setPensionPersonalRate(entity.getPensionPersonalRate());
        existing.setPensionCompanyRate(entity.getPensionCompanyRate());
        existing.setMedicalPersonalRate(entity.getMedicalPersonalRate());
        existing.setMedicalCompanyRate(entity.getMedicalCompanyRate());
        existing.setUnemploymentPersonalRate(entity.getUnemploymentPersonalRate());
        existing.setUnemploymentCompanyRate(entity.getUnemploymentCompanyRate());
        existing.setInjuryCompanyRate(entity.getInjuryCompanyRate());
        existing.setMaternityCompanyRate(entity.getMaternityCompanyRate());
        existing.setCriticalIllnessPersonalRate(entity.getCriticalIllnessPersonalRate());
        existing.setCriticalIllnessCompanyRate(entity.getCriticalIllnessCompanyRate());
        existing.setBaseMin(entity.getBaseMin());
        existing.setBaseMax(entity.getBaseMax());
        existing.setStatus(entity.getStatus());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrSocialInsuranceConfig entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("社保配置不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
        log.info("删除社保配置: id={}", id);
    }

    public HrSocialInsuranceConfig getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("社保配置不存在"));
    }

    public Optional<HrSocialInsuranceConfig> getByName(String configName, Long tenantId) {
        return repository.findByConfigNameAndTenantIdAndIsDeletedFalse(configName, tenantId);
    }

    public List<HrSocialInsuranceConfig> getByCityCode(String cityCode, Long tenantId) {
        return repository.findByCityCodeAndTenantIdAndIsDeletedFalse(cityCode, tenantId);
    }

    public List<HrSocialInsuranceConfig> getEnabledByCityCode(String cityCode) {
        return repository.findByCityCodeAndStatusAndIsDeletedFalse(cityCode, 1);
    }

    public Optional<HrSocialInsuranceConfig> getValidOnDate(String cityCode, Long tenantId, LocalDate date) {
        return repository.findValidOnDate(cityCode, tenantId, date);
    }

    public List<HrSocialInsuranceConfig> getByTenantId(Long tenantId) {
        return repository.findByTenantIdAndIsDeletedFalse(tenantId);
    }

    public List<HrSocialInsuranceConfig> getEnabledByTenantId(Long tenantId) {
        return repository.findByTenantIdAndStatusAndIsDeletedFalse(tenantId, 1);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        HrSocialInsuranceConfig entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("社保配置不存在"));
        entity.setStatus(status);
        repository.save(entity);
    }
}
