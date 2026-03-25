package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FiJournalEntryItm;
import com.nexterp.business.finance.domain.model.JournalEntryItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 凭证项 Repository
 *
 * @author NextERP
 */
@Repository
public interface FiJournalEntryItmRepository extends JpaRepository<FiJournalEntryItm, JournalEntryItemId>,
        JpaSpecificationExecutor<FiJournalEntryItm> {

    /**
     * 根据凭证头ID查询所有分录
     */
    List<FiJournalEntryItm> findByFiscalYearAndHeaderIdAndIsDeletedFalseOrderByLineItemAsc(
            Integer fiscalYear, Long headerId);

    /**
     * 根据科目ID查询
     */
    List<FiJournalEntryItm> findByAccountIdAndTenantIdAndIsDeletedFalse(Long accountId, Long tenantId);

    /**
     * 根据业务伙伴查询
     */
    List<FiJournalEntryItm> findByPartnerIdAndTenantIdAndIsDeletedFalse(Long partnerId, Long tenantId);

    /**
     * 查询未清项
     */
    List<FiJournalEntryItm> findByPartnerIdAndClearingDateIsNullAndTenantIdAndIsDeletedFalse(
            Long partnerId, Long tenantId);

    /**
     * 计算凭证借贷合计
     */
    @Query("SELECT SUM(CASE WHEN i.debitCredit = 'D' THEN i.amount ELSE 0 END), " +
           "       SUM(CASE WHEN i.debitCredit = 'C' THEN i.amount ELSE 0 END) " +
           "FROM FiJournalEntryItm i " +
           "WHERE i.fiscalYear = :fiscalYear AND i.headerId = :headerId AND i.isDeleted = false")
    List<Object[]> sumDebitCreditByHeader(@Param("fiscalYear") Integer fiscalYear,
                                          @Param("headerId") Long headerId);

    /**
     * 删除凭证的所有分录 (软删除)
     */
    @Query("UPDATE FiJournalEntryItm i SET i.isDeleted = true " +
           "WHERE i.fiscalYear = :fiscalYear AND i.headerId = :headerId")
    void softDeleteByHeader(@Param("fiscalYear") Integer fiscalYear, @Param("headerId") Long headerId);
}
