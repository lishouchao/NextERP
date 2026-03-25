package com.nexterp.business.controlling.domain.repository;

import com.nexterp.business.controlling.domain.model.CoActivityType;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 作业类型仓储接口
 *
 * @author NextERP
 */
@Repository
public interface CoActivityTypeRepository extends TenantAwareRepository<CoActivityType> {

    /**
     * 根据作业类型代码查询
     */
    Optional<CoActivityType> findByActivityTypeCodeAndTenantIdAndIsDeletedFalse(String activityTypeCode, Long tenantId);

    /**
     * 检查作业类型代码是否存在
     */
    boolean existsByActivityTypeCodeAndTenantIdAndIsDeletedFalse(String activityTypeCode, Long tenantId);

    /**
     * 按作业类别查询
     */
    List<CoActivityType> findByActivityCategoryAndTenantIdAndIsDeletedFalseOrderByActivityTypeCodeAsc(String activityCategory, Long tenantId);

    /**
     * 按成本控制范围查询
     */
    List<CoActivityType> findByControllingAreaAndTenantIdAndIsDeletedFalseOrderByActivityTypeCodeAsc(String controllingArea, Long tenantId);

    /**
     * 按成本要素查询
     */
    List<CoActivityType> findByCostElementIdAndTenantIdAndIsDeletedFalseOrderByActivityTypeCodeAsc(Long costElementId, Long tenantId);

    /**
     * 查询有效作业类型
     */
    List<CoActivityType> findByTenantIdAndIsDeletedFalseAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByActivityTypeCodeAsc(
            Long tenantId, LocalDate validFrom, LocalDate validTo);

    /**
     * 查询启用的作业类型
     */
    List<CoActivityType> findByTenantIdAndIsDeletedFalseAndStatusOrderByActivityTypeCodeAsc(Long tenantId, Integer status);
}
