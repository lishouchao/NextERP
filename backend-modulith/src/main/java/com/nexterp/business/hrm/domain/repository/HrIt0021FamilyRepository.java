package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt0021Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0021 - 家庭成员 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt0021FamilyRepository extends JpaRepository<HrIt0021Family, Long>,
        JpaSpecificationExecutor<HrIt0021Family> {

    /**
     * 根据员工ID查询
     */
    List<HrIt0021Family> findByEmployeeIdAndIsDeletedFalse(Long employeeId);

    /**
     * 根据员工编号查询
     */
    List<HrIt0021Family> findByEmployeeNoAndTenantIdAndIsDeletedFalse(String employeeNo, Long tenantId);

    /**
     * 根据员工和家庭成员类型查询
     */
    List<HrIt0021Family> findByEmployeeIdAndFamilyTypeAndIsDeletedFalse(
            Long employeeId, String familyType);

    /**
     * 查询指定日期有效的记录
     */
    @Query("SELECT f FROM HrIt0021Family f WHERE f.employeeId = :employeeId " +
           "AND f.isDeleted = false " +
           "AND f.validFrom <= :keyDate AND f.validTo >= :keyDate")
    List<HrIt0021Family> findValidOnDate(@Param("employeeId") Long employeeId,
                                          @Param("keyDate") LocalDate keyDate);

    /**
     * 查询紧急联系人
     */
    @Query("SELECT f FROM HrIt0021Family f WHERE f.employeeId = :employeeId " +
           "AND f.isEmergencyContact = true AND f.isDeleted = false " +
           "AND f.validFrom <= :keyDate AND f.validTo >= :keyDate")
    List<HrIt0021Family> findEmergencyContacts(@Param("employeeId") Long employeeId,
                                                @Param("keyDate") LocalDate keyDate);

    /**
     * 查询抚养对象
     */
    @Query("SELECT f FROM HrIt0021Family f WHERE f.employeeId = :employeeId " +
           "AND f.isDependent = true AND f.isDeleted = false " +
           "AND f.validFrom <= :keyDate AND f.validTo >= :keyDate")
    List<HrIt0021Family> findDependents(@Param("employeeId") Long employeeId,
                                         @Param("keyDate") LocalDate keyDate);

    /**
     * 查询子女 (个税专项附加扣除)
     */
    @Query("SELECT f FROM HrIt0021Family f WHERE f.employeeId = :employeeId " +
           "AND f.isChildForTax = true AND f.isDeleted = false " +
           "AND f.validFrom <= :keyDate AND f.validTo >= :keyDate")
    List<HrIt0021Family> findChildrenForTax(@Param("employeeId") Long employeeId,
                                             @Param("keyDate") LocalDate keyDate);

    /**
     * 查询老人 (个税专项附加扣除)
     */
    @Query("SELECT f FROM HrIt0021Family f WHERE f.employeeId = :employeeId " +
           "AND f.isElderForTax = true AND f.isDeleted = false " +
           "AND f.validFrom <= :keyDate AND f.validTo >= :keyDate")
    List<HrIt0021Family> findEldersForTax(@Param("employeeId") Long employeeId,
                                           @Param("keyDate") LocalDate keyDate);

    /**
     * 查询员工历史记录
     */
    @Query("SELECT f FROM HrIt0021Family f WHERE f.employeeId = :employeeId " +
           "AND f.isDeleted = false ORDER BY f.familyType, f.validFrom DESC")
    List<HrIt0021Family> findHistoryByEmployee(@Param("employeeId") Long employeeId);
}
