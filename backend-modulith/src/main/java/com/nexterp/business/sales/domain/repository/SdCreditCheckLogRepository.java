package com.nexterp.business.sales.domain.repository;

import com.nexterp.business.sales.domain.model.SdCreditCheckLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 信用检查日志仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SdCreditCheckLogRepository extends JpaRepository<SdCreditCheckLog, Long> {

    /**
     * 根据租户ID和客户ID分页查询信用检查日志 (按检查时间降序)
     *
     * @param tenantId   租户ID
     * @param customerId 客户ID
     * @param pageable   分页
     * @return 信用检查日志分页
     */
    Page<SdCreditCheckLog> findByTenantIdAndCustomerIdOrderByCheckTimeDesc(Long tenantId, Long customerId, Pageable pageable);
}
