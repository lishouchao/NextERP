package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmJob;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 职务仓储接口
 *
 * @author NextERP
 */
@Repository
public interface HrmJobRepository extends TenantAwareRepository<HrmJob> {

    /**
     * 根据职务编码查询
     */
    Optional<HrmJob> findByJobCodeAndTenantIdAndIsDeletedFalse(String jobCode, Long tenantId);

    /**
     * 检查职务编码是否存在
     */
    boolean existsByJobCodeAndTenantIdAndIsDeletedFalse(String jobCode, Long tenantId);

    /**
     * 按职务类别查询
     */
    List<HrmJob> findByJobCategoryAndTenantIdAndIsDeletedFalseOrderByJobCodeAsc(String jobCategory, Long tenantId);

    /**
     * 按职务族查询
     */
    List<HrmJob> findByJobFamilyIdAndTenantIdAndIsDeletedFalseOrderByJobCodeAsc(Long jobFamilyId, Long tenantId);

    /**
     * 按上级职务查询
     */
    List<HrmJob> findByParentJobIdAndTenantIdAndIsDeletedFalseOrderByJobCodeAsc(Long parentJobId, Long tenantId);

    /**
     * 查询有效职务
     */
    List<HrmJob> findByTenantIdAndIsDeletedFalseAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByJobCodeAsc(
            Long tenantId, LocalDate validFrom, LocalDate validTo);
}
