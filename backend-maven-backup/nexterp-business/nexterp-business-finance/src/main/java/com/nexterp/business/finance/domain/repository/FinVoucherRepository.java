package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FinVoucher;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 财务凭证仓储接口
 *
 * @author NextERP
 */
@Repository
public interface FinVoucherRepository extends TenantAwareRepository<FinVoucher> {

    /**
     * 根据凭证号查询
     *
     * @param voucherNo 凭证号
     * @param tenantId  租户ID
     * @return 凭证
     */
    Optional<FinVoucher> findByVoucherNoAndTenantIdAndIsDeletedFalse(String voucherNo, Long tenantId);

    /**
     * 检查凭证号是否存在
     *
     * @param voucherNo 凭证号
     * @param tenantId  租户ID
     * @return 是否存在
     */
    boolean existsByVoucherNoAndTenantIdAndIsDeletedFalse(String voucherNo, Long tenantId);

    /**
     * 查询指定会计期间的凭证
     *
     * @param accountingPeriod 会计期间
     * @param tenantId         租户ID
     * @return 凭证列表
     */
    List<FinVoucher> findByAccountingPeriodAndTenantIdAndIsDeletedFalseOrderByVoucherDateAscVoucherNoAsc(
            String accountingPeriod, Long tenantId);

    /**
     * 查询指定状态的凭证
     *
     * @param status   状态
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 凭证分页
     */
    Page<FinVoucher> findByVoucherStatusAndTenantIdAndIsDeletedFalse(Integer status, Long tenantId, Pageable pageable);

    /**
     * 查询指定日期范围的凭证
     *
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @param tenantId  租户ID
     * @return 凭证列表
     */
    List<FinVoucher> findByVoucherDateBetweenAndTenantIdAndIsDeletedFalseOrderByVoucherDateAscVoucherNoAsc(
            LocalDate startDate, LocalDate endDate, Long tenantId);

    /**
     * 查询待审核的凭证
     *
     * @param tenantId 租户ID
     * @return 凭证列表
     */
    @Query("SELECT v FROM FinVoucher v WHERE v.tenantId = :tenantId AND v.isDeleted = false AND v.voucherStatus = 1 ORDER BY v.voucherDate ASC, v.voucherNo ASC")
    List<FinVoucher> findPendingApprovalVouchers(@Param("tenantId") Long tenantId);

    /**
     * 查询已审核未记账的凭证
     *
     * @param tenantId 租户ID
     * @return 凭证列表
     */
    @Query("SELECT v FROM FinVoucher v WHERE v.tenantId = :tenantId AND v.isDeleted = false AND v.voucherStatus = 2 ORDER BY v.voucherDate ASC, v.voucherNo ASC")
    List<FinVoucher> findApprovedUnpostedVouchers(@Param("tenantId") Long tenantId);

    /**
     * 查询已记账的凭证
     *
     * @param tenantId 租户ID
     * @return 凭证列表
     */
    @Query("SELECT v FROM FinVoucher v WHERE v.tenantId = :tenantId AND v.isDeleted = false AND v.voucherStatus = 3 ORDER BY v.voucherDate ASC, v.voucherNo ASC")
    List<FinVoucher> findPostedVouchers(@Param("tenantId") Long tenantId);

    /**
     * 统计指定期间的凭证数量
     *
     * @param accountingPeriod 会计期间
     * @param tenantId         租户ID
     * @return 数量
     */
    long countByAccountingPeriodAndTenantIdAndIsDeletedFalse(String accountingPeriod, Long tenantId);

    /**
     * 查询下一凭证号
     *
     * @param accountingPeriod 会计期间
     * @param tenantId         租户ID
     * @return 下一凭证号
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(v.voucherNo, -4) AS INTEGER)), 0) + 1 FROM FinVoucher v WHERE v.accountingPeriod = :accountingPeriod AND v.tenantId = :tenantId AND v.isDeleted = false")
    Integer findNextVoucherNo(@Param("accountingPeriod") String accountingPeriod, @Param("tenantId") Long tenantId);
}
