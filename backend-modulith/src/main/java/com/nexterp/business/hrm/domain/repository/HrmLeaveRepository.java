package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmLeave;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 请假记录仓储接口
 *
 * @author NextERP
 */
@Repository
public interface HrmLeaveRepository extends TenantAwareRepository<HrmLeave> {

    /**
     * 根据请假单号查询
     */
    Optional<HrmLeave> findByLeaveNoAndTenantIdAndIsDeletedFalse(String leaveNo, Long tenantId);

    /**
     * 按员工查询请假记录
     */
    List<HrmLeave> findByEmployeeIdAndTenantIdAndIsDeletedFalseOrderByStartDateDesc(Long employeeId, Long tenantId);

    /**
     * 按员工和日期范围查询
     */
    List<HrmLeave> findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndTenantIdAndIsDeletedFalse(
            Long employeeId, LocalDate startDate, LocalDate endDate, Long tenantId);

    /**
     * 按审批状态查询
     */
    List<HrmLeave> findByApprovalStatusAndTenantIdAndIsDeletedFalseOrderByStartDateDesc(String approvalStatus, Long tenantId);

    /**
     * 按当前审批人查询
     */
    List<HrmLeave> findByCurrentApproverIdAndTenantIdAndIsDeletedFalse(Long currentApproverId, Long tenantId);
}
