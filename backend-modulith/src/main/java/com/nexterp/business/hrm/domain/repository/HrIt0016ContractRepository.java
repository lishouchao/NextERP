package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt0016Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0016 - 合同 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt0016ContractRepository extends JpaRepository<HrIt0016Contract, Long>,
        JpaSpecificationExecutor<HrIt0016Contract> {

    /**
     * 根据员工ID查询
     */
    List<HrIt0016Contract> findByEmployeeIdAndIsDeletedFalse(Long employeeId);

    /**
     * 根据员工编号查询
     */
    List<HrIt0016Contract> findByEmployeeNoAndTenantIdAndIsDeletedFalse(String employeeNo, Long tenantId);

    /**
     * 根据合同编号查询
     */
    Optional<HrIt0016Contract> findByContractNoAndTenantIdAndIsDeletedFalse(
            String contractNo, Long tenantId);

    /**
     * 查询当前有效合同
     */
    @Query("SELECT c FROM HrIt0016Contract c WHERE c.employeeId = :employeeId " +
           "AND c.contractStatus = '1' AND c.isDeleted = false " +
           "AND c.validFrom <= :keyDate AND c.validTo >= :keyDate")
    Optional<HrIt0016Contract> findCurrentContract(@Param("employeeId") Long employeeId,
                                                    @Param("keyDate") LocalDate keyDate);

    /**
     * 查询指定日期有效的记录
     */
    @Query("SELECT c FROM HrIt0016Contract c WHERE c.employeeId = :employeeId " +
           "AND c.isDeleted = false " +
           "AND c.validFrom <= :keyDate AND c.validTo >= :keyDate")
    List<HrIt0016Contract> findValidOnDate(@Param("employeeId") Long employeeId,
                                            @Param("keyDate") LocalDate keyDate);

    /**
     * 查询即将到期的合同
     */
    @Query("SELECT c FROM HrIt0016Contract c WHERE c.tenantId = :tenantId " +
           "AND c.contractStatus = '1' AND c.isDeleted = false " +
           "AND c.endDate IS NOT NULL " +
           "AND c.endDate BETWEEN :startDate AND :endDate")
    List<HrIt0016Contract> findExpiringContracts(@Param("tenantId") Long tenantId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * 查询试用期即将结束的员工
     */
    @Query("SELECT c FROM HrIt0016Contract c WHERE c.tenantId = :tenantId " +
           "AND c.contractStatus = '1' AND c.isDeleted = false " +
           "AND c.probationEndDate IS NOT NULL " +
           "AND c.probationEndDate BETWEEN :startDate AND :endDate")
    List<HrIt0016Contract> findEndingProbation(@Param("tenantId") Long tenantId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * 根据合同类型查询
     */
    List<HrIt0016Contract> findByContractTypeAndTenantIdAndIsDeletedFalse(
            String contractType, Long tenantId);

    /**
     * 查询员工历史记录
     */
    @Query("SELECT c FROM HrIt0016Contract c WHERE c.employeeId = :employeeId " +
           "AND c.isDeleted = false ORDER BY c.validFrom DESC")
    List<HrIt0016Contract> findHistoryByEmployee(@Param("employeeId") Long employeeId);
}
