package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrmOmPositionDetail;
import com.nexterp.business.hrm.domain.repository.HrmOmPositionDetailRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 职位详情服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrmOmPositionDetailService {

    private final HrmOmPositionDetailRepository positionDetailRepository;

    /**
     * 创建职位详情
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPositionDetail(HrmOmPositionDetail detail) {
        // 检查职位编码是否已存在
        if (positionDetailRepository.findByPositionCodeAndTenantIdAndIsDeletedFalse(
                detail.getPositionCode(), detail.getTenantId()).isPresent()) {
            throw new BusinessException("职位编码已存在: " + detail.getPositionCode());
        }

        // 设置默认值
        if (detail.getValidFrom() == null) {
            detail.setValidFrom(LocalDate.now());
        }
        if (detail.getValidTo() == null) {
            detail.setValidTo(LocalDate.of(9999, 12, 31));
        }
        if (detail.getHeadcount() == null) {
            detail.setHeadcount(1);
        }
        if (detail.getCurrentCount() == null) {
            detail.setCurrentCount(0);
        }
        if (detail.getPositionStatus() == null) {
            detail.setPositionStatus("VACANT");
        }
        if (detail.getPositionType() == null) {
            detail.setPositionType("FULL");
        }
        if (detail.getIsManager() == null) {
            detail.setIsManager(false);
        }
        if (detail.getIsKeyPosition() == null) {
            detail.setIsKeyPosition(false);
        }

        // 更新职位状态
        updatePositionStatus(detail);

        HrmOmPositionDetail saved = positionDetailRepository.save(detail);
        log.info("创建职位详情成功: positionCode={}", saved.getPositionCode());
        return saved.getId();
    }

    /**
     * 更新职位详情
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmOmPositionDetail updatePositionDetail(Long id, HrmOmPositionDetail detail) {
        HrmOmPositionDetail existing = positionDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException("职位不存在"));

        existing.setCostCenterCode(detail.getCostCenterCode());
        existing.setHeadcount(detail.getHeadcount());
        existing.setGrade(detail.getGrade());
        existing.setJobLevel(detail.getJobLevel());
        existing.setSalaryMin(detail.getSalaryMin());
        existing.setSalaryMax(detail.getSalaryMax());
        existing.setIsManager(detail.getIsManager());
        existing.setIsKeyPosition(detail.getIsKeyPosition());
        existing.setRemark(detail.getRemark());

        // 更新职位状态
        updatePositionStatus(existing);

        return positionDetailRepository.save(existing);
    }

    /**
     * 删除职位详情
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePositionDetail(Long id) {
        HrmOmPositionDetail detail = positionDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException("职位不存在"));

        // 检查是否有任职者
        if (detail.getCurrentCount() != null && detail.getCurrentCount() > 0) {
            throw new BusinessException("该职位有任职者，无法删除");
        }

        detail.setIsDeleted(true);
        positionDetailRepository.save(detail);
        log.info("删除职位详情成功: id={}", id);
    }

    /**
     * 获取职位详情
     */
    public HrmOmPositionDetail getById(Long id) {
        return positionDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException("职位不存在"));
    }

    /**
     * 根据 OM 对象内码获取
     */
    public HrmOmPositionDetail getByObjectPk(Long objectPk) {
        return positionDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("职位不存在"));
    }

    /**
     * 根据职位编码获取
     */
    public HrmOmPositionDetail getByPositionCode(String positionCode, Long tenantId) {
        return positionDetailRepository.findByPositionCodeAndTenantIdAndIsDeletedFalse(positionCode, tenantId)
                .orElseThrow(() -> new BusinessException("职位不存在: " + positionCode));
    }

    /**
     * 获取指定日期有效的职位
     */
    public HrmOmPositionDetail getValidOnDate(Long objectPk, LocalDate keyDate) {
        return positionDetailRepository.findValidOnDate(objectPk, keyDate)
                .orElseThrow(() -> new BusinessException("职位在指定日期无有效记录"));
    }

    /**
     * 根据组织查询职位
     */
    public List<HrmOmPositionDetail> getByOrgObjectPk(Long orgObjectPk, Long tenantId) {
        return positionDetailRepository.findByOrgObjectPkAndTenantIdAndIsDeletedFalse(orgObjectPk, tenantId);
    }

    /**
     * 根据职务查询职位
     */
    public List<HrmOmPositionDetail> getByJobObjectPk(Long jobObjectPk, Long tenantId) {
        return positionDetailRepository.findByJobObjectPkAndTenantIdAndIsDeletedFalse(jobObjectPk, tenantId);
    }

    /**
     * 根据职位状态查询
     */
    public List<HrmOmPositionDetail> getByPositionStatus(String positionStatus, Long tenantId) {
        return positionDetailRepository.findByPositionStatusAndTenantIdAndIsDeletedFalse(positionStatus, tenantId);
    }

    /**
     * 获取空缺职位
     */
    public List<HrmOmPositionDetail> getVacantPositions(Long tenantId, LocalDate keyDate) {
        return positionDetailRepository.findVacantPositions(tenantId, keyDate);
    }

    /**
     * 获取有编制空缺的职位
     */
    public List<HrmOmPositionDetail> getPositionsWitVacancy(Long tenantId, LocalDate keyDate) {
        return positionDetailRepository.findWithVacancy(tenantId, keyDate);
    }

    /**
     * 获取关键岗位
     */
    public List<HrmOmPositionDetail> getKeyPositions(Long tenantId, LocalDate keyDate) {
        return positionDetailRepository.findKeyPositions(tenantId, keyDate);
    }

    /**
     * 获取经理岗位
     */
    public List<HrmOmPositionDetail> getManagerPositions(Long tenantId, LocalDate keyDate) {
        return positionDetailRepository.findManagerPositions(tenantId, keyDate);
    }

    /**
     * 分配任职者
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignHolder(Long objectPk, Long holderObjectPk, String employeeNo, String holderName) {
        HrmOmPositionDetail detail = positionDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("职位不存在"));

        // 检查是否有空缺
        if (!detail.hasVacancy()) {
            throw new BusinessException("职位已满，无法分配");
        }

        // 增加当前人数
        int updated = positionDetailRepository.incrementCurrentCount(objectPk);
        if (updated == 0) {
            throw new BusinessException("职位已满，分配失败");
        }

        // 如果是单人职位且是第一个任职者，更新任职者信息
        if (detail.isSinglePosition() && detail.getCurrentCount() == 0) {
            positionDetailRepository.updateHolder(objectPk, holderObjectPk, employeeNo, holderName);
        }

        // 重新获取更新状态
        detail = positionDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk).orElseThrow();
        updatePositionStatus(detail);
        positionDetailRepository.save(detail);

        log.info("分配任职者成功: position={}, employee={}", detail.getPositionCode(), employeeNo);
    }

    /**
     * 移除任职者
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeHolder(Long objectPk) {
        HrmOmPositionDetail detail = positionDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("职位不存在"));

        if (detail.getCurrentCount() == null || detail.getCurrentCount() == 0) {
            throw new BusinessException("职位无任职者");
        }

        // 减少当前人数
        positionDetailRepository.decrementCurrentCount(objectPk);

        // 如果是单人职位，清空任职者信息
        if (detail.isSinglePosition()) {
            positionDetailRepository.clearHolder(objectPk);
        }

        // 重新获取更新状态
        detail = positionDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk).orElseThrow();
        updatePositionStatus(detail);
        positionDetailRepository.save(detail);

        log.info("移除任职者成功: position={}", detail.getPositionCode());
    }

    /**
     * 冻结职位
     */
    @Transactional(rollbackFor = Exception.class)
    public void freezePosition(Long objectPk) {
        HrmOmPositionDetail detail = positionDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("职位不存在"));

        detail.setPositionStatus("FROZEN");
        positionDetailRepository.save(detail);
        log.info("冻结职位成功: position={}", detail.getPositionCode());
    }

    /**
     * 解冻职位
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfreezePosition(Long objectPk) {
        HrmOmPositionDetail detail = positionDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("职位不存在"));

        updatePositionStatus(detail);
        positionDetailRepository.save(detail);
        log.info("解冻职位成功: position={}", detail.getPositionCode());
    }

    /**
     * 废除职位
     */
    @Transactional(rollbackFor = Exception.class)
    public void abolishPosition(Long objectPk) {
        HrmOmPositionDetail detail = positionDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("职位不存在"));

        if (detail.getCurrentCount() != null && detail.getCurrentCount() > 0) {
            throw new BusinessException("职位有任职者，无法废除");
        }

        detail.setPositionStatus("ABOLISHED");
        positionDetailRepository.save(detail);
        log.info("废除职位成功: position={}", detail.getPositionCode());
    }

    /**
     * 按组织统计职位数
     */
    public Map<Long, Long> countByOrg(Long tenantId, LocalDate keyDate) {
        List<Object[]> results = positionDetailRepository.countByOrg(tenantId, keyDate);
        return results.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
    }

    /**
     * 按职务统计职位数
     */
    public Map<Long, Long> countByJob(Long tenantId, LocalDate keyDate) {
        List<Object[]> results = positionDetailRepository.countByJob(tenantId, keyDate);
        return results.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
    }

    /**
     * 分页查询
     */
    public Page<HrmOmPositionDetail> listPositionDetails(Long tenantId, Pageable pageable) {
        return positionDetailRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }

    /**
     * 更新职位状态
     */
    private void updatePositionStatus(HrmOmPositionDetail detail) {
        if ("FROZEN".equals(detail.getPositionStatus()) || "ABOLISHED".equals(detail.getPositionStatus())) {
            return; // 保持冻结或废除状态
        }

        if (detail.getCurrentCount() != null && detail.getCurrentCount() > 0) {
            detail.setPositionStatus("OCCUPIED");
        } else {
            detail.setPositionStatus("VACANT");
        }
    }
}
