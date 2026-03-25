package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmOmOrgUnitDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 组织单元详情 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrmOmOrgUnitDetailRepository extends JpaRepository<HrmOmOrgUnitDetail, Long>,
        JpaSpecificationExecutor<HrmOmOrgUnitDetail> {

    /**
     * 根据 OM 对象内码查找
     */
    Optional<HrmOmOrgUnitDetail> findByObjectPkAndIsDeletedFalse(Long objectPk);

    /**
     * 根据组织编码查找
     */
    Optional<HrmOmOrgUnitDetail> findByOrgCodeAndTenantIdAndIsDeletedFalse(
            String orgCode, Long tenantId);

    /**
     * 根据父组织查询子组织
     */
    List<HrmOmOrgUnitDetail> findByParentObjectPkAndTenantIdAndIsDeletedFalse(
            Long parentObjectPk, Long tenantId);

    /**
     * 根据公司代码查询
     */
    List<HrmOmOrgUnitDetail> findByCompanyCodeAndTenantIdAndIsDeletedFalse(
            String companyCode, Long tenantId);

    /**
     * 根据成本中心查询
     */
    List<HrmOmOrgUnitDetail> findByCostCenterCodeAndTenantIdAndIsDeletedFalse(
            String costCenterCode, Long tenantId);

    /**
     * 根据组织分类查询
     */
    List<HrmOmOrgUnitDetail> findByOrgCategoryAndTenantIdAndIsDeletedFalse(
            String orgCategory, Long tenantId);

    /**
     * 查询指定层级及以下的组织
     */
    @Query("SELECT d FROM HrmOmOrgUnitDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false AND d.orgLevel <= :maxLevel")
    List<HrmOmOrgUnitDetail> findByMaxLevel(@Param("tenantId") Long tenantId,
                                             @Param("maxLevel") Integer maxLevel);

    /**
     * 查询指定日期有效的组织详情
     */
    @Query("SELECT d FROM HrmOmOrgUnitDetail d WHERE d.objectPk = :objectPk " +
           "AND d.isDeleted = false " +
           "AND d.validFrom <= :keyDate AND d.validTo >= :keyDate")
    Optional<HrmOmOrgUnitDetail> findValidOnDate(@Param("objectPk") Long objectPk,
                                                  @Param("keyDate") LocalDate keyDate);

    /**
     * 查询超编组织
     */
    @Query("SELECT d FROM HrmOmOrgUnitDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND d.maxHeadcount IS NOT NULL " +
           "AND d.headcount > d.maxHeadcount")
    List<HrmOmOrgUnitDetail> findOverstaffed(@Param("tenantId") Long tenantId);

    /**
     * 按组织路径前缀查询
     */
    @Query("SELECT d FROM HrmOmOrgUnitDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND d.orgPath LIKE :pathPrefix%")
    List<HrmOmOrgUnitDetail> findByPathPrefix(@Param("tenantId") Long tenantId,
                                               @Param("pathPrefix") String pathPrefix);

    /**
     * 查询根组织 (无父组织)
     */
    @Query("SELECT d FROM HrmOmOrgUnitDetail d WHERE d.tenantId = :tenantId " +
           "AND d.isDeleted = false " +
           "AND (d.parentObjectPk IS NULL OR d.parentObjectPk = 0)")
    List<HrmOmOrgUnitDetail> findRootOrgs(@Param("tenantId") Long tenantId);

    /**
     * 统计组织层级深度
     */
    @Query("SELECT MAX(d.orgLevel) FROM HrmOmOrgUnitDetail d " +
           "WHERE d.tenantId = :tenantId AND d.isDeleted = false")
    Optional<Integer> findMaxOrgLevel(@Param("tenantId") Long tenantId);
}
