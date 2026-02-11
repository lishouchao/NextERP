package com.nexterp.platform.workflow.domain.repository;

import com.nexterp.platform.workflow.domain.model.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务分配规则仓储接口
 *
 * @author NextERP
 */
@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    /**
     * 根据任务Key查询分配规则
     *
     * @param processKey 流程Key
     * @param taskKey 任务Key
     * @param tenantId 租户ID
     * @return 分配规则列表
     */
    @Query("SELECT t FROM TaskAssignment t WHERE t.processKey = :processKey AND t.taskKey = :taskKey AND t.tenantId = :tenantId AND t.enabled = true AND t.isDeleted = false ORDER BY t.priority DESC")
    List<TaskAssignment> findByTaskKey(@Param("processKey") String processKey, @Param("taskKey") String taskKey, @Param("tenantId") Long tenantId);

    /**
     * 查询租户所有分配规则
     *
     * @param tenantId 租户ID
     * @return 分配规则列表
     */
    @Query("SELECT t FROM TaskAssignment t WHERE t.tenantId = :tenantId AND t.isDeleted = false ORDER BY t.processKey, t.taskKey, t.priority DESC")
    List<TaskAssignment> findByTenantId(@Param("tenantId") Long tenantId);
}
