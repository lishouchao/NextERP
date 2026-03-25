package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmOmPositionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 职位详情 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrmOmPositionDetailRepository extends JpaRepository<HrmOmPositionDetail, Long>,
        JpaSpecificationExecutor<HrmOmPositionDetail> {

    /**
     * 根据 OM 对象内码查找
     */
    Optional<HrmOmPositionDetail> findByObjectPkAndIsDeletedFalse(Long objectPk);

    /**
     * 根据职位编码查找
     */
    Optional<HrmOmPositionDetail> findByPositionCodeAndTenantIdAndIsDeletedFalse(
            String positionCode, Long tenantId);

    /**
     * 根据所属组织查询
     */
    List<HrmOmPositionDetail> findByOrgObjectPkAndTenantIdAndIsDeletedFalse(
            Long orgObjectPk, Long tenantId);

    /**
     * 根据职务查询
     */
    List<HrmOmPositionDetail> findByJobObjectPkAndTenantIdAndIsDeletedFalse(
            Long jobObjectPk, Long tenantId);

    /**
     * 根据任职者查询
     */
    Optional<HrmOmPositionDetail> findByHolderObjectPkAndTenantIdAndIsDeletedFalse(
            Long holderObjectPk, Long tenantId);

    /**
     * 根据职位状态查询
     */
    List<HrmOmPositionDetail> findByPositionStatusAndTenantIdAndIsDeletedFalse(
            String positionStatus, Long tenantId);

    /**
     * 根据职位类型查询
     */
    List<HrmOmPositionDetail> findByPositionTypeAndTenantIdAndIsDeletedFalse(
            String positionType, Long tenantId);

    /**
     * 查询指定日期有效的职位详情
     */
    @Query("SELECT d FROM HrmOmPositionDetail d WHERE d.objectPk = :objectPk " +
           "AND d.isDeleted = false " +
           "AND d.validFrom <= :keyDate AND d.validTo >= :keyDate")
    Optional<HrmOmPositionDetail> findValidOnDate(@Param("objectPk") Long objectPk,
                                                   @Param("keyDate") LocalDate keyDate);

    /**
     * 查询空缺职位
     */
    @Query("SELECT d FROM HrmOmPositionDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND d.positionStatus = 'VACANT' " +
           "AND d.validFrom <= :keyDate AND d.validTo >= :keyDate")
    List<HrmOmPositionDetail> findVacantPositions(@Param("tenantId") Long tenantId,
                                                   @Param("keyDate") LocalDate keyDate);

    /**
     * 查询有编制空缺的职位
     */
    @Query("SELECT d FROM HrmOmPositionDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND d.currentCount < d.headcount " +
           "AND d.validFrom <= :keyDate AND d.validTo >= :keyDate")
    List<HrmOmPositionDetail> findWithVacancy(@Param("tenantId") Long tenantId,
                                               @Param("keyDate") LocalDate keyDate);

    /**
     * 查询关键岗位
     */
    @Query("SELECT d FROM HrmOmPositionDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND d.isKeyPosition = true " +
           "AND d.validFrom <= :keyDate AND d.validTo >= :keyDate")
    List<HrmOmPositionDetail> findKeyPositions(@Param("tenantId") Long tenantId,
                                                @Param("keyDate") LocalDate keyDate);

    /**
     * 查询经理岗位
     */
    @Query("SELECT d FROM HrmOmPositionDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND d.isManager = true " +
           "AND d.validFrom <= :keyDate AND d.validTo >= :keyDate")
    List<HrmOmPositionDetail> findManagerPositions(@Param("tenantId") Long tenantId,
                                                    @Param("keyDate") LocalDate keyDate);

    /**
     * 根据职级查询
     */
    @Query("SELECT d FROM HrmOmPositionDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND d.grade = :grade " +
           "AND d.validFrom <= :keyDate AND d.validTo >= :keyDate")
    List<HrmOmPositionDetail> findByGrade(@Param("tenantId") Long tenantId,
                                           @Param("grade") String grade,
                                           @Param("keyDate") LocalDate keyDate);

    /**
     * 增加当前人数
     */
    @Modifying
    @Query("UPDATE HrmOmPositionDetail d SET d.currentCount = d.currentCount + 1 " +
           "WHERE d.objectPk = :objectPk AND d.currentCount < d.headcount")
    int incrementCurrentCount(@Param("objectPk") Long objectPk);

    /**
     * 减少当前人数
     */
    @Modifying
    @Query("UPDATE HrmOmPositionDetail d SET d.currentCount = d.currentCount - 1 " +
           "WHERE d.objectPk = :objectPk AND d.currentCount > 0")
    int decrementCurrentCount(@Param("objectPk") Long objectPk);

    /**
     * 更新任职者信息
     */
    @Modifying
    @Query("UPDATE HrmOmPositionDetail d SET " +
           "d.holderObjectPk = :holderPk, " +
           "d.holderEmployeeNo = :employeeNo, " +
           "d.holderName = :holderName, " +
           "d.currentCount = 1, " +
           "d.positionStatus = 'OCCUPIED' " +
           "WHERE d.objectPk = :objectPk")
    void updateHolder(@Param("objectPk") Long objectPk,
                      @Param("holderPk") Long holderPk,
                      @Param("employeeNo") String employeeNo,
                      @Param("holderName") String holderName);

    /**
     * 清空任职者信息
     */
    @Modifying
    @Query("UPDATE HrmOmPositionDetail d SET " +
           "d.holderObjectPk = NULL, " +
           "d.holderEmployeeNo = NULL, " +
           "d.holderName = NULL, " +
           "d.currentCount = 0, " +
           "d.positionStatus = 'VACANT' " +
           "WHERE d.objectPk = :objectPk")
    void clearHolder(@Param("objectPk") Long objectPk);

    /**
     * 按组织统计职位数
     */
    @Query("SELECT d.orgObjectPk, COUNT(d) FROM HrmOmPositionDetail d " +
           "WHERE d.tenantId = :tenantId AND d.isDeleted = false " +
           "AND d.validFrom <= :keyDate AND d.validTo >= :keyDate " +
           "GROUP BY d.orgObjectPk")
    List<Object[]> countByOrg(@Param("tenantId") Long tenantId,
                               @Param("keyDate") LocalDate keyDate);

    /**
     * 按职务统计职位数
     */
    @Query("SELECT d.jobObjectPk, COUNT(d) FROM HrmOmPositionDetail d " +
           "WHERE d.tenantId = :tenantId AND d.isDeleted = false " +
           "AND d.validFrom <= :keyDate AND d.validTo >= :keyDate " +
           "GROUP BY d.jobObjectPk")
    List<Object[]> countByJob(@Param("tenantId") Long tenantId,
                               @Param("keyDate") LocalDate keyDate);
}
