package com.nexterp.business.production.application.service;

import com.nexterp.business.production.domain.model.ProWorkProcess;
import com.nexterp.business.production.domain.repository.ProWorkProcessRepository;
import com.nexterp.business.production.dto.CreateWorkProcessRequest;
import com.nexterp.business.production.dto.ProWorkProcessDTO;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工序服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProWorkProcessService {

    private final ProWorkProcessRepository workProcessRepository;

    /**
     * 创建工序
     *
     * @param request 创建请求
     * @return 工序ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkProcess(CreateWorkProcessRequest request) {
        // 检查工序编码是否已存在
        if (workProcessRepository.existsByProcessCodeAndTenantIdAndIsDeletedFalse(
                request.getProcessCode(), request.getTenantId())) {
            throw new BusinessException("工序编码已存在: " + request.getProcessCode());
        }

        ProWorkProcess process = ProWorkProcess.builder()
                .tenantId(request.getTenantId())
                .processCode(request.getProcessCode())
                .processName(request.getProcessName())
                .processType(request.getProcessType())
                .categoryId(request.getCategoryId())
                .categoryName(request.getCategoryName())
                .departmentId(request.getDepartmentId())
                .departmentName(request.getDepartmentName())
                .workCenterId(request.getWorkCenterId())
                .workCenterName(request.getWorkCenterName())
                .standardManHours(request.getStandardManHours())
                .standardMachineHours(request.getStandardMachineHours())
                .setupTime(request.getSetupTime())
                .waitTime(request.getWaitTime())
                .laborRate(request.getLaborRate())
                .machineRate(request.getMachineRate())
                .variableOverheadRate(request.getVariableOverheadRate())
                .fixedOverheadRate(request.getFixedOverheadRate())
                .minBatchQty(request.getMinBatchQty())
                .maxBatchQty(request.getMaxBatchQty())
                .isBottleneck(request.getIsBottleneck())
                .isQualityCheck(request.getIsQualityCheck())
                .qcPlanId(request.getQcPlanId())
                .sortOrder(request.getSortOrder())
                .status(1) // 默认启用
                .remark(request.getRemark())
                .isDeleted(false)
                .build();

        ProWorkProcess saved = workProcessRepository.save(process);
        log.info("创建工序成功: code={}, name={}", saved.getProcessCode(), saved.getProcessName());
        return saved.getId();
    }

    /**
     * 更新工序
     *
     * @param id      工序ID
     * @param request 更新请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkProcess(Long id, CreateWorkProcessRequest request) {
        ProWorkProcess existing = workProcessRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工序不存在"));

        // 检查工序编码是否被其他工序使用
        if (!existing.getProcessCode().equals(request.getProcessCode()) &&
            workProcessRepository.existsByProcessCodeAndTenantIdAndIsDeletedFalse(
                    request.getProcessCode(), existing.getTenantId())) {
            throw new BusinessException("工序编码已被其他工序使用: " + request.getProcessCode());
        }

        existing.setProcessCode(request.getProcessCode());
        existing.setProcessName(request.getProcessName());
        existing.setProcessType(request.getProcessType());
        existing.setCategoryId(request.getCategoryId());
        existing.setCategoryName(request.getCategoryName());
        existing.setDepartmentId(request.getDepartmentId());
        existing.setDepartmentName(request.getDepartmentName());
        existing.setWorkCenterId(request.getWorkCenterId());
        existing.setWorkCenterName(request.getWorkCenterName());
        existing.setStandardManHours(request.getStandardManHours());
        existing.setStandardMachineHours(request.getStandardMachineHours());
        existing.setSetupTime(request.getSetupTime());
        existing.setWaitTime(request.getWaitTime());
        existing.setLaborRate(request.getLaborRate());
        existing.setMachineRate(request.getMachineRate());
        existing.setVariableOverheadRate(request.getVariableOverheadRate());
        existing.setFixedOverheadRate(request.getFixedOverheadRate());
        existing.setMinBatchQty(request.getMinBatchQty());
        existing.setMaxBatchQty(request.getMaxBatchQty());
        existing.setIsBottleneck(request.getIsBottleneck());
        existing.setIsQualityCheck(request.getIsQualityCheck());
        existing.setQcPlanId(request.getQcPlanId());
        existing.setSortOrder(request.getSortOrder());
        existing.setRemark(request.getRemark());

        workProcessRepository.save(existing);
        log.info("更新工序成功: id={}", id);
    }

    /**
     * 删除工序（软删除）
     *
     * @param id 工序ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkProcess(Long id) {
        ProWorkProcess process = workProcessRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工序不存在"));

        process.setIsDeleted(true);
        workProcessRepository.save(process);
        log.info("删除工序成功: id={}", id);
    }

    /**
     * 根据ID获取工序详情
     *
     * @param id 工序ID
     * @return 工序DTO
     */
    public ProWorkProcessDTO getWorkProcessById(Long id) {
        ProWorkProcess process = workProcessRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工序不存在"));
        return convertToDTO(process);
    }

    /**
     * 分页查询工序
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param current  当前页（从1开始）
     * @param size     每页大小
     * @return 分页结果
     */
    public PageResult<ProWorkProcessDTO> listWorkProcesses(Long tenantId, Integer status, int current, int size) {
        Specification<ProWorkProcess> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.equal(root.get("isDeleted"), false));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<ProWorkProcess> page = workProcessRepository.findAll(spec, PageRequest.of(current - 1, size));

        List<ProWorkProcessDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<ProWorkProcessDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(current)
                .size(size)
                .build();
    }

    // ========== 私有方法 ==========

    /**
     * 实体转DTO
     */
    private ProWorkProcessDTO convertToDTO(ProWorkProcess process) {
        return ProWorkProcessDTO.builder()
                .id(process.getId())
                .tenantId(process.getTenantId())
                .processCode(process.getProcessCode())
                .processName(process.getProcessName())
                .processType(process.getProcessType())
                .processTypeName(process.getProcessTypeName())
                .categoryId(process.getCategoryId())
                .categoryName(process.getCategoryName())
                .departmentId(process.getDepartmentId())
                .departmentName(process.getDepartmentName())
                .workCenterId(process.getWorkCenterId())
                .workCenterName(process.getWorkCenterName())
                .standardManHours(process.getStandardManHours())
                .standardMachineHours(process.getStandardMachineHours())
                .setupTime(process.getSetupTime())
                .waitTime(process.getWaitTime())
                .laborRate(process.getLaborRate())
                .machineRate(process.getMachineRate())
                .variableOverheadRate(process.getVariableOverheadRate())
                .fixedOverheadRate(process.getFixedOverheadRate())
                .minBatchQty(process.getMinBatchQty())
                .maxBatchQty(process.getMaxBatchQty())
                .isBottleneck(process.getIsBottleneck())
                .isQualityCheck(process.getIsQualityCheck())
                .qcPlanId(process.getQcPlanId())
                .sortOrder(process.getSortOrder())
                .status(process.getStatus())
                .statusName(getStatusName(process.getStatus()))
                .remark(process.getRemark())
                .createdAt(process.getCreatedAt())
                .createdBy(process.getCreatedBy())
                .updatedAt(process.getUpdatedAt())
                .updatedBy(process.getUpdatedBy())
                .build();
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "禁用";
            case 1 -> "启用";
            default -> "未知";
        };
    }
}
