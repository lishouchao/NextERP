package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrLeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 假期类型 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrLeaveTypeRepository extends JpaRepository<HrLeaveType, Long>,
        JpaSpecificationExecutor<HrLeaveType> {

    /**
     * 根据假期类型编码查询
     */
    Optional<HrLeaveType> findByLeaveTypeCodeAndTenantIdAndIsDeletedFalse(
            String leaveTypeCode, Long tenantId);

    /**
     * 根据假期分类查询
     */
    List<HrLeaveType> findByLeaveCategoryAndTenantIdAndIsDeletedFalse(
            String leaveCategory, Long tenantId);

    /**
     * 查询启用的假期类型
     */
    List<HrLeaveType> findByTenantIdAndStatusAndIsDeletedFalseOrderBySortOrderAsc(
            Long tenantId, Integer status);

    /**
     * 查询所有假期类型
     */
    List<HrLeaveType> findByTenantIdAndIsDeletedFalseOrderBySortOrderAsc(Long tenantId);

    /**
     * 查询带薪假期
     */
    List<HrLeaveType> findByIsPaidAndTenantIdAndIsDeletedFalse(
            Boolean isPaid, Long tenantId);

    /**
     * 查询需要审批的假期
     */
    List<HrLeaveType> findByRequireApprovalAndTenantIdAndIsDeletedFalse(
            Boolean requireApproval, Long tenantId);

    /**
     * 根据性别查询适用假期
     */
    @Query("SELECT lt FROM HrLeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND lt.status = 1 AND lt.isDeleted = false " +
           "AND (lt.applyGender = 'A' OR lt.applyGender = :gender)")
    List<HrLeaveType> findByGender(@Param("tenantId") Long tenantId,
                                    @Param("gender") String gender);

    /**
     * 模糊搜索
     */
    @Query("SELECT lt FROM HrLeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND lt.isDeleted = false " +
           "AND (lt.leaveTypeCode LIKE %:keyword% OR lt.leaveTypeName LIKE %:keyword%)")
    org.springframework.data.domain.Page<HrLeaveType> findByTenantIdAndKeyword(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable);
}
