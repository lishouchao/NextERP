package com.nexterp.platform.notification.domain.repository;

import com.nexterp.platform.notification.domain.model.SysNotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 通知模板仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SysNotificationTemplateRepository extends JpaRepository<SysNotificationTemplate, Long> {

    /**
     * 根据模板编码查询模板
     *
     * @param templateCode 模板编码
     * @param tenantId     租户ID
     * @return 模板
     */
    @Query("SELECT t FROM SysNotificationTemplate t WHERE t.templateCode = :templateCode AND (t.tenantId = :tenantId OR t.tenantId IS NULL) AND t.isDeleted = false ORDER BY t.tenantId DESC")
    Optional<SysNotificationTemplate> findByTemplateCode(@Param("templateCode") String templateCode, @Param("tenantId") Long tenantId);

    /**
     * 根据通知类型查询模板
     *
     * @param notificationType 通知类型
     * @param tenantId         租户ID
     * @return 模板列表
     */
    @Query("SELECT t FROM SysNotificationTemplate t WHERE t.notificationType = :notificationType AND t.status = 1 AND t.isDeleted = false AND (t.tenantId = :tenantId OR t.tenantId IS NULL)")
    List<SysNotificationTemplate> findByNotificationType(@Param("notificationType") String notificationType, @Param("tenantId") Long tenantId);

    /**
     * 查询所有启用状态的模板
     *
     * @param tenantId 租户ID
     * @return 模板列表
     */
    @Query("SELECT t FROM SysNotificationTemplate t WHERE t.status = 1 AND t.isDeleted = false AND (t.tenantId = :tenantId OR t.tenantId IS NULL)")
    List<SysNotificationTemplate> findAllActive(@Param("tenantId") Long tenantId);
}
