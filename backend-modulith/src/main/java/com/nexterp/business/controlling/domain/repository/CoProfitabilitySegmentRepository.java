package com.nexterp.business.controlling.domain.repository;

import com.nexterp.business.controlling.domain.model.CoProfitabilitySegment;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 盈利段仓储接口
 *
 * @author NextERP
 */
@Repository
public interface CoProfitabilitySegmentRepository extends TenantAwareRepository<CoProfitabilitySegment> {

    /**
     * 按期间日期查询
     */
    List<CoProfitabilitySegment> findByPeriodDateAndTenantIdAndIsDeletedFalse(LocalDate periodDate, Long tenantId);

    /**
     * 按会计年度查询
     */
    List<CoProfitabilitySegment> findByFiscalYearAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(String fiscalYear, Long tenantId);

    /**
     * 按会计年度和期间查询
     */
    List<CoProfitabilitySegment> findByFiscalYearAndFiscalPeriodAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(
            String fiscalYear, String fiscalPeriod, Long tenantId);

    /**
     * 按利润中心查询
     */
    List<CoProfitabilitySegment> findByProfitCenterAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(String profitCenter, Long tenantId);

    /**
     * 按客户查询
     */
    List<CoProfitabilitySegment> findByCustomerIdAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(Long customerId, Long tenantId);

    /**
     * 按物料查询
     */
    List<CoProfitabilitySegment> findByMaterialIdAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(Long materialId, Long tenantId);

    /**
     * 按销售组织查询
     */
    List<CoProfitabilitySegment> findBySalesOrgAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(String salesOrg, Long tenantId);

    /**
     * 按日期范围查询
     */
    List<CoProfitabilitySegment> findByPeriodDateBetweenAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(
            LocalDate startDate, LocalDate endDate, Long tenantId);
}
