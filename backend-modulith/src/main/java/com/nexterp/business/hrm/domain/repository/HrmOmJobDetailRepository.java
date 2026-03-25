package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmOmJobDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 职务详情 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrmOmJobDetailRepository extends JpaRepository<HrmOmJobDetail, Long>,
        JpaSpecificationExecutor<HrmOmJobDetail> {

    /**
     * 根据 OM 对象内码查找
     */
    Optional<HrmOmJobDetail> findByObjectPkAndIsDeletedFalse(Long objectPk);

    /**
     * 根据职务编码查找
     */
    Optional<HrmOmJobDetail> findByJobCodeAndTenantIdAndIsDeletedFalse(
            String jobCode, Long tenantId);

    /**
     * 根据职务族查询
     */
    List<HrmOmJobDetail> findByJobFamilyIdAndTenantIdAndIsDeletedFalse(
            Long jobFamilyId, Long tenantId);

    /**
     * 根据职能分类查询
     */
    List<HrmOmJobDetail> findByJobFunctionAndTenantIdAndIsDeletedFalse(
            String jobFunction, Long tenantId);

    /**
     * 根据父职务查询
     */
    List<HrmOmJobDetail> findByParentJobPkAndTenantIdAndIsDeletedFalse(
            Long parentJobPk, Long tenantId);

    /**
     * 查询指定日期有效的职务详情
     */
    @Query("SELECT d FROM HrmOmJobDetail d WHERE d.objectPk = :objectPk " +
           "AND d.isDeleted = false " +
           "AND d.validFrom <= :keyDate AND d.validTo >= :keyDate")
    Optional<HrmOmJobDetail> findValidOnDate(@Param("objectPk") Long objectPk,
                                              @Param("keyDate") LocalDate keyDate);

    /**
     * 根据职级范围查询职务
     */
    @Query("SELECT d FROM HrmOmJobDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND d.gradeFrom <= :grade AND d.gradeTo >= :grade")
    List<HrmOmJobDetail> findByGrade(@Param("tenantId") Long tenantId,
                                      @Param("grade") String grade);

    /**
     * 根据职等范围查询职务
     */
    @Query("SELECT d FROM HrmOmJobDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND d.levelFrom <= :level AND d.levelTo >= :level")
    List<HrmOmJobDetail> findByLevel(@Param("tenantId") Long tenantId,
                                      @Param("level") Integer level);

    /**
     * 查询根职务 (无父职务)
     */
    @Query("SELECT d FROM HrmOmJobDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND (d.parentJobPk IS NULL OR d.parentJobPk = 0)")
    List<HrmOmJobDetail> findRootJobs(@Param("tenantId") Long tenantId);

    /**
     * 更新关联职位数
     */
    @Query("UPDATE HrmOmJobDetail d SET d.positionCount = :count " +
           "WHERE d.objectPk = :objectPk")
    void updatePositionCount(@Param("objectPk") Long objectPk,
                              @Param("count") Integer count);

    /**
     * 根据学历要求查询
     */
    @Query("SELECT d FROM HrmOmJobDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND d.educationReq <= :educationLevel")
    List<HrmOmJobDetail> findByMaxEducation(@Param("tenantId") Long tenantId,
                                             @Param("educationLevel") Integer educationLevel);

    /**
     * 根据工作年限要求查询
     */
    @Query("SELECT d FROM HrmOmJobDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND d.experienceYears IS NOT NULL AND d.experienceYears <= :years")
    List<HrmOmJobDetail> findByMaxExperience(@Param("tenantId") Long tenantId,
                                              @Param("years") Integer years);
}
