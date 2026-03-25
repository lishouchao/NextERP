package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt0592HousingFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0592 - 公积金信息 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt0592HousingFundRepository extends JpaRepository<HrIt0592HousingFund, Long>,
        JpaSpecificationExecutor<HrIt0592HousingFund> {

    /**
     * 根据员工ID查询
     */
    List<HrIt0592HousingFund> findByEmployeeIdAndIsDeletedFalse(Long employeeId);

    /**
     * 根据员工编号查询
     */
    List<HrIt0592HousingFund> findByEmployeeNoAndTenantIdAndIsDeletedFalse(
            String employeeNo, Long tenantId);

    /**
     * 根据员工和公积金类型查询
     */
    List<HrIt0592HousingFund> findByEmployeeIdAndFundTypeAndIsDeletedFalse(
            Long employeeId, String fundType);

    /**
     * 查询指定日期有效的记录
     */
    @Query("SELECT hf FROM HrIt0592HousingFund hf WHERE hf.employeeId = :employeeId " +
           "AND hf.fundType = :fundType AND hf.isDeleted = false " +
           "AND hf.validFrom <= :date AND (hf.validTo IS NULL OR hf.validTo >= :date)")
    Optional<HrIt0592HousingFund> findValidOnDateByType(@Param("employeeId") Long employeeId,
                                                         @Param("fundType") String fundType,
                                                         @Param("date") LocalDate date);

    /**
     * 查询员工所有有效记录
     */
    @Query("SELECT hf FROM HrIt0592HousingFund hf WHERE hf.employeeId = :employeeId " +
           "AND hf.isDeleted = false " +
           "AND hf.validFrom <= :date AND (hf.validTo IS NULL OR hf.validTo >= :date)")
    List<HrIt0592HousingFund> findAllValidOnDate(@Param("employeeId") Long employeeId,
                                                  @Param("date") LocalDate date);

    /**
     * 根据缴存状态查询
     */
    List<HrIt0592HousingFund> findByFundStatusAndTenantIdAndIsDeletedFalse(
            String fundStatus, Long tenantId);

    /**
     * 根据公积金中心查询
     */
    List<HrIt0592HousingFund> findByFundCenterCodeAndTenantIdAndIsDeletedFalse(
            String fundCenterCode, Long tenantId);

    /**
     * 根据公积金账号查询
     */
    Optional<HrIt0592HousingFund> findByFundAccountAndTenantIdAndIsDeletedFalse(
            String fundAccount, Long tenantId);

    /**
     * 查询员工历史记录
     */
    @Query("SELECT hf FROM HrIt0592HousingFund hf WHERE hf.employeeId = :employeeId " +
           "AND hf.isDeleted = false ORDER BY hf.validFrom DESC")
    List<HrIt0592HousingFund> findHistoryByEmployee(@Param("employeeId") Long employeeId);
}
