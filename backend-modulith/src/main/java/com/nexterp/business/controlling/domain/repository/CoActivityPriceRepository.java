package com.nexterp.business.controlling.domain.repository;

import com.nexterp.business.controlling.domain.model.CoActivityPrice;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 作业价格仓储接口
 *
 * @author NextERP
 */
@Repository
public interface CoActivityPriceRepository extends TenantAwareRepository<CoActivityPrice> {

    /**
     * 按成本中心和作业类型查询
     */
    List<CoActivityPrice> findByCostCenterIdAndActivityTypeIdAndTenantIdAndIsDeletedFalse(
            Long costCenterId, Long activityTypeId, Long tenantId);

    /**
     * 按价格类型查询
     */
    List<CoActivityPrice> findByPriceTypeAndTenantIdAndIsDeletedFalse(Long tenantId, String priceType);

    /**
     * 查询有效价格
     */
    Optional<CoActivityPrice> findByCostCenterIdAndActivityTypeIdAndPriceTypeAndTenantIdAndIsDeletedFalseAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            Long costCenterId, Long activityTypeId, String priceType, Long tenantId, LocalDate validFrom, LocalDate validTo);

    /**
     * 按版本查询
     */
    List<CoActivityPrice> findByVersionAndTenantIdAndIsDeletedFalse(String version, Long tenantId);
}
