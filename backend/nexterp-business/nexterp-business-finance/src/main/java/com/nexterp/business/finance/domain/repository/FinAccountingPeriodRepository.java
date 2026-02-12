package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FinAccountingPeriod;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 会计期间仓储接口
 *
 * @author NextERP
 */
@Repository
public interface FinAccountingPeriodRepository extends TenantAwareRepository<FinAccountingPeriod> {

    /**
     * 根据会计期间查询
     *
     * @param accountingPeriod 会计期间
     * @param tenantId         租户ID
     * @return 期间
     */
    Optional<FinAccountingPeriod> findByAccountingPeriodAndTenantIdAndIsDeletedFalse(String accountingPeriod, Long tenantId);

    /**
     * 查询指定年度的所有期间
     *
     * @param accountingYear 会计年度
     * @param tenantId       租户ID
     * @return 期间列表
     */
    List<FinAccountingPeriod> findByAccountingYearAndTenantIdAndIsDeletedFalseOrderByAccountingPeriodAsc(
            Integer accountingYear, Long tenantId);

    /**
     * 查询当前开启的期间
     *
     * @param tenantId 租户ID
     * @return 期间
     */
    @Query("SELECT p FROM FinAccountingPeriod p WHERE p.tenantId = :tenantId AND p.isDeleted = false AND p.periodStatus = 1 ORDER BY p.accountingPeriod DESC")
    Optional<FinAccountingPeriod> findCurrentPeriod(@Param("tenantId") Long tenantId);

    /**
     * 查询指定日期所在的期间
     *
     * @param date     日期
     * @param tenantId 租户ID
     * @return 期间
     */
    @Query("SELECT p FROM FinAccountingPeriod p WHERE p.tenantId = :tenantId AND p.isDeleted = false AND :date >= p.periodStartDate AND :date <= p.periodEndDate")
    Optional<FinAccountingPeriod> findByDate(@Param("date") LocalDate date, @Param("tenantId") Long tenantId);

    /**
     * 查询已开启的期间列表
     *
     * @param tenantId 租户ID
     * @return 期间列表
     */
    @Query("SELECT p FROM FinAccountingPeriod p WHERE p.tenantId = :tenantId AND p.isDeleted = false AND p.periodStatus >= 1 ORDER BY p.accountingPeriod ASC")
    List<FinAccountingPeriod> findOpenedPeriods(@Param("tenantId") Long tenantId);

    /**
     * 查询未结账的期间
     *
     * @param tenantId 租户ID
     * @return 期间列表
     */
    @Query("SELECT p FROM FinAccountingPeriod p WHERE p.tenantId = :tenantId AND p.isDeleted = false AND p.periodStatus = 1 ORDER BY p.accountingPeriod ASC")
    List<FinAccountingPeriod> findUnclosedPeriods(@Param("tenantId") Long tenantId);

    /**
     * 检查期间是否存在
     *
     * @param accountingPeriod 会计期间
     * @param tenantId         租户ID
     * @return 是否存在
     */
    boolean existsByAccountingPeriodAndTenantIdAndIsDeletedFalse(String accountingPeriod, Long tenantId);
}
