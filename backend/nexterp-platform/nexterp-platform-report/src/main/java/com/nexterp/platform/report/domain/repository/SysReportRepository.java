package com.nexterp.platform.report.domain.repository;

import com.nexterp.platform.report.domain.model.SysReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 报表仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SysReportRepository extends JpaRepository<SysReport, Long> {

    /**
     * 根据报表编码查询报表
     *
     * @param reportCode 报表编码
     * @param tenantId   租户ID
     * @return 报表
     */
    @Query("SELECT r FROM SysReport r WHERE r.reportCode = :reportCode AND r.tenantId = :tenantId AND r.isDeleted = false")
    Optional<SysReport> findByReportCode(@Param("reportCode") String reportCode, @Param("tenantId") Long tenantId);

    /**
     * 检查报表编码是否存在
     *
     * @param reportCode 报表编码
     * @param tenantId   租户ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(r) > 0 FROM SysReport r WHERE r.reportCode = :reportCode AND r.tenantId = :tenantId AND r.isDeleted = false")
    boolean existsByReportCode(@Param("reportCode") String reportCode, @Param("tenantId") Long tenantId);

    /**
     * 查询租户所有启用状态的报表
     *
     * @param tenantId 租户ID
     * @return 报表列表
     */
    @Query("SELECT r FROM SysReport r WHERE r.tenantId = :tenantId AND r.status = 1 AND r.isDeleted = false ORDER BY r.sortOrder")
    List<SysReport> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据分组查询报表
     *
     * @param tenantId    租户ID
     * @param reportGroup 分组
     * @return 报表列表
     */
    @Query("SELECT r FROM SysReport r WHERE r.tenantId = :tenantId AND r.reportGroup = :reportGroup AND r.status = 1 AND r.isDeleted = false ORDER BY r.sortOrder")
    List<SysReport> findByTenantIdAndGroup(@Param("tenantId") Long tenantId, @Param("reportGroup") String reportGroup);
}
