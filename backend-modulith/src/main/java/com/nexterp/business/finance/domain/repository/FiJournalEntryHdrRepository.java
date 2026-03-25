package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FiJournalEntryHdr;
import com.nexterp.business.finance.domain.model.JournalEntryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 凭证头 Repository
 *
 * @author NextERP
 */
@Repository
public interface FiJournalEntryHdrRepository extends JpaRepository<FiJournalEntryHdr, JournalEntryId>,
        JpaSpecificationExecutor<FiJournalEntryHdr> {

    /**
     * 根据年度和ID查找
     */
    Optional<FiJournalEntryHdr> findByFiscalYearAndIdAndIsDeletedFalse(Integer fiscalYear, Long id);

    /**
     * 根据凭证号查找
     */
    Optional<FiJournalEntryHdr> findByFiscalYearAndDocumentNumberAndTenantIdAndIsDeletedFalse(
            Integer fiscalYear, String documentNumber, Long tenantId);

    /**
     * 检查凭证号是否存在
     */
    boolean existsByFiscalYearAndDocumentNumberAndTenantIdAndIsDeletedFalse(
            Integer fiscalYear, String documentNumber, Long tenantId);

    /**
     * 按年度和公司查询
     */
    List<FiJournalEntryHdr> findByFiscalYearAndCompanyCodeAndTenantIdAndIsDeletedFalseOrderByDocumentDateDesc(
            Integer fiscalYear, String companyCode, Long tenantId);

    /**
     * 按过账日期范围查询
     */
    List<FiJournalEntryHdr> findByPostingDateBetweenAndTenantIdAndIsDeletedFalseOrderByPostingDateDesc(
            LocalDate startDate, LocalDate endDate, Long tenantId);

    /**
     * 按凭证状态查询
     */
    List<FiJournalEntryHdr> findByDocStatusAndTenantIdAndIsDeletedFalseOrderByPostingDateDesc(
            String docStatus, Long tenantId);

    /**
     * 查询已过账未冲销的凭证
     */
    List<FiJournalEntryHdr> findByIsPostedTrueAndIsReversedFalseAndTenantIdAndIsDeletedFalse(
            Long tenantId);

    /**
     * 获取年度最大凭证号
     */
    @Query("SELECT MAX(h.documentNumber) FROM FiJournalEntryHdr h " +
           "WHERE h.fiscalYear = :fiscalYear AND h.tenantId = :tenantId AND h.isDeleted = false")
    String findMaxDocumentNumber(@Param("fiscalYear") Integer fiscalYear, @Param("tenantId") Long tenantId);
}
