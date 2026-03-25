package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrSocialInsuranceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 社保配置 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrSocialInsuranceConfigRepository extends JpaRepository<HrSocialInsuranceConfig, Long>,
        JpaSpecificationExecutor<HrSocialInsuranceConfig> {

    /**
     * 根据城市代码查询
     */
    List<HrSocialInsuranceConfig> findByCityCodeAndTenantIdAndIsDeletedFalse(
            String cityCode, Long tenantId);

    /**
     * 根据城市代码查询启用的配置
     */
    List<HrSocialInsuranceConfig> findByCityCodeAndStatusAndIsDeletedFalse(
            String cityCode, Integer status);

    /**
     * 查询指定日期有效的配置
     */
    @Query("SELECT c FROM HrSocialInsuranceConfig c WHERE c.cityCode = :cityCode " +
           "AND c.tenantId = :tenantId AND c.status = 1 AND c.isDeleted = false " +
           "AND c.validFrom <= :date AND (c.validTo IS NULL OR c.validTo >= :date)")
    Optional<HrSocialInsuranceConfig> findValidOnDate(@Param("cityCode") String cityCode,
                                                       @Param("tenantId") Long tenantId,
                                                       @Param("date") LocalDate date);

    /**
     * 查询所有启用的配置
     */
    List<HrSocialInsuranceConfig> findByTenantIdAndStatusAndIsDeletedFalse(
            Long tenantId, Integer status);

    /**
     * 查询所有配置
     */
    List<HrSocialInsuranceConfig> findByTenantIdAndIsDeletedFalse(Long tenantId);

    /**
     * 根据配置名称查询
     */
    Optional<HrSocialInsuranceConfig> findByConfigNameAndTenantIdAndIsDeletedFalse(
            String configName, Long tenantId);
}
