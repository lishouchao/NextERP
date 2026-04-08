package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FiAccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 科目余额 Repository
 *
 * @author NextERP
 */
@Repository
public interface FiAccountBalanceRepository extends JpaRepository<FiAccountBalance, Long> {

    /**
     * 根据公司和科目查找
     */
    Optional<FiAccountBalance> findByCompanyIdAndAccountIdAndFiscalYearAndCurrencyCode(
            Long companyId, Long accountId, Integer fiscalYear, String currencyCode);

    /**
     * 根据公司查询所有余额
     */
    List<FiAccountBalance> findByCompanyIdAndFiscalYear(Long companyId, Integer fiscalYear);

    /**
     * 根据科目查询余额
     */
    List<FiAccountBalance> findByAccountIdAndTenantId(Long accountId, Long tenantId);

    /**
     * 查询有发生额的科目
     */
    @Query("SELECT b FROM FiAccountBalance b WHERE b.tenantId = :tenantId " +
           "AND b.fiscalYear = :fiscalYear " +
           "AND (b.yearDebit > 0 OR b.yearCredit > 0)")
    List<FiAccountBalance> findWithActivity(@Param("tenantId") Long tenantId,
                                            @Param("fiscalYear") Integer fiscalYear);

    /**
     * 查询科目期末余额
     */
    @Query("SELECT b.endingBalance FROM FiAccountBalance b " +
           "WHERE b.companyId = :companyId AND b.accountId = :accountId " +
           "AND b.fiscalYear = :fiscalYear")
    Optional<BigDecimal> findEndingBalance(@Param("companyId") Long companyId,
                                           @Param("accountId") Long accountId,
                                           @Param("fiscalYear") Integer fiscalYear);
}
