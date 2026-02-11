package com.nexterp.business.sales.domain.repository;

import com.nexterp.business.sales.domain.model.SalCustomer;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SalCustomerRepository extends TenantAwareRepository<SalCustomer> {

    /**
     * 根据客户编码查询
     *
     * @param customerCode 客户编码
     * @param tenantId      租户ID
     * @return 客户
     */
    Optional<SalCustomer> findByCustomerCodeAndTenantIdAndIsDeletedFalse(String customerCode, Long tenantId);

    /**
     * 检查客户编码是否存在
     *
     * @param customerCode 客户编码
     * @param tenantId      租户ID
     * @return 是否存在
     */
    boolean existsByCustomerCodeAndTenantIdAndIsDeletedFalse(String customerCode, Long tenantId);

    /**
     * 检查客户名称是否存在
     *
     * @param customerName 客户名称
     * @param tenantId      租户ID
     * @return 是否存在
     */
    boolean existsByCustomerNameAndTenantIdAndIsDeletedFalse(String customerName, Long tenantId);

    /**
     * 根据分类查询客户
     *
     * @param categoryId 分类ID
     * @param tenantId   租户ID
     * @return 客户列表
     */
    List<SalCustomer> findByCategoryIdAndTenantIdAndIsDeletedFalseOrderByCustomerCodeAsc(Long categoryId, Long tenantId);

    /**
     * 查询启用状态的客户
     *
     * @param tenantId 租户ID
     * @return 客户列表
     */
    @Query("SELECT c FROM SalCustomer c WHERE c.tenantId = :tenantId AND c.isDeleted = false AND c.status = 1 ORDER BY c.customerCode ASC")
    List<SalCustomer> findActiveCustomers(@Param("tenantId") Long tenantId);

    /**
     * 分页查询客户
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param pageable 分页
     * @return 客户分页
     */
    Page<SalCustomer> findByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, Integer status, Pageable pageable);

    /**
     * 根据类型查询客户
     *
     * @param customerType 客户类型
     * @param tenantId     租户ID
     * @return 客户列表
     */
    List<SalCustomer> findByCustomerTypeAndTenantIdAndIsDeletedFalseOrderByCustomerCodeAsc(Integer customerType, Long tenantId);

    /**
     * 根据销售员查询客户
     *
     * @param salesPersonId 销售员ID
     * @param tenantId       租户ID
     * @return 客户列表
     */
    List<SalCustomer> findBySalesPersonIdAndTenantIdAndIsDeletedFalseOrderByCustomerCodeAsc(Long salesPersonId, Long tenantId);

    /**
     * 搜索客户
     *
     * @param keyword  关键词 (编码或名称)
     * @param tenantId 租户ID
     * @return 客户列表
     */
    @Query("SELECT c FROM SalCustomer c WHERE c.tenantId = :tenantId AND c.isDeleted = false " +
           "AND (c.customerCode LIKE %:keyword% OR c.customerName LIKE %:keyword% OR c.shortName LIKE %:keyword%) " +
           "ORDER BY c.customerCode ASC")
    List<SalCustomer> searchCustomers(@Param("keyword") String keyword, @Param("tenantId") Long tenantId);
}
