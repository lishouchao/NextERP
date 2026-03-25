package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmOrgObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * OM 对象 Repository
 * 对标 SAP HRP1000
 *
 * @author NextERP
 */
@Repository
public interface HrmOrgObjectRepository extends JpaRepository<HrmOrgObject, Long>,
        JpaSpecificationExecutor<HrmOrgObject> {

    /**
     * 根据对象类型和对象ID查找
     */
    Optional<HrmOrgObject> findByObjectTypeAndObjectIdAndIsDeletedFalse(
            String objectType, String objectId);

    /**
     * 根据对象类型查询所有对象
     */
    List<HrmOrgObject> findByObjectTypeAndTenantIdAndIsDeletedFalse(
            String objectType, Long tenantId);

    /**
     * 查询指定日期有效的对象
     */
    @Query("SELECT o FROM HrmOrgObject o WHERE o.objectType = :objectType " +
           "AND o.tenantId = :tenantId AND o.isDeleted = false " +
           "AND o.validFrom <= :keyDate AND o.validTo >= :keyDate")
    List<HrmOrgObject> findValidOnDate(@Param("objectType") String objectType,
                                        @Param("tenantId") Long tenantId,
                                        @Param("keyDate") LocalDate keyDate);

    /**
     * 查询指定对象类型的活跃对象
     */
    @Query("SELECT o FROM HrmOrgObject o WHERE o.objectType = :objectType " +
           "AND o.tenantId = :tenantId AND o.isDeleted = false " +
           "AND o.objStatus = 'ACTIVE' " +
           "AND o.validFrom <= :keyDate AND o.validTo >= :keyDate")
    List<HrmOrgObject> findActiveOnDate(@Param("objectType") String objectType,
                                         @Param("tenantId") Long tenantId,
                                         @Param("keyDate") LocalDate keyDate);

    /**
     * 根据对象状态查询
     */
    List<HrmOrgObject> findByObjectTypeAndObjStatusAndTenantIdAndIsDeletedFalse(
            String objectType, String objStatus, Long tenantId);

    /**
     * 查询对象的完整有效期
     */
    @Query("SELECT o FROM HrmOrgObject o WHERE o.objectType = :objectType " +
           "AND o.objectId = :objectId AND o.tenantId = :tenantId " +
           "AND o.isDeleted = false ORDER BY o.validFrom")
    List<HrmOrgObject> findHistoryByObjectId(@Param("objectType") String objectType,
                                              @Param("objectId") String objectId,
                                              @Param("tenantId") Long tenantId);

    /**
     * 查询在指定日期有变化的对象
     */
    @Query("SELECT o FROM HrmOrgObject o WHERE o.tenantId = :tenantId " +
           "AND o.isDeleted = false " +
           "AND (o.validFrom = :keyDate OR o.validTo = :keyDate)")
    List<HrmOrgObject> findChangedOnDate(@Param("tenantId") Long tenantId,
                                          @Param("keyDate") LocalDate keyDate);
}
