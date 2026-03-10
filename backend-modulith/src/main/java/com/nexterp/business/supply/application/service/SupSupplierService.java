package com.nexterp.business.supply.application.service;

import com.nexterp.business.supply.domain.model.SupSupplier;
import com.nexterp.business.supply.domain.repository.SupSupplierRepository;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupSupplierService {

    private final SupSupplierRepository supplierRepository;

    /**
     * 创建供应商
     *
     * @param supplier 供应商
     * @return 供应商ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createSupplier(SupSupplier supplier) {
        // 检查供应商编码是否已存在
        if (supplierRepository.existsBySupplierCodeAndTenantIdAndIsDeletedFalse(
                supplier.getSupplierCode(), supplier.getTenantId())) {
            throw new BusinessException("供应商编码已存在");
        }

        // 检查供应商名称是否已存在
        if (supplierRepository.existsBySupplierNameAndTenantIdAndIsDeletedFalse(
                supplier.getSupplierName(), supplier.getTenantId())) {
            throw new BusinessException("供应商名称已存在");
        }

        // 设置默认值
        if (supplier.getSupplierType() == null) {
            supplier.setSupplierType(1); // 一般供应商
        }
        if (supplier.getCreditLimit() == null) {
            supplier.setCreditLimit(BigDecimal.ZERO);
        }
        if (supplier.getCreditDays() == null) {
            supplier.setCreditDays(0);
        }
        if (supplier.getDeliveryDays() == null) {
            supplier.setDeliveryDays(7); // 默认7天
        }
        if (supplier.getQualifiedRate() == null) {
            supplier.setQualifiedRate(new BigDecimal("100.00"));
        }
        if (supplier.getOnTimeDeliveryRate() == null) {
            supplier.setOnTimeDeliveryRate(new BigDecimal("100.00"));
        }
        if (supplier.getTotalPurchaseAmount() == null) {
            supplier.setTotalPurchaseAmount(BigDecimal.ZERO);
        }
        if (supplier.getPurchaseCount() == null) {
            supplier.setPurchaseCount(0);
        }
        if (supplier.getStatus() == null) {
            supplier.setStatus(1); // 启用
        }

        SupSupplier saved = supplierRepository.save(supplier);
        log.info("创建供应商成功: code={}, name={}", supplier.getSupplierCode(), supplier.getSupplierName());
        return saved.getId();
    }

    /**
     * 更新供应商
     *
     * @param id       供应商ID
     * @param supplier 供应商
     * @return 更新后的供应商
     */
    @Transactional(rollbackFor = Exception.class)
    public SupSupplier updateSupplier(Long id, SupSupplier supplier) {
        SupSupplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException("供应商不存在"));

        // 检查供应商编码是否被其他供应商使用
        if (!existing.getSupplierCode().equals(supplier.getSupplierCode()) &&
            supplierRepository.existsBySupplierCodeAndTenantIdAndIsDeletedFalse(
                    supplier.getSupplierCode(), existing.getTenantId())) {
            throw new BusinessException("供应商编码已被其他供应商使用");
        }

        // 检查供应商名称是否被其他供应商使用
        if (!existing.getSupplierName().equals(supplier.getSupplierName()) &&
            supplierRepository.existsBySupplierNameAndTenantIdAndIsDeletedFalse(
                    supplier.getSupplierName(), existing.getTenantId())) {
            throw new BusinessException("供应商名称已被其他供应商使用");
        }

        // 更新基本信息
        existing.setSupplierCode(supplier.getSupplierCode());
        existing.setSupplierName(supplier.getSupplierName());
        existing.setShortName(supplier.getShortName());
        existing.setSupplierType(supplier.getSupplierType());
        existing.setCategoryId(supplier.getCategoryId());
        existing.setCategoryName(supplier.getCategoryName());
        existing.setContactPerson(supplier.getContactPerson());
        existing.setContactPhone(supplier.getContactPhone());
        existing.setContactMobile(supplier.getContactMobile());
        existing.setContactEmail(supplier.getContactEmail());
        existing.setProvince(supplier.getProvince());
        existing.setCity(supplier.getCity());
        existing.setDistrict(supplier.getDistrict());
        existing.setAddress(supplier.getAddress());
        existing.setTaxNo(supplier.getTaxNo());
        existing.setBankName(supplier.getBankName());
        existing.setBankAccount(supplier.getBankAccount());
        existing.setCreditLimit(supplier.getCreditLimit());
        existing.setCreditDays(supplier.getCreditDays());
        existing.setPaymentTerms(supplier.getPaymentTerms());
        existing.setCurrency(supplier.getCurrency());
        existing.setDeliveryDays(supplier.getDeliveryDays());
        existing.setMinimumOrderQty(supplier.getMinimumOrderQty());
        existing.setQualityLevel(supplier.getQualityLevel());
        existing.setRemark(supplier.getRemark());
        existing.setAttachments(supplier.getAttachments());
        existing.setCustomField1(supplier.getCustomField1());
        existing.setCustomField2(supplier.getCustomField2());
        existing.setCustomField3(supplier.getCustomField3());

        return supplierRepository.save(existing);
    }

    /**
     * 删除供应商
     *
     * @param id 供应商ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSupplier(Long id) {
        SupSupplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException("供应商不存在"));

        // TODO: 检查是否有关联的采购订单

        // 软删除
        supplier.setIsDeleted(true);
        supplierRepository.save(supplier);

        log.info("删除供应商成功: id={}", id);
    }

    /**
     * 启用/禁用供应商
     *
     * @param id     供应商ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSupplierStatus(Long id, Integer status) {
        SupSupplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException("供应商不存在"));

        supplier.setStatus(status);
        supplierRepository.save(supplier);

        log.info("更新供应商状态成功: id={}, status={}", id, status);
    }

    /**
     * 获取供应商详情
     *
     * @param id 供应商ID
     * @return 供应商
     */
    public SupSupplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException("供应商不存在"));
    }

    /**
     * 根据供应商编码获取供应商
     *
     * @param supplierCode 供应商编码
     * @param tenantId      租户ID
     * @return 供应商
     */
    public SupSupplier getSupplierByCode(String supplierCode, Long tenantId) {
        return supplierRepository.findBySupplierCodeAndTenantIdAndIsDeletedFalse(supplierCode, tenantId)
                .orElseThrow(() -> new BusinessException("供应商不存在"));
    }

    /**
     * 分页查询供应商
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param pageable 分页
     * @return 分页结果
     */
    public PageResult<SupSupplier> listSuppliers(Long tenantId, Integer status, Pageable pageable) {
        Page<SupSupplier> page;
        if (status != null) {
            page = supplierRepository.findByTenantIdAndStatusAndIsDeletedFalse(tenantId, status, pageable);
        } else {
            page = supplierRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("tenantId"), tenantId),
                            cb.equal(root.get("isDeleted"), false)
                    ),
                    pageable);
        }

        return PageResult.<SupSupplier>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 查询启用状态的供应商
     *
     * @param tenantId 租户ID
     * @return 供应商列表
     */
    public List<SupSupplier> listActiveSuppliers(Long tenantId) {
        return supplierRepository.findActiveSuppliers(tenantId);
    }

    /**
     * 根据分类查询供应商
     *
     * @param categoryId 分类ID
     * @param tenantId   租户ID
     * @return 供应商列表
     */
    public List<SupSupplier> listSuppliersByCategory(Long categoryId, Long tenantId) {
        return supplierRepository.findByCategoryIdAndTenantIdAndIsDeletedFalseOrderBySupplierCodeAsc(
                categoryId, tenantId);
    }

    /**
     * 根据类型查询供应商
     *
     * @param supplierType 供应商类型
     * @param tenantId      租户ID
     * @return 供应商列表
     */
    public List<SupSupplier> listSuppliersByType(Integer supplierType, Long tenantId) {
        return supplierRepository.findBySupplierTypeAndTenantIdAndIsDeletedFalseOrderBySupplierCodeAsc(
                supplierType, tenantId);
    }

    /**
     * 搜索供应商
     *
     * @param keyword  关键词
     * @param tenantId 租户ID
     * @return 供应商列表
     */
    public List<SupSupplier> searchSuppliers(String keyword, Long tenantId) {
        return supplierRepository.searchSuppliers(keyword, tenantId);
    }
}
