package com.nexterp.business.controlling.application.service;

import com.nexterp.business.controlling.domain.model.CoCostCenter;
import com.nexterp.business.controlling.domain.model.CoCostCenterGroup;
import com.nexterp.business.controlling.domain.repository.CoCostCenterRepository;
import com.nexterp.business.controlling.domain.repository.CoCostCenterGroupRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 成本中心服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoCostCenterService {

    private final CoCostCenterRepository costCenterRepository;
    private final CoCostCenterGroupRepository costCenterGroupRepository;

    /**
     * 创建成本中心
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCostCenter(CoCostCenter costCenter) {
        if (costCenterRepository.existsByCostCenterCodeAndTenantIdAndIsDeletedFalse(
                costCenter.getCostCenterCode(), costCenter.getTenantId())) {
            throw new BusinessException("成本中心代码已存在: " + costCenter.getCostCenterCode());
        }

        if (costCenter.getStatus() == null) {
            costCenter.setStatus(1);
        }

        CoCostCenter saved = costCenterRepository.save(costCenter);
        log.info("创建成本中心成功: code={}, name={}", costCenter.getCostCenterCode(), costCenter.getCostCenterName());
        return saved.getId();
    }

    /**
     * 更新成本中心
     */
    @Transactional(rollbackFor = Exception.class)
    public CoCostCenter updateCostCenter(Long id, CoCostCenter costCenter) {
        CoCostCenter existing = costCenterRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成本中心不存在"));

        existing.setCostCenterName(costCenter.getCostCenterName());
        existing.setPersonResponsibleId(costCenter.getPersonResponsibleId());
        existing.setPersonResponsibleName(costCenter.getPersonResponsibleName());
        existing.setDescription(costCenter.getDescription());
        existing.setValidFrom(costCenter.getValidFrom());
        existing.setValidTo(costCenter.getValidTo());

        return costCenterRepository.save(existing);
    }

    /**
     * 删除成本中心
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCostCenter(Long id) {
        CoCostCenter costCenter = costCenterRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成本中心不存在"));

        costCenter.setIsDeleted(true);
        costCenterRepository.save(costCenter);
        log.info("删除成本中心成功: id={}", id);
    }

    /**
     * 获取成本中心详情
     */
    public CoCostCenter getCostCenterById(Long id) {
        return costCenterRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成本中心不存在"));
    }

    /**
     * 根据代码获取成本中心
     */
    public CoCostCenter getCostCenterByCode(String costCenterCode, Long tenantId) {
        return costCenterRepository.findByCostCenterCodeAndTenantIdAndIsDeletedFalse(costCenterCode, tenantId)
                .orElseThrow(() -> new BusinessException("成本中心不存在: " + costCenterCode));
    }

    /**
     * 按类型查询成本中心
     */
    public List<CoCostCenter> listByType(String costCenterType, Long tenantId) {
        return costCenterRepository.findByCostCenterTypeAndTenantIdAndIsDeletedFalseOrderByCostCenterCodeAsc(costCenterType, tenantId);
    }

    /**
     * 按组查询成本中心
     */
    public List<CoCostCenter> listByGroup(Long groupId, Long tenantId) {
        return costCenterRepository.findByCostCenterGroupIdAndTenantIdAndIsDeletedFalseOrderByCostCenterCodeAsc(groupId, tenantId);
    }

    /**
     * 查询有效成本中心
     */
    public List<CoCostCenter> listValidCostCenters(Long tenantId) {
        LocalDate today = LocalDate.now();
        return costCenterRepository.findByTenantIdAndIsDeletedFalseAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByCostCenterCodeAsc(
                tenantId, today, today);
    }

    /**
     * 分页查询
     */
    public Page<CoCostCenter> listCostCenters(Long tenantId, Pageable pageable) {
        return costCenterRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }

    /**
     * 获取成本中心组树
     */
    public List<CoCostCenterGroup> getCostCenterGroupTree(Long tenantId) {
        List<CoCostCenterGroup> allGroups = costCenterGroupRepository.findByTenantIdAndIsDeletedFalseOrderByHierarchyLevelAscGroupCodeAsc(tenantId);
        return buildGroupTree(allGroups, null);
    }

    /**
     * 构建组树
     */
    private List<CoCostCenterGroup> buildGroupTree(List<CoCostCenterGroup> groups, Long parentGroupId) {
        return groups.stream()
                .filter(group -> {
                    if (parentGroupId == null) {
                        return group.getParentGroupId() == null;
                    }
                    return parentGroupId.equals(group.getParentGroupId());
                })
                .peek(group -> {
                    List<CoCostCenterGroup> children = buildGroupTree(groups, group.getId());
                    if (!children.isEmpty()) {
                        group.setChildren(children);
                        group.setIsLeaf(false);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 创建成本中心组
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCostCenterGroup(CoCostCenterGroup group) {
        if (costCenterGroupRepository.existsByGroupCodeAndTenantIdAndIsDeletedFalse(
                group.getGroupCode(), group.getTenantId())) {
            throw new BusinessException("成本中心组代码已存在: " + group.getGroupCode());
        }

        if (group.getStatus() == null) {
            group.setStatus(1);
        }
        if (group.getIsLeaf() == null) {
            group.setIsLeaf(true);
        }

        // 如果有父组，更新父组的isLeaf状态
        if (group.getParentGroupId() != null) {
            costCenterGroupRepository.findById(group.getParentGroupId()).ifPresent(parent -> {
                parent.setIsLeaf(false);
                costCenterGroupRepository.save(parent);
            });
        }

        CoCostCenterGroup saved = costCenterGroupRepository.save(group);
        log.info("创建成本中心组成功: code={}, name={}", group.getGroupCode(), group.getGroupName());
        return saved.getId();
    }
}
