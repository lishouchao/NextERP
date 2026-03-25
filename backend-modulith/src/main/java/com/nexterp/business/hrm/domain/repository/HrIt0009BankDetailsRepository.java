package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt0009BankDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0009 - 银行信息 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt0009BankDetailsRepository extends JpaRepository<HrIt0009BankDetails, Long>,
        JpaSpecificationExecutor<HrIt0009BankDetails> {

    /**
     * 根据员工ID查询
     */
    List<HrIt0009BankDetails> findByEmployeeIdAndIsDeletedFalse(Long employeeId);

    /**
     * 根据员工编号查询
     */
    List<HrIt0009BankDetails> findByEmployeeNoAndTenantIdAndIsDeletedFalse(String employeeNo, Long tenantId);

    /**
     * 根据员工和银行类型查询
     */
    List<HrIt0009BankDetails> findByEmployeeIdAndBankTypeAndIsDeletedFalse(
            Long employeeId, String bankType);

    /**
     * 查询主账户
     */
    @Query("SELECT b FROM HrIt0009BankDetails b WHERE b.employeeId = :employeeId " +
           "AND b.isPrimary = true AND b.isDeleted = false " +
           "AND b.validFrom <= :keyDate AND b.validTo >= :keyDate")
    Optional<HrIt0009BankDetails> findPrimaryAccount(@Param("employeeId") Long employeeId,
                                                      @Param("keyDate") LocalDate keyDate);

    /**
     * 查询指定日期有效的记录
     */
    @Query("SELECT b FROM HrIt0009BankDetails b WHERE b.employeeId = :employeeId " +
           "AND b.bankType = :bankType AND b.isDeleted = false " +
           "AND b.validFrom <= :keyDate AND b.validTo >= :keyDate")
    Optional<HrIt0009BankDetails> findValidOnDateByType(@Param("employeeId") Long employeeId,
                                                         @Param("bankType") String bankType,
                                                         @Param("keyDate") LocalDate keyDate);

    /**
     * 查询员工所有有效银行账户
     */
    @Query("SELECT b FROM HrIt0009BankDetails b WHERE b.employeeId = :employeeId " +
           "AND b.isDeleted = false " +
           "AND b.validFrom <= :keyDate AND b.validTo >= :keyDate")
    List<HrIt0009BankDetails> findAllValidOnDate(@Param("employeeId") Long employeeId,
                                                  @Param("keyDate") LocalDate keyDate);

    /**
     * 根据银行账号查询
     */
    Optional<HrIt0009BankDetails> findByBankAccountAndTenantIdAndIsDeletedFalse(
            String bankAccount, Long tenantId);

    /**
     * 查询员工历史记录
     */
    @Query("SELECT b FROM HrIt0009BankDetails b WHERE b.employeeId = :employeeId " +
           "AND b.isDeleted = false ORDER BY b.bankType, b.validFrom DESC")
    List<HrIt0009BankDetails> findHistoryByEmployee(@Param("employeeId") Long employeeId);
}
