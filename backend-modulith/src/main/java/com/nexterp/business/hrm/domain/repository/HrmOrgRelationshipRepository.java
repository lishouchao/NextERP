package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmOrgRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * OM 对象关系 Repository
 * 对标 SAP HRP1001
 *
 * @author NextERP
 */
@Repository
public interface HrmOrgRelationshipRepository extends JpaRepository<HrmOrgRelationship, Long>,
        JpaSpecificationExecutor<HrmOrgRelationship> {

    /**
     * 根据关系类型查询
     */
    List<HrmOrgRelationship> findByRelationTypeAndTenantIdAndIsDeletedFalse(
            String relationType, Long tenantId);

    /**
     * 查询指定对象作为 A 端的所有关系
     */
    List<HrmOrgRelationship> findByObjectTypeAAndObjectIdAAndTenantIdAndIsDeletedFalse(
            String objectTypeA, String objectIdA, Long tenantId);

    /**
     * 查询指定对象作为 B 端的所有关系
     */
    List<HrmOrgRelationship> findByObjectTypeBAndObjectIdBAndTenantIdAndIsDeletedFalse(
            String objectTypeB, String objectIdB, Long tenantId);

    /**
     * 查询指定关系的所有记录 (A->B)
     */
    Optional<HrmOrgRelationship> findByObjectTypeAAndObjectIdAAndObjectTypeBAndObjectIdBAndRelationTypeAndIsDeletedFalse(
            String objectTypeA, String objectIdA,
            String objectTypeB, String objectIdB,
            String relationType);

    /**
     * 查询指定日期有效的关系
     */
    @Query("SELECT r FROM HrmOrgRelationship r WHERE r.objectTypeA = :objectTypeA " +
           "AND r.objectIdA = :objectIdA AND r.tenantId = :tenantId " +
           "AND r.isDeleted = false " +
           "AND r.validFrom <= :keyDate AND r.validTo >= :keyDate")
    List<HrmOrgRelationship> findValidOnDate(@Param("objectTypeA") String objectTypeA,
                                              @Param("objectIdA") String objectIdA,
                                              @Param("tenantId") Long tenantId,
                                              @Param("keyDate") LocalDate keyDate);

    /**
     * 查询组织单元的下级 (002 关系)
     */
    @Query("SELECT r FROM HrmOrgRelationship r WHERE r.relationType = '002' " +
           "AND r.objectTypeA = 'O' AND r.objectIdA = :orgId " +
           "AND r.tenantId = :tenantId AND r.isDeleted = false " +
           "AND r.validFrom <= :keyDate AND r.validTo >= :keyDate")
    List<HrmOrgRelationship> findSubOrgUnits(@Param("orgId") String orgId,
                                              @Param("tenantId") Long tenantId,
                                              @Param("keyDate") LocalDate keyDate);

    /**
     * 查询组织单元的职位 (003 关系: 组织-职位)
     */
    @Query("SELECT r FROM HrmOrgRelationship r WHERE r.relationType = '003' " +
           "AND r.objectTypeA = 'O' AND r.objectIdA = :orgId " +
           "AND r.tenantId = :tenantId AND r.isDeleted = false " +
           "AND r.validFrom <= :keyDate AND r.validTo >= :keyDate")
    List<HrmOrgRelationship> findPositionsByOrg(@Param("orgId") String orgId,
                                                 @Param("tenantId") Long tenantId,
                                                 @Param("keyDate") LocalDate keyDate);

    /**
     * 查询职位的职务 (007 关系: 职位-职务)
     */
    @Query("SELECT r FROM HrmOrgRelationship r WHERE r.relationType = '007' " +
           "AND r.objectTypeA = 'S' AND r.objectIdA = :positionId " +
           "AND r.tenantId = :tenantId AND r.isDeleted = false " +
           "AND r.validFrom <= :keyDate AND r.validTo >= :keyDate")
    Optional<HrmOrgRelationship> findJobByPosition(@Param("positionId") String positionId,
                                                    @Param("tenantId") Long tenantId,
                                                    @Param("keyDate") LocalDate keyDate);

    /**
     * 查询职位的任职者 (008 关系: 职位-人员)
     */
    @Query("SELECT r FROM HrmOrgRelationship r WHERE r.relationType = '008' " +
           "AND r.objectTypeA = 'S' AND r.objectIdA = :positionId " +
           "AND r.tenantId = :tenantId AND r.isDeleted = false " +
           "AND r.validFrom <= :keyDate AND r.validTo >= :keyDate")
    List<HrmOrgRelationship> findHoldersByPosition(@Param("positionId") String positionId,
                                                    @Param("tenantId") Long tenantId,
                                                    @Param("keyDate") LocalDate keyDate);

    /**
     * 查询人员的职位 (009 关系: 人员-职位)
     */
    @Query("SELECT r FROM HrmOrgRelationship r WHERE r.relationType = '009' " +
           "AND r.objectTypeB = 'S' AND r.objectIdB = :positionId " +
           "AND r.objectTypeA = 'P' AND r.tenantId = :tenantId " +
           "AND r.isDeleted = false AND r.isPrimary = true " +
           "AND r.validFrom <= :keyDate AND r.validTo >= :keyDate")
    Optional<HrmOrgRelationship> findPrimaryHolder(@Param("positionId") String positionId,
                                                    @Param("tenantId") Long tenantId,
                                                    @Param("keyDate") LocalDate keyDate);

    /**
     * 查询组织单元的负责人 (009 关系 + manager标识)
     */
    @Query("SELECT r FROM HrmOrgRelationship r WHERE r.relationType = '009' " +
           "AND r.objectTypeB = 'O' AND r.objectIdB = :orgId " +
           "AND r.tenantId = :tenantId AND r.isDeleted = false " +
           "AND r.validFrom <= :keyDate AND r.validTo >= :keyDate")
    List<HrmOrgRelationship> findManagersByOrg(@Param("orgId") String orgId,
                                                @Param("tenantId") Long tenantId,
                                                @Param("keyDate") LocalDate keyDate);
}
