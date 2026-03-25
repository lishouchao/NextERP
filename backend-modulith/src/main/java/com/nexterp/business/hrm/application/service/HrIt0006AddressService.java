package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt0006Address;
import com.nexterp.business.hrm.domain.repository.HrIt0006AddressRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0006 - 地址 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt0006AddressService {

    private final HrIt0006AddressRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrIt0006Address entity) {
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDate.now());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(LocalDate.of(9999, 12, 31));
        }
        HrIt0006Address saved = repository.save(entity);
        log.info("创建地址记录: employeeId={}, type={}", saved.getEmployeeId(), saved.getAddressType());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt0006Address update(Long id, HrIt0006Address entity) {
        HrIt0006Address existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        existing.setCountry(entity.getCountry());
        existing.setProvinceCode(entity.getProvinceCode());
        existing.setProvinceName(entity.getProvinceName());
        existing.setCityCode(entity.getCityCode());
        existing.setCityName(entity.getCityName());
        existing.setDistrictCode(entity.getDistrictCode());
        existing.setDistrictName(entity.getDistrictName());
        existing.setStreet(entity.getStreet());
        existing.setAddressLine(entity.getAddressLine());
        existing.setPostalCode(entity.getPostalCode());
        existing.setPhone(entity.getPhone());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt0006Address entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt0006Address getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
    }

    public List<HrIt0006Address> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    public List<HrIt0006Address> getByEmployeeIdAndType(Long employeeId, String addressType) {
        return repository.findByEmployeeIdAndAddressTypeAndIsDeletedFalse(employeeId, addressType);
    }

    public Optional<HrIt0006Address> getValidOnDateByType(Long employeeId, String addressType, LocalDate keyDate) {
        return repository.findValidOnDateByType(employeeId, addressType, keyDate);
    }

    public List<HrIt0006Address> getAllValidOnDate(Long employeeId, LocalDate keyDate) {
        return repository.findAllValidOnDate(employeeId, keyDate);
    }

    public List<HrIt0006Address> getHistory(Long employeeId) {
        return repository.findHistoryByEmployee(employeeId);
    }
}
