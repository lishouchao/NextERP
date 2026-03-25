package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt0022Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0022 - 教育经历 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt0022EducationRepository extends JpaRepository<HrIt0022Education, Long>,
        JpaSpecificationExecutor<HrIt0022Education> {

    /**
     * 根据员工ID查询
     */
    List<HrIt0022Education> findByEmployeeIdAndIsDeletedFalse(Long employeeId);

    /**
     * 根据员工编号查询
     */
    List<HrIt0022Education> findByEmployeeNoAndTenantIdAndIsDeletedFalse(String employeeNo, Long tenantId);

    /**
     * 查询指定日期有效的记录
     */
    @Query("SELECT e FROM HrIt0022Education e WHERE e.employeeId = :employeeId " +
           "AND e.isDeleted = false " +
           "AND e.validFrom <= :keyDate AND e.validTo >= :keyDate")
    List<HrIt0022Education> findValidOnDate(@Param("employeeId") Long employeeId,
                                             @Param("keyDate") LocalDate keyDate);

    /**
     * 查询最高学历
     */
    @Query("SELECT e FROM HrIt0022Education e WHERE e.employeeId = :employeeId " +
           "AND e.isHighestEducation = true AND e.isDeleted = false " +
           "AND e.validFrom <= :keyDate AND e.validTo >= :keyDate")
    Optional<HrIt0022Education> findHighestEducation(@Param("employeeId") Long employeeId,
                                                      @Param("keyDate") LocalDate keyDate);

    /**
     * 查询最高学位
     */
    @Query("SELECT e FROM HrIt0022Education e WHERE e.employeeId = :employeeId " +
           "AND e.isHighestDegree = true AND e.isDeleted = false " +
           "AND e.validFrom <= :keyDate AND e.validTo >= :keyDate")
    Optional<HrIt0022Education> findHighestDegree(@Param("employeeId") Long employeeId,
                                                   @Param("keyDate") LocalDate keyDate);

    /**
     * 查询第一学历
     */
    @Query("SELECT e FROM HrIt0022Education e WHERE e.employeeId = :employeeId " +
           "AND e.isFirstEducation = true AND e.isDeleted = false " +
           "AND e.validFrom <= :keyDate AND e.validTo >= :keyDate")
    Optional<HrIt0022Education> findFirstEducation(@Param("employeeId") Long employeeId,
                                                    @Param("keyDate") LocalDate keyDate);

    /**
     * 根据学历级别查询
     */
    List<HrIt0022Education> findByEducationLevelAndTenantIdAndIsDeletedFalse(
            String educationLevel, Long tenantId);

    /**
     * 根据学校查询
     */
    List<HrIt0022Education> findBySchoolNameContainingAndTenantIdAndIsDeletedFalse(
            String schoolName, Long tenantId);

    /**
     * 根据专业查询
     */
    List<HrIt0022Education> findByMajorContainingAndTenantIdAndIsDeletedFalse(
            String major, Long tenantId);

    /**
     * 查询待验证学历
     */
    @Query("SELECT e FROM HrIt0022Education e WHERE e.tenantId = :tenantId " +
           "AND e.verifyStatus = '0' AND e.isDeleted = false")
    List<HrIt0022Education> findPendingVerification(@Param("tenantId") Long tenantId);

    /**
     * 查询员工历史记录
     */
    @Query("SELECT e FROM HrIt0022Education e WHERE e.employeeId = :employeeId " +
           "AND e.isDeleted = false ORDER BY e.startDate DESC")
    List<HrIt0022Education> findHistoryByEmployee(@Param("employeeId") Long employeeId);
}
