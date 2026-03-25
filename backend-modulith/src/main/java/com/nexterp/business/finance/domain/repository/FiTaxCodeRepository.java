package com.nexterp.business.finance.domain.repository;

import com.nexterp.business.finance.domain.model.FiTaxCode;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 税码 Repository
 *
 * @author NextERP
 */
@Repository
public interface FiTaxCodeRepository extends TenantAwareRepository<FiTaxCode> {

    /**
     * 根据税码查找
     */
    Optional<FiTaxCode> findByTaxCodeAndTenantIdAndIsDeletedFalse(String taxCode, Long tenantId);

    /**
     * 检查税码是否存在
     */
    boolean existsByTaxCodeAndTenantIdAndIsDeletedFalse(String taxCode, Long tenantId);

    /**
     * 根据税类型查询
     */
    List<FiTaxCode> findByTaxTypeAndTenantIdAndIsDeletedFalseOrderBySortOrder(String taxType, Long tenantId);

    /**
     * 根据国家代码查询
     */
    List<FiTaxCode> findByCountryCodeAndTenantIdAndIsDeletedFalseOrderBySortOrder(String countryCode, Long tenantId);

    /**
     * 查询所有启用的税码
     */
    List<FiTaxCode> findByStatusAndTenantIdAndIsDeletedFalseOrderBySortOrder(Integer status, Long tenantId);

    /**
     * 查询当前有效的税码
     */
    @Query("SELECT t FROM FiTaxCode t WHERE t.tenantId = :tenantId " +
           "AND t.isDeleted = false AND t.status = 1 " +
           "AND (t.validFrom IS NULL OR t.validFrom <= :date) " +
           "AND (t.validTo IS NULL OR t.validTo >= :date) " +
           "ORDER BY t.sortOrder")
    List<FiTaxCode> findValidTaxCodesByTenantIdAndDate(
            @Param("tenantId") Long tenantId,
            @Param("date") LocalDate date);

    /**
     * 根据税率查询
     */
    List<FiTaxCode> findByTaxRateAndTenantIdAndIsDeletedFalseOrderByTaxCode(
            java.math.BigDecimal taxRate, Long tenantId);
}
