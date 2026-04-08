package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmAttendance;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 考勤记录仓储接口
 *
 * @author NextERP
 */
@Repository
public interface HrmAttendanceRepository extends TenantAwareRepository<HrmAttendance> {

    /**
     * 按员工和日期查询
     */
    Optional<HrmAttendance> findByEmployeeIdAndAttendanceDateAndTenantIdAndIsDeletedFalse(
            Long employeeId, LocalDate attendanceDate, Long tenantId);

    /**
     * 按员工和日期范围查询
     */
    List<HrmAttendance> findByEmployeeIdAndAttendanceDateBetweenAndTenantIdAndIsDeletedFalseOrderByAttendanceDateAsc(
            Long employeeId, LocalDate startDate, LocalDate endDate, Long tenantId);

    /**
     * 按考勤状态查询
     */
    List<HrmAttendance> findByAttendanceStatusAndTenantIdAndIsDeletedFalse(String attendanceStatus, Long tenantId);
}
