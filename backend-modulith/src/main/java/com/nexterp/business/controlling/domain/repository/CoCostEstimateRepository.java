package com.nexterp.business.controlling.domain.repository;

import com.nexterp.business.controlling.domain.model.CoCostEstimate;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 成本估算仓储接口
 *
 * @author NextERP
 */
@Repository
public interface CoCostEstimateRepository extends TenantAwareRepository<CoCostEstimate> {

    /**
     * 根据估算号查询
     */
    Optional<CoCostEstimate> findByEstimateNumberAndTenantIdAndIsDeletedFalse(String estimateNumber, Long tenantId);

    /**
     * 按物料查询
     */
    List<CoCostEstimate> findByMaterialIdAndTenantIdAndIsDeletedFalseOrderByEstimateDateDesc(Long materialId, Long tenantId);

    /**
     * 按估算类型查询
     */
    List<CoCostEstimate> findByEstimateTypeAndTenantIdAndIsDeletedFalseOrderByEstimateDateDesc(String estimateType, Long tenantId);

    /**
     * 按估算状态查询
     */
    List<CoCostEstimate> findByEstimateStatusAndTenantIdAndIsDeletedFalseOrderByEstimateDateDesc(String estimateStatus, Long tenantId);

    /**
     * 按工厂查询
     */
    List<CoCostEstimate> findByPlantCodeAndTenantIdAndIsDeletedFalseOrderByEstimateDateDesc(String plantCode, Long tenantId);

    /**
     * 查询物料的有效估算
     */
    Optional<CoCostEstimate> findByMaterialIdAndEstimateTypeAndEstimateStatusAndTenantIdAndIsDeletedFalseAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            Long materialId, String estimateType, String estimateStatus, Long tenantId, LocalDate validFrom, LocalDate validTo);

    /**
     * 按版本查询
     */
    List<CoCostEstimate> findByEstimateVersionAndTenantIdAndIsDeletedFalseOrderByEstimateDateDesc(String version, Long tenantId);
}
