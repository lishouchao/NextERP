package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrWageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工资类型 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrWageTypeRepository extends JpaRepository<HrWageType, Long>,
        JpaSpecificationExecutor<HrWageType> {

    /**
     * 根据工资类型代码查询
     */
    Optional<HrWageType> findByWageTypeCodeAndTenantIdAndIsDeletedFalse(
            String wageTypeCode, Long tenantId);

    /**
     * 根据工资分类查询
     */
    List<HrWageType> findByWageCategoryAndTenantIdAndIsDeletedFalse(
            String wageCategory, Long tenantId);

    /**
     * 查询启用的工资类型
     */
    List<HrWageType> findByTenantIdAndStatusAndIsDeletedFalseOrderBySortOrderAsc(
            Long tenantId, Integer status);

    /**
     * 查询所有工资类型
     */
    List<HrWageType> findByTenantIdAndIsDeletedFalseOrderBySortOrderAsc(Long tenantId);

    /**
     * 查询应税工资类型
     */
    List<HrWageType> findByIsTaxableAndTenantIdAndIsDeletedFalse(
            Boolean isTaxable, Long tenantId);

    /**
     * 查询计入社保基数的工资类型
     */
    List<HrWageType> findByIsSocialBaseAndTenantIdAndIsDeletedFalse(
            Boolean isSocialBase, Long tenantId);

    /**
     * 查询计入公积金基数的工资类型
     */
    List<HrWageType> findByIsFundBaseAndTenantIdAndIsDeletedFalse(
            Boolean isFundBase, Long tenantId);

    /**
     * 根据借贷方向查询
     */
    List<HrWageType> findByDcIndicatorAndTenantIdAndIsDeletedFalse(
            String dcIndicator, Long tenantId);

    /**
     * 查询加项工资类型
     */
    @Query("SELECT wt FROM HrWageType wt WHERE wt.tenantId = :tenantId " +
           "AND wt.dcIndicator = 'D' AND wt.status = 1 AND wt.isDeleted = false " +
           "ORDER BY wt.sortOrder")
    List<HrWageType> findAdditions(@Param("tenantId") Long tenantId);

    /**
     * 查询减项工资类型
     */
    @Query("SELECT wt FROM HrWageType wt WHERE wt.tenantId = :tenantId " +
           "AND wt.dcIndicator = 'C' AND wt.status = 1 AND wt.isDeleted = false " +
           "ORDER BY wt.sortOrder")
    List<HrWageType> findDeductions(@Param("tenantId") Long tenantId);

    /**
     * 模糊搜索
     */
    @Query("SELECT wt FROM HrWageType wt WHERE wt.tenantId = :tenantId " +
           "AND wt.isDeleted = false " +
           "AND (wt.wageTypeCode LIKE %:keyword% OR wt.wageTypeName LIKE %:keyword%)")
    org.springframework.data.domain.Page<HrWageType> findByTenantIdAndKeyword(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable);
}
