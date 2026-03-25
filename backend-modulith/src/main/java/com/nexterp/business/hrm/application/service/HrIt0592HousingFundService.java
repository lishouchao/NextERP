package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrIt0592HousingFund;
import com.nexterp.business.hrm.domain.repository.HrIt0592HousingFundRepository;
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
 * InfoType 0592 - 公积金信息 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrIt0592HousingFundService {

    private final HrIt0592HousingFundRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrIt0592HousingFund entity) {
        // 设置默认时间有效性
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDate.now());
        }
        if (entity.getValidTo() == null) {
            entity.setValidTo(LocalDate.of(9999, 12, 31));
        }
        // 计算金额
        entity.setPersonalAmount(entity.calculatePersonalAmount());
        entity.setCompanyAmount(entity.calculateCompanyAmount());
        entity.setTotalAmount(entity.calculateTotalAmount());
        HrIt0592HousingFund saved = repository.save(entity);
        log.info("创建公积金记录: employeeId={}, type={}", saved.getEmployeeId(), saved.getFundType());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrIt0592HousingFund update(Long id, HrIt0592HousingFund entity) {
        HrIt0592HousingFund existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("公积金记录不存在"));
        existing.setFundType(entity.getFundType());
        existing.setFundCenterCode(entity.getFundCenterCode());
        existing.setFundCenterName(entity.getFundCenterName());
        existing.setFundBase(entity.getFundBase());
        existing.setPersonalRate(entity.getPersonalRate());
        existing.setCompanyRate(entity.getCompanyRate());
        existing.setFundAccount(entity.getFundAccount());
        existing.setFundStatus(entity.getFundStatus());
        existing.setFirstDepositDate(entity.getFirstDepositDate());
        existing.setBaseMin(entity.getBaseMin());
        existing.setBaseMax(entity.getBaseMax());
        existing.setRemark(entity.getRemark());
        // 重新计算金额
        existing.setPersonalAmount(existing.calculatePersonalAmount());
        existing.setCompanyAmount(existing.calculateCompanyAmount());
        existing.setTotalAmount(existing.calculateTotalAmount());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrIt0592HousingFund entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("公积金记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    public HrIt0592HousingFund getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("公积金记录不存在"));
    }

    public List<HrIt0592HousingFund> getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    public List<HrIt0592HousingFund> getByEmployeeNo(String employeeNo, Long tenantId) {
        return repository.findByEmployeeNoAndTenantIdAndIsDeletedFalse(employeeNo, tenantId);
    }

    public List<HrIt0592HousingFund> getByEmployeeAndType(Long employeeId, String fundType) {
        return repository.findByEmployeeIdAndFundTypeAndIsDeletedFalse(employeeId, fundType);
    }

    public Optional<HrIt0592HousingFund> getValidOnDateByType(Long employeeId, String fundType, LocalDate date) {
        return repository.findValidOnDateByType(employeeId, fundType, date);
    }

    public List<HrIt0592HousingFund> getAllValidOnDate(Long employeeId, LocalDate date) {
        return repository.findAllValidOnDate(employeeId, date);
    }

    public List<HrIt0592HousingFund> getByStatus(Long tenantId, String status) {
        return repository.findByFundStatusAndTenantIdAndIsDeletedFalse(status, tenantId);
    }

    public List<HrIt0592HousingFund> getByFundCenter(String fundCenterCode, Long tenantId) {
        return repository.findByFundCenterCodeAndTenantIdAndIsDeletedFalse(fundCenterCode, tenantId);
    }

    public Optional<HrIt0592HousingFund> getByFundAccount(String fundAccount, Long tenantId) {
        return repository.findByFundAccountAndTenantIdAndIsDeletedFalse(fundAccount, tenantId);
    }

    public List<HrIt0592HousingFund> getHistory(Long employeeId) {
        return repository.findHistoryByEmployee(employeeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        HrIt0592HousingFund entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("公积金记录不存在"));
        entity.setFundStatus(status);
        repository.save(entity);
    }

    public BigDecimal getPersonalAmount(Long employeeId, String fundType, LocalDate date) {
        return getValidOnDateByType(employeeId, fundType, date)
                .map(HrIt0592HousingFund::getPersonalAmount)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTotalFundPersonal(Long employeeId, LocalDate date) {
        return getAllValidOnDate(employeeId, date).stream()
                .map(HrIt0592HousingFund::getPersonalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
