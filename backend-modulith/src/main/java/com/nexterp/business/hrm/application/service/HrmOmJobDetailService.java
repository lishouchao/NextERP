package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrmOmJobDetail;
import com.nexterp.business.hrm.domain.repository.HrmOmJobDetailRepository;
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
 * 职务详情服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrmOmJobDetailService {

    private final HrmOmJobDetailRepository jobDetailRepository;

    /**
     * 创建职务详情
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createJobDetail(HrmOmJobDetail detail) {
        // 检查职务编码是否已存在
        if (jobDetailRepository.findByJobCodeAndTenantIdAndIsDeletedFalse(
                detail.getJobCode(), detail.getTenantId()).isPresent()) {
            throw new BusinessException("职务编码已存在: " + detail.getJobCode());
        }

        // 设置默认值
        if (detail.getValidFrom() == null) {
            detail.setValidFrom(LocalDate.now());
        }
        if (detail.getValidTo() == null) {
            detail.setValidTo(LocalDate.of(9999, 12, 31));
        }
        if (detail.getPositionCount() == null) {
            detail.setPositionCount(0);
        }

        HrmOmJobDetail saved = jobDetailRepository.save(detail);
        log.info("创建职务详情成功: jobCode={}", saved.getJobCode());
        return saved.getId();
    }

    /**
     * 更新职务详情
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmOmJobDetail updateJobDetail(Long id, HrmOmJobDetail detail) {
        HrmOmJobDetail existing = jobDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException("职务不存在"));

        existing.setJobFamilyId(detail.getJobFamilyId());
        existing.setJobFamilyCode(detail.getJobFamilyCode());
        existing.setJobFamilyName(detail.getJobFamilyName());
        existing.setJobFunction(detail.getJobFunction());
        existing.setGradeFrom(detail.getGradeFrom());
        existing.setGradeTo(detail.getGradeTo());
        existing.setLevelFrom(detail.getLevelFrom());
        existing.setLevelTo(detail.getLevelTo());
        existing.setResponsibility(detail.getResponsibility());
        existing.setQualificationReq(detail.getQualificationReq());
        existing.setCompetencyReq(detail.getCompetencyReq());
        existing.setEducationReq(detail.getEducationReq());
        existing.setExperienceYears(detail.getExperienceYears());
        existing.setCertificatesReq(detail.getCertificatesReq());
        existing.setRemark(detail.getRemark());

        return jobDetailRepository.save(existing);
    }

    /**
     * 删除职务详情
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteJobDetail(Long id) {
        HrmOmJobDetail detail = jobDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException("职务不存在"));

        // 检查是否有关联职位
        if (detail.getPositionCount() != null && detail.getPositionCount() > 0) {
            throw new BusinessException("该职务有关联职位，无法删除");
        }

        detail.setIsDeleted(true);
        jobDetailRepository.save(detail);
        log.info("删除职务详情成功: id={}", id);
    }

    /**
     * 获取职务详情
     */
    public HrmOmJobDetail getById(Long id) {
        return jobDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException("职务不存在"));
    }

    /**
     * 根据 OM 对象内码获取
     */
    public HrmOmJobDetail getByObjectPk(Long objectPk) {
        return jobDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("职务不存在"));
    }

    /**
     * 根据职务编码获取
     */
    public HrmOmJobDetail getByJobCode(String jobCode, Long tenantId) {
        return jobDetailRepository.findByJobCodeAndTenantIdAndIsDeletedFalse(jobCode, tenantId)
                .orElseThrow(() -> new BusinessException("职务不存在: " + jobCode));
    }

    /**
     * 获取指定日期有效的职务
     */
    public HrmOmJobDetail getValidOnDate(Long objectPk, LocalDate keyDate) {
        return jobDetailRepository.findValidOnDate(objectPk, keyDate)
                .orElseThrow(() -> new BusinessException("职务在指定日期无有效记录"));
    }

    /**
     * 根据职务族查询
     */
    public List<HrmOmJobDetail> getByJobFamilyId(Long jobFamilyId, Long tenantId) {
        return jobDetailRepository.findByJobFamilyIdAndTenantIdAndIsDeletedFalse(jobFamilyId, tenantId);
    }

    /**
     * 根据职能分类查询
     */
    public List<HrmOmJobDetail> getByJobFunction(String jobFunction, Long tenantId) {
        return jobDetailRepository.findByJobFunctionAndTenantIdAndIsDeletedFalse(jobFunction, tenantId);
    }

    /**
     * 根据职级查询
     */
    public List<HrmOmJobDetail> getByGrade(String grade, Long tenantId) {
        return jobDetailRepository.findByGrade(tenantId, grade);
    }

    /**
     * 根据职等查询
     */
    public List<HrmOmJobDetail> getByLevel(Integer level, Long tenantId) {
        return jobDetailRepository.findByLevel(tenantId, level);
    }

    /**
     * 获取根职务 (无父职务)
     */
    public List<HrmOmJobDetail> getRootJobs(Long tenantId) {
        return jobDetailRepository.findRootJobs(tenantId);
    }

    /**
     * 更新关联职位数
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePositionCount(Long objectPk, Integer count) {
        jobDetailRepository.updatePositionCount(objectPk, count);
        log.info("更新职务职位数: objectPk={}, count={}", objectPk, count);
    }

    /**
     * 增加职位数
     */
    @Transactional(rollbackFor = Exception.class)
    public void incrementPositionCount(Long objectPk) {
        HrmOmJobDetail detail = jobDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("职务不存在"));

        detail.setPositionCount(detail.getPositionCount() + 1);
        jobDetailRepository.save(detail);
    }

    /**
     * 减少职位数
     */
    @Transactional(rollbackFor = Exception.class)
    public void decrementPositionCount(Long objectPk) {
        HrmOmJobDetail detail = jobDetailRepository.findByObjectPkAndIsDeletedFalse(objectPk)
                .orElseThrow(() -> new BusinessException("职务不存在"));

        if (detail.getPositionCount() > 0) {
            detail.setPositionCount(detail.getPositionCount() - 1);
            jobDetailRepository.save(detail);
        }
    }

    /**
     * 分页查询
     */
    public Page<HrmOmJobDetail> listJobDetails(Long tenantId, Pageable pageable) {
        return jobDetailRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }
}
