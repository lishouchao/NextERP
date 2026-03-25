package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmCandidate;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 候选人仓储接口
 *
 * @author NextERP
 */
@Repository
public interface HrmCandidateRepository extends TenantAwareRepository<HrmCandidate> {

    /**
     * 根据候选人编号查询
     */
    Optional<HrmCandidate> findByCandidateNoAndTenantIdAndIsDeletedFalse(String candidateNo, Long tenantId);

    /**
     * 按招聘需求查询
     */
    List<HrmCandidate> findByRequisitionNoAndTenantIdAndIsDeletedFalse(String requisitionNo, Long tenantId);

    /**
     * 按阶段查询
     */
    List<HrmCandidate> findByStageAndTenantIdAndIsDeletedFalse(String stage, Long tenantId);

    /**
     * 按状态查询
     */
    List<HrmCandidate> findByStatusAndTenantIdAndIsDeletedFalse(String status, Long tenantId);

    /**
     * 按姓名模糊查询
     */
    List<HrmCandidate> findByCandidateNameContainingAndTenantIdAndIsDeletedFalse(
            String candidateName, Long tenantId);

}
