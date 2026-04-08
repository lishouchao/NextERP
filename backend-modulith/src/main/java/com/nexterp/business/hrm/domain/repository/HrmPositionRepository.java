package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmPosition;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 职位仓储接口
 *
 * @author NextERP
 */
@Repository
public interface HrmPositionRepository extends TenantAwareRepository<HrmPosition> {

    /**
     * 根据职位编码查询
     */
    Optional<HrmPosition> findByPositionCodeAndTenantIdAndIsDeletedFalse(String positionCode, Long tenantId);

    /**
     * 按部门查询职位
     */
    List<HrmPosition> findByDepartmentIdAndTenantIdAndIsDeletedFalseOrderByPositionCodeAsc(Long departmentId, Long tenantId);

    /**
     * 按职务查询职位
     */
    List<HrmPosition> findByJobIdAndTenantIdAndIsDeletedFalseOrderByPositionCodeAsc(Long jobId, Long tenantId);

    /**
     * 查询有空缺的职位
     */
    @Query("SELECT p FROM HrmPosition p WHERE p.tenantId = :tenantId AND p.isDeleted = false AND p.headCount > p.actualCount ORDER BY p.positionCode ASC")
    List<HrmPosition> findVacantPositions(@Param("tenantId") Long tenantId);

    /**
     * 查询有效职位
     */
    List<HrmPosition> findByTenantIdAndIsDeletedFalseAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByPositionCodeAsc(
            Long tenantId, LocalDate validFrom, LocalDate validTo);
}
