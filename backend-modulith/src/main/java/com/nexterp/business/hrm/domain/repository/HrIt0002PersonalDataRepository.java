package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt0002PersonalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0002 - 个人数据 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt0002PersonalDataRepository extends JpaRepository<HrIt0002PersonalData, Long>,
        JpaSpecificationExecutor<HrIt0002PersonalData> {

    /**
     * 根据员工ID查询
     */
    List<HrIt0002PersonalData> findByEmployeeIdAndIsDeletedFalse(Long employeeId);

    /**
     * 根据员工编号查询
     */
    List<HrIt0002PersonalData> findByEmployeeNoAndTenantIdAndIsDeletedFalse(String employeeNo, Long tenantId);

    /**
     * 查询指定日期有效的记录
     */
    @Query("SELECT p FROM HrIt0002PersonalData p WHERE p.employeeId = :employeeId " +
           "AND p.isDeleted = false " +
           "AND p.validFrom <= :keyDate AND p.validTo >= :keyDate")
    Optional<HrIt0002PersonalData> findValidOnDate(@Param("employeeId") Long employeeId,
                                                    @Param("keyDate") LocalDate keyDate);

    /**
     * 根据证件号码查询
     */
    Optional<HrIt0002PersonalData> findByIdNumberAndTenantIdAndIsDeletedFalse(
            String idNumber, Long tenantId);

    /**
     * 查询员工历史记录
     */
    @Query("SELECT p FROM HrIt0002PersonalData p WHERE p.employeeId = :employeeId " +
           "AND p.isDeleted = false ORDER BY p.validFrom DESC")
    List<HrIt0002PersonalData> findHistoryByEmployee(@Param("employeeId") Long employeeId);
}
