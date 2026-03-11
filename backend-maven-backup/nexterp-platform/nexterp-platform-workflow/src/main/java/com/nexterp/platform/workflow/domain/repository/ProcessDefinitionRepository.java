package com.nexterp.platform.workflow.domain.repository;

import com.nexterp.platform.workflow.domain.model.ProcessDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 流程定义仓储接口
 *
 * @author NextERP
 */
@Repository
public interface ProcessDefinitionRepository extends JpaRepository<ProcessDefinition, Long> {

    /**
     * 根据流程Key查询最新版本
     *
     * @param processKey 流程Key
     * @param tenantId 租户ID
     * @return 流程定义
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.processKey = :processKey AND p.tenantId = :tenantId AND p.status = 1 AND p.isDeleted = false ORDER BY p.version DESC")
    Optional<ProcessDefinition> findLatestByKey(@Param("processKey") String processKey, @Param("tenantId") Long tenantId);

    /**
     * 根据流程Key和版本查询
     *
     * @param processKey 流程Key
     * @param version 版本
     * @param tenantId 租户ID
     * @return 流程定义
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.processKey = :processKey AND p.version = :version AND p.tenantId = :tenantId AND p.isDeleted = false")
    Optional<ProcessDefinition> findByKeyAndVersion(@Param("processKey") String processKey, @Param("version") Integer version, @Param("tenantId") Long tenantId);

    /**
     * 查询租户所有已发布的流程定义
     *
     * @param tenantId 租户ID
     * @return 流程定义列表
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.tenantId = :tenantId AND p.status = 1 AND p.isDeleted = false ORDER BY p.category, p.processName")
    List<ProcessDefinition> findPublished(@Param("tenantId") Long tenantId);

    /**
     * 根据分类查询流程定义
     *
     * @param category 分类
     * @param tenantId 租户ID
     * @return 流程定义列表
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.category = :category AND p.tenantId = :tenantId AND p.status = 1 AND p.isDeleted = false ORDER BY p.processName")
    List<ProcessDefinition> findByCategory(@Param("category") String category, @Param("tenantId") Long tenantId);
}
