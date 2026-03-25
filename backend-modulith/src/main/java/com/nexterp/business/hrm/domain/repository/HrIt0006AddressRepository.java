package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrIt0006Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InfoType 0006 - 地址 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrIt0006AddressRepository extends JpaRepository<HrIt0006Address, Long>,
        JpaSpecificationExecutor<HrIt0006Address> {

    /**
     * 根据员工ID查询
     */
    List<HrIt0006Address> findByEmployeeIdAndIsDeletedFalse(Long employeeId);

    /**
     * 根据员工编号查询
     */
    List<HrIt0006Address> findByEmployeeNoAndTenantIdAndIsDeletedFalse(String employeeNo, Long tenantId);

    /**
     * 根据员工和地址类型查询
     */
    List<HrIt0006Address> findByEmployeeIdAndAddressTypeAndIsDeletedFalse(
            Long employeeId, String addressType);

    /**
     * 查询指定日期有效的记录
     */
    @Query("SELECT a FROM HrIt0006Address a WHERE a.employeeId = :employeeId " +
           "AND a.addressType = :addressType AND a.isDeleted = false " +
           "AND a.validFrom <= :keyDate AND a.validTo >= :keyDate")
    Optional<HrIt0006Address> findValidOnDateByType(@Param("employeeId") Long employeeId,
                                                      @Param("addressType") String addressType,
                                                      @Param("keyDate") LocalDate keyDate);

    /**
     * 查询员工所有有效地址
     */
    @Query("SELECT a FROM HrIt0006Address a WHERE a.employeeId = :employeeId " +
           "AND a.isDeleted = false " +
           "AND a.validFrom <= :keyDate AND a.validTo >= :keyDate")
    List<HrIt0006Address> findAllValidOnDate(@Param("employeeId") Long employeeId,
                                              @Param("keyDate") LocalDate keyDate);

    /**
     * 查询员工历史记录
     */
    @Query("SELECT a FROM HrIt0006Address a WHERE a.employeeId = :employeeId " +
           "AND a.isDeleted = false ORDER BY a.addressType, a.validFrom DESC")
    List<HrIt0006Address> findHistoryByEmployee(@Param("employeeId") Long employeeId);
}
