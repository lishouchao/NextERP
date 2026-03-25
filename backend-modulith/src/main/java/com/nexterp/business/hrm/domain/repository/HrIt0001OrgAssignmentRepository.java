package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt0001OrgAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0001 - 组织分配 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt0001OrgAssignmentRepository extends JpaRepository<HrIt0001OrgAssignment, Long>,
        JpaSpecificationExecutor<HrIt0001OrgAssignment> {

    /**
     * 根据员工ID查询
     */
    List<HrIt0001OrgAssignment> findByEmployeeIdAndIsDeletedFalse(Long employeeId);

    /**
     * 根据员工编号查询
     */
    List<HrIt0001OrgAssignment> findByEmployeeNoAndTenantIdAndIsDeletedFalse(String employeeNo, Long tenantId);

    /**
     * 查询指定日期有效的记录
     */
    @Query("SELECT a FROM HrIt0001OrgAssignment a WHERE a.employeeId = :employeeId " +
           "AND a.isDeleted = false " +
           "AND a.validFrom <= :keyDate AND a.validTo >= :keyDate")
    Optional<HrIt0001OrgAssignment> findValidOnDate(@Param("employeeId") Long employeeId,
                                                     @Param("keyDate") LocalDate keyDate);

    /**
     * 查询员工的主分配
     */
    @Query("SELECT a FROM HrIt0001OrgAssignment a WHERE a.employeeId = :employeeId " +
           "AND a.isPrimary = true AND a.isDeleted = false " +
           "AND a.validFrom <= :keyDate AND a.validTo >= :keyDate")
    Optional<HrIt0001OrgAssignment> findPrimaryAssignment(@Param("employeeId") Long employeeId,
                                                           @Param("keyDate") LocalDate keyDate);

    /**
     * 根据组织查询员工分配
     */
    @Query("SELECT a FROM HrIt0001OrgAssignment a WHERE a.orgPk = :orgPk " +
           "AND a.tenantId = :tenantId AND a.isDeleted = false " +
           "AND a.validFrom <= :keyDate AND a.validTo >= :keyDate")
    List<HrIt0001OrgAssignment> findByOrg(@Param("orgPk") Long orgPk,
                                           @Param("tenantId") Long tenantId,
                                           @Param("keyDate") LocalDate keyDate);

    /**
     * 根据职位查询员工分配
     */
    @Query("SELECT a FROM HrIt0001OrgAssignment a WHERE a.positionPk = :positionPk " +
           "AND a.tenantId = :tenantId AND a.isDeleted = false " +
           "AND a.validFrom <= :keyDate AND a.validTo >= :keyDate")
    List<HrIt0001OrgAssignment> findByPosition(@Param("positionPk") Long positionPk,
                                                @Param("tenantId") Long tenantId,
                                                @Param("keyDate") LocalDate keyDate);

    /**
     * 根据公司代码查询
     */
    List<HrIt0001OrgAssignment> findByCompanyCodeAndTenantIdAndIsDeletedFalse(
            String companyCode, Long tenantId);

    /**
     * 查询员工历史记录
     */
    @Query("SELECT a FROM HrIt0001OrgAssignment a WHERE a.employeeId = :employeeId " +
           "AND a.isDeleted = false ORDER BY a.validFrom DESC")
    List<HrIt0001OrgAssignment> findHistoryByEmployee(@Param("employeeId") Long employeeId);
}
