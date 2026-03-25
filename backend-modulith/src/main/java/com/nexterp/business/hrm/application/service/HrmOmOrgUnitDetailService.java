package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrmOmOrgUnitDetail;
import com.nexterp.business.hrm.domain.repository.HrmOmOrgUnitDetailRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 组织单元详情服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrmOmOrgUnitDetailService {

    private final HrmOmOrgUnitDetailRepository orgUnitDetailRepository;

    /**
     * 创建组织单元详情
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrgUnitDetail(HrmOmOrgUnitDetail detail) {
        // 检查组织编码是否已存在
        if (orgUnitDetailRepository.findByOrgCodeAndTenantIdAndIsDeletedFalse(
                detail.getOrgCode(), detail.getTenantId()).isPresent()) {
            throw new BusinessException("组织编码已存在: " + detail.getOrgCode());
        }

        // 设置默认值
        if (detail.getValidFrom() == null) {
            detail.setValidFrom(LocalDate.now());
        }
        if (detail.getValidTo() == null) {
            detail.setValidTo(LocalDate.of(9999, 12, 31));
        }
        if (detail.getOrgLevel() == null) {
            detail.setOrgLevel(1);
        }
        if (detail.getHeadcount() == null) {
            detail.setHeadcount(0);
        }

        // 计算组织路径
        if (detail.getParentObjectPk() != null) {
            HrmOmOrgUnitDetail parent = orgUnitDetailRepository
                    .findByObjectPkAndIsDeletedFalse(detail.getParentObjectPk())
                    .orElseThrow(() -> new BusinessException("父组织不存在"));

            detail.setOrgLevel(parent.getOrgLevel() + 1);
            detail.setOrgPath(parent.getOrgPath() + "/" + parent.getOrgCode());
        } else {
            detail.setOrgLevel(1);
            detail.setOrgPath("");
        }

        HrmOmOrgUnitDetail saved = orgUnitDetailRepository.save(detail);
        log.info("创建组织单元详情成功: orgCode={}", saved.getOrgCode());
        return saved.getId();
    }

    /**
     * 更新组织单元详情
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmOmOrgUnitDetail updateOrgUnitDetail(Long id, HrmOmOrgUnitDetail detail) {
        HrmOmOrgUnitDetail existing = orgUnitDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException("组织单元不存在"));

        existing.setCompanyCode(detail.getCompanyCode());
        existing.setCostCenterCode(detail.getCostCenterCode());
        existing.setMaxHeadcount(detail.getMaxHeadcount());
        existing.setBudgetHeadcount(detail.getBudgetHeadcount());
        existing.setManagerPk(detail.getManagerPk());
        existing.setManagerEmployeeNo(detail.getManagerEmployeeNo());
        existing.setManagerName(detail.getManagerName());
        existing.setPhone(detail.getPhone());
        existing.setEmail(detail.getEmail());
        existing.setOfficeAddress(detail.getOfficeAddress());
        existing.setRemark(detail.getRemark());

        return orgUnitDetailRepository.save(existing);
    }

    /**
     * 删除组织单元详情
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrgUnitDetail(Long id) {
        HrmOmOrgUnitDetail detail = orgUnitDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException("组织单元不存在"));

        // 检查是否有子组织
        List<HrmOmOrgUnitDetail> children = orgUnitDetailRepository
                .findByParentObjectPkAndTenantIdAndIsDeletedFalse(id, detail.getTenantId());
        if (!children.isEmpty()) {
            throw new BusinessException("该组织存在子组织，无法删除");
        }

        detail.setIsDeleted(true);
        orgUnitDetailRepository.save(detail);
        log.info("删除组织单元详情成功: id={}", id);
    }

    /**
     * 获取组织单元详情
     */
    public HrmOmOrgUnitDetail getById(Long id) {
        return orgUnitDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException("组织单元不存在"));
    }

    /**
     * 根据 OM 对象内码获取
     */
    public HrmOmOrgUnitDetail getByObjectPk(Long objectPk) {
        return orgUnitDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("组织单元不存在"));
    }

    /**
     * 根据组织编码获取
     */
    public HrmOmOrgUnitDetail getByOrgCode(String orgCode, Long tenantId) {
        return orgUnitDetailRepository.findByOrgCodeAndTenantIdAndIsDeletedFalse(orgCode, tenantId)
                .orElseThrow(() -> new BusinessException("组织不存在: " + orgCode));
    }

    /**
     * 获取子组织
     */
    public List<HrmOmOrgUnitDetail> getChildren(Long parentObjectPk, Long tenantId) {
        return orgUnitDetailRepository.findByParentObjectPkAndTenantIdAndIsDeletedFalse(parentObjectPk, tenantId);
    }

    /**
     * 获取根组织
     */
    public List<HrmOmOrgUnitDetail> getRootOrgs(Long tenantId) {
        return orgUnitDetailRepository.findRootOrgs(tenantId);
    }

    /**
     * 获取指定日期有效的组织
     */
    public HrmOmOrgUnitDetail getValidOnDate(Long objectPk, LocalDate keyDate) {
        return orgUnitDetailRepository.findValidOnDate(objectPk, keyDate)
                .orElseThrow(() -> new BusinessException("组织单元在指定日期无有效记录"));
    }

    /**
     * 根据公司代码查询
     */
    public List<HrmOmOrgUnitDetail> getByCompanyCode(String companyCode, Long tenantId) {
        return orgUnitDetailRepository.findByCompanyCodeAndTenantIdAndIsDeletedFalse(companyCode, tenantId);
    }

    /**
     * 根据成本中心查询
     */
    public List<HrmOmOrgUnitDetail> getByCostCenterCode(String costCenterCode, Long tenantId) {
        return orgUnitDetailRepository.findByCostCenterCodeAndTenantIdAndIsDeletedFalse(costCenterCode, tenantId);
    }

    /**
     * 查询超编组织
     */
    public List<HrmOmOrgUnitDetail> getOverstaffed(Long tenantId) {
        return orgUnitDetailRepository.findOverstaffed(tenantId);
    }

    /**
     * 更新人数
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateHeadcount(Long objectPk, Integer newHeadcount) {
        HrmOmOrgUnitDetail detail = orgUnitDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("组织单元不存在"));

        detail.setHeadcount(newHeadcount);
        orgUnitDetailRepository.save(detail);
        log.info("更新组织人数: objectPk={}, headcount={}", objectPk, newHeadcount);
    }

    /**
     * 增加人数
     */
    @Transactional(rollbackFor = Exception.class)
    public void incrementHeadcount(Long objectPk) {
        HrmOmOrgUnitDetail detail = orgUnitDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("组织单元不存在"));

        detail.setHeadcount(detail.getHeadcount() + 1);
        orgUnitDetailRepository.save(detail);
    }

    /**
     * 减少人数
     */
    @Transactional(rollbackFor = Exception.class)
    public void decrementHeadcount(Long objectPk) {
        HrmOmOrgUnitDetail detail = orgUnitDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("组织单元不存在"));

        if (detail.getHeadcount() > 0) {
            detail.setHeadcount(detail.getHeadcount() - 1);
            orgUnitDetailRepository.save(detail);
        }
    }

    /**
     * 分页查询
     */
    public Page<HrmOmOrgUnitDetail> listOrgUnits(Long tenantId, Pageable pageable) {
        return orgUnitDetailRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }
}
