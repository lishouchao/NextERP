package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt0591SocialInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0591 - 社保信息 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt0591SocialInsuranceRepository extends JpaRepository<HrIt0591SocialInsurance, Long>,
        JpaSpecificationExecutor<HrIt0591SocialInsurance> {

    /**
     * 根据员工ID查询
     */
    List<HrIt0591SocialInsurance> findByEmployeeIdAndIsDeletedFalse(Long employeeId);

    /**
     * 根据员工编号查询
     */
    List<HrIt0591SocialInsurance> findByEmployeeNoAndTenantIdAndIsDeletedFalse(
            String employeeNo, Long tenantId);

    /**
     * 查询指定日期有效的记录
     */
    @Query("SELECT si FROM HrIt0591SocialInsurance si WHERE si.employeeId = :employeeId " +
           "AND si.isDeleted = false " +
           "AND si.validFrom <= :date AND (si.validTo IS NULL OR si.validTo >= :date)")
    Optional<HrIt0591SocialInsurance> findValidOnDate(@Param("employeeId") Long employeeId,
                                                       @Param("date") LocalDate date);

    /**
     * 根据城市查询
     */
    List<HrIt0591SocialInsurance> findByCityCodeAndTenantIdAndIsDeletedFalse(
            String cityCode, Long tenantId);

    /**
     * 根据参保状态查询
     */
    List<HrIt0591SocialInsurance> findByInsuranceStatusAndTenantIdAndIsDeletedFalse(
            String insuranceStatus, Long tenantId);

    /**
     * 查询员工历史记录
     */
    @Query("SELECT si FROM HrIt0591SocialInsurance si WHERE si.employeeId = :employeeId " +
           "AND si.isDeleted = false ORDER BY si.validFrom DESC")
    List<HrIt0591SocialInsurance> findHistoryByEmployee(@Param("employeeId") Long employeeId);

    /**
     * 根据社保配置查询
     */
    List<HrIt0591SocialInsurance> findByConfigIdAndTenantIdAndIsDeletedFalse(
            Long configId, Long tenantId);
}
