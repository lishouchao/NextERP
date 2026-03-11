package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.SupSupplier;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 供应商仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SupSupplierRepository extends TenantAwareRepository<SupSupplier> {

    /**
     * 根据供应商编码查询
     *
     * @param supplierCode 供应商编码
     * @param tenantId      租户ID
     * @return 供应商
     */
    Optional<SupSupplier> findBySupplierCodeAndTenantIdAndIsDeletedFalse(String supplierCode, Long tenantId);

    /**
     * 检查供应商编码是否存在
     *
     * @param supplierCode 供应商编码
     * @param tenantId      租户ID
     * @return 是否存在
     */
    boolean existsBySupplierCodeAndTenantIdAndIsDeletedFalse(String supplierCode, Long tenantId);

    /**
     * 检查供应商名称是否存在
     *
     * @param supplierName 供应商名称
     * @param tenantId      租户ID
     * @return 是否存在
     */
    boolean existsBySupplierNameAndTenantIdAndIsDeletedFalse(String supplierName, Long tenantId);

    /**
     * 根据分类查询供应商
     *
     * @param categoryId 分类ID
     * @param tenantId   租户ID
     * @return 供应商列表
     */
    List<SupSupplier> findByCategoryIdAndTenantIdAndIsDeletedFalseOrderBySupplierCodeAsc(Long categoryId, Long tenantId);

    /**
     * 查询启用状态的供应商
     *
     * @param tenantId 租户ID
     * @return 供应商列表
     */
    @Query("SELECT s FROM SupSupplier s WHERE s.tenantId = :tenantId AND s.isDeleted = false AND s.status = 1 ORDER BY s.supplierCode ASC")
    List<SupSupplier> findActiveSuppliers(@Param("tenantId") Long tenantId);

    /**
     * 分页查询供应商
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param pageable 分页
     * @return 供应商分页
     */
    Page<SupSupplier> findByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, Integer status, Pageable pageable);

    /**
     * 根据类型查询供应商
     *
     * @param supplierType 供应商类型
     * @param tenantId      租户ID
     * @return 供应商列表
     */
    List<SupSupplier> findBySupplierTypeAndTenantIdAndIsDeletedFalseOrderBySupplierCodeAsc(Integer supplierType, Long tenantId);

    /**
     * 搜索供应商
     *
     * @param keyword  关键词 (编码或名称)
     * @param tenantId 租户ID
     * @return 供应商列表
     */
    @Query("SELECT s FROM SupSupplier s WHERE s.tenantId = :tenantId AND s.isDeleted = false " +
           "AND (s.supplierCode LIKE %:keyword% OR s.supplierName LIKE %:keyword% OR s.shortName LIKE %:keyword%) " +
           "ORDER BY s.supplierCode ASC")
    List<SupSupplier> searchSuppliers(@Param("keyword") String keyword, @Param("tenantId") Long tenantId);
}
