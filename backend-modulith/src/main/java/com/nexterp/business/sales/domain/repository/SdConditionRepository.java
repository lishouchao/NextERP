package com.nexterp.business.sales.domain.repository;

import com.nexterp.business.sales.domain.model.SdCondition;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 定价条件记录仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SdConditionRepository extends TenantAwareRepository<SdCondition> {

    /**
     * 根据条件类型和租户ID查询有效条件
     *
     * @param conditionType 条件类型
     * @param tenantId      租户ID
     * @return 条件列表
     */
    List<SdCondition> findByConditionTypeAndTenantIdAndIsDeletedFalse(String conditionType, Long tenantId);

    /**
     * 根据条件类型、客户、物料和有效日期范围查询条件
     *
     * @param conditionType 条件类型
     * @param customerId    客户ID
     * @param materialId    物料ID
     * @param validDate     有效日期
     * @param tenantId      租户ID
     * @return 条件列表
     */
    @Query("SELECT c FROM SdCondition c WHERE c.tenantId = :tenantId AND c.isDeleted = false " +
           "AND c.conditionType = :conditionType " +
           "AND (c.customerId = :customerId OR c.customerId IS NULL) " +
           "AND (c.materialId = :materialId OR c.materialId IS NULL) " +
           "AND c.validFrom <= :validDate AND c.validTo >= :validDate " +
           "ORDER BY c.customerId DESC NULLS LAST, c.materialId DESC NULLS LAST")
    List<SdCondition> findValidConditions(@Param("conditionType") String conditionType,
                                          @Param("customerId") Long customerId,
                                          @Param("materialId") Long materialId,
                                          @Param("validDate") LocalDate validDate,
                                          @Param("tenantId") Long tenantId);
}
