package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrLeaveType;
import com.nexterp.business.hrm.domain.repository.HrLeaveTypeRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 假期类型 Service
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrLeaveTypeService {

    private final HrLeaveTypeRepository repository;

    @Transactional(rollbackFor = Exception.class)
    public Long create(HrLeaveType entity) {
        // 检查编码唯一性
        if (repository.findByLeaveTypeCodeAndTenantIdAndIsDeletedFalse(
                entity.getLeaveTypeCode(), entity.getTenantId()).isPresent()) {
            throw new BusinessException("假期类型编码已存在");
        }
        HrLeaveType saved = repository.save(entity);
        log.info("创建假期类型: code={}, name={}", saved.getLeaveTypeCode(), saved.getLeaveTypeName());
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public HrLeaveType update(Long id, HrLeaveType entity) {
        HrLeaveType existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期类型不存在"));
        existing.setLeaveTypeName(entity.getLeaveTypeName());
        existing.setLeaveCategory(entity.getLeaveCategory());
        existing.setIsPaid(entity.getIsPaid());
        existing.setPayRatio(entity.getPayRatio());
        existing.setRequireApproval(entity.getRequireApproval());
        existing.setApprovalLevel(entity.getApprovalLevel());
        existing.setRequireAttachment(entity.getRequireAttachment());
        existing.setMinUnit(entity.getMinUnit());
        existing.setAnnualLimit(entity.getAnnualLimit());
        existing.setCanCarryOver(entity.getCanCarryOver());
        existing.setMaxCarryOverDays(entity.getMaxCarryOverDays());
        existing.setCarryOverExpireMonths(entity.getCarryOverExpireMonths());
        existing.setApplyGender(entity.getApplyGender());
        existing.setApplyEmployeeGroups(entity.getApplyEmployeeGroups());
        existing.setMinTenureYears(entity.getMinTenureYears());
        existing.setSortOrder(entity.getSortOrder());
        existing.setStatus(entity.getStatus());
        existing.setRemark(entity.getRemark());
        return repository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HrLeaveType entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期类型不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
        log.info("删除假期类型: id={}", id);
    }

    public HrLeaveType getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期类型不存在"));
    }

    public Optional<HrLeaveType> getByCode(String code, Long tenantId) {
        return repository.findByLeaveTypeCodeAndTenantIdAndIsDeletedFalse(code, tenantId);
    }

    public List<HrLeaveType> getByTenantId(Long tenantId) {
        return repository.findByTenantIdAndIsDeletedFalseOrderBySortOrderAsc(tenantId);
    }

    public List<HrLeaveType> getEnabledByTenantId(Long tenantId) {
        return repository.findByTenantIdAndStatusAndIsDeletedFalseOrderBySortOrderAsc(tenantId, 1);
    }

    public List<HrLeaveType> getByCategory(Long tenantId, String category) {
        return repository.findByLeaveCategoryAndTenantIdAndIsDeletedFalse(category, tenantId);
    }

    public List<HrLeaveType> getPaidLeaveTypes(Long tenantId) {
        return repository.findByIsPaidAndTenantIdAndIsDeletedFalse(true, tenantId);
    }

    public Page<HrLeaveType> search(Long tenantId, String keyword, Pageable pageable) {
        return repository.findByTenantIdAndKeyword(tenantId, keyword, pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        HrLeaveType entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("假期类型不存在"));
        entity.setStatus(status);
        repository.save(entity);
    }
}
