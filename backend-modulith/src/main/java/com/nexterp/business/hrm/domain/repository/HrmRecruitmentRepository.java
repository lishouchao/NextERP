package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmRecruitment;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 招聘需求仓储接口
 *
 * @author NextERP
 */
@Repository
public interface HrmRecruitmentRepository extends TenantAwareRepository<HrmRecruitment> {

    /**
     * 根据需求单号查询
     */
    Optional<HrmRecruitment> findByRequisitionNoAndTenantIdAndIsDeletedFalse(String requisitionNo, Long tenantId);

    /**
     * 按部门查询
     */
    List<HrmRecruitment> findByDepartmentIdAndTenantIdAndIsDeletedFalseOrderByRequirementDateDesc(
            Long departmentId, Long tenantId);

    /**
     * 按状态查询
     */
    List<HrmRecruitment> findByStatusAndTenantIdAndIsDeletedFalseOrderByRequirementDateDesc(
            String status, Long tenantId);

    /**
     * 按招聘类型查询
     */
    List<HrmRecruitment> findByRecruitmentTypeAndTenantIdAndIsDeletedFalse(
            String recruitmentType, Long tenantId);

    /**
     * 按需求人查询
     */
    List<HrmRecruitment> findByRequesterIdAndTenantIdAndIsDeletedFalse(Long requesterId, Long tenantId);
}
