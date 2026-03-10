package com.nexterp.business.sales.application.service;

import com.nexterp.business.sales.domain.model.SalCustomer;
import com.nexterp.business.sales.domain.repository.SalCustomerRepository;
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
 * 客户服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalCustomerService {

    private final SalCustomerRepository customerRepository;

    /**
     * 创建客户
     *
     * @param customer 客户
     * @return 客户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCustomer(SalCustomer customer) {
        // 检查客户编码是否已存在
        if (customerRepository.existsByCustomerCodeAndTenantIdAndIsDeletedFalse(
                customer.getCustomerCode(), customer.getTenantId())) {
            throw new BusinessException("客户编码已存在");
        }

        // 检查客户名称是否已存在
        if (customerRepository.existsByCustomerNameAndTenantIdAndIsDeletedFalse(
                customer.getCustomerName(), customer.getTenantId())) {
            throw new BusinessException("客户名称已存在");
        }

        // 设置默认值
        if (customer.getCustomerType() == null) {
            customer.setCustomerType(1); // 一般客户
        }
        if (customer.getCreditLimit() == null) {
            customer.setCreditLimit(BigDecimal.ZERO);
        }
        if (customer.getCreditDays() == null) {
            customer.setCreditDays(0);
        }
        if (customer.getTotalSaleAmount() == null) {
            customer.setTotalSaleAmount(BigDecimal.ZERO);
        }
        if (customer.getSaleCount() == null) {
            customer.setSaleCount(0);
        }
        if (customer.getStatus() == null) {
            customer.setStatus(1); // 启用
        }

        SalCustomer saved = customerRepository.save(customer);
        log.info("创建客户成功: code={}, name={}", customer.getCustomerCode(), customer.getCustomerName());
        return saved.getId();
    }

    /**
     * 更新客户
     *
     * @param id       客户ID
     * @param customer 客户
     * @return 更新后的客户
     */
    @Transactional(rollbackFor = Exception.class)
    public SalCustomer updateCustomer(Long id, SalCustomer customer) {
        SalCustomer existing = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在"));

        // 检查客户编码是否被其他客户使用
        if (!existing.getCustomerCode().equals(customer.getCustomerCode()) &&
            customerRepository.existsByCustomerCodeAndTenantIdAndIsDeletedFalse(
                    customer.getCustomerCode(), existing.getTenantId())) {
            throw new BusinessException("客户编码已被其他客户使用");
        }

        // 检查客户名称是否被其他客户使用
        if (!existing.getCustomerName().equals(customer.getCustomerName()) &&
            customerRepository.existsByCustomerNameAndTenantIdAndIsDeletedFalse(
                    customer.getCustomerName(), existing.getTenantId())) {
            throw new BusinessException("客户名称已被其他客户使用");
        }

        // 更新基本信息
        existing.setCustomerCode(customer.getCustomerCode());
        existing.setCustomerName(customer.getCustomerName());
        existing.setShortName(customer.getShortName());
        existing.setCustomerType(customer.getCustomerType());
        existing.setCategoryId(customer.getCategoryId());
        existing.setCategoryName(customer.getCategoryName());
        existing.setContactPerson(customer.getContactPerson());
        existing.setContactPhone(customer.getContactPhone());
        existing.setContactMobile(customer.getContactMobile());
        existing.setContactEmail(customer.getContactEmail());
        existing.setProvince(customer.getProvince());
        existing.setCity(customer.getCity());
        existing.setDistrict(customer.getDistrict());
        existing.setAddress(customer.getAddress());
        existing.setTaxNo(customer.getTaxNo());
        existing.setBankName(customer.getBankName());
        existing.setBankAccount(customer.getBankAccount());
        existing.setCreditLimit(customer.getCreditLimit());
        existing.setCreditDays(customer.getCreditDays());
        existing.setPaymentTerms(customer.getPaymentTerms());
        existing.setCurrency(customer.getCurrency());
        existing.setDeliveryTerms(customer.getDeliveryTerms());
        existing.setSalesPersonId(customer.getSalesPersonId());
        existing.setSalesPersonName(customer.getSalesPersonName());
        existing.setRemark(customer.getRemark());
        existing.setAttachments(customer.getAttachments());
        existing.setCustomField1(customer.getCustomField1());
        existing.setCustomField2(customer.getCustomField2());
        existing.setCustomField3(customer.getCustomField3());

        return customerRepository.save(existing);
    }

    /**
     * 删除客户
     *
     * @param id 客户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCustomer(Long id) {
        SalCustomer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在"));

        // TODO: 检查是否有关联的销售订单

        // 软删除
        customer.setIsDeleted(true);
        customerRepository.save(customer);

        log.info("删除客户成功: id={}", id);
    }

    /**
     * 启用/禁用客户
     *
     * @param id     客户ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomerStatus(Long id, Integer status) {
        SalCustomer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在"));

        customer.setStatus(status);
        customerRepository.save(customer);

        log.info("更新客户状态成功: id={}, status={}", id, status);
    }

    /**
     * 获取客户详情
     *
     * @param id 客户ID
     * @return 客户
     */
    public SalCustomer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在"));
    }

    /**
     * 根据客户编码获取客户
     *
     * @param customerCode 客户编码
     * @param tenantId     租户ID
     * @return 客户
     */
    public SalCustomer getCustomerByCode(String customerCode, Long tenantId) {
        return customerRepository.findByCustomerCodeAndTenantIdAndIsDeletedFalse(customerCode, tenantId)
                .orElseThrow(() -> new BusinessException("客户不存在"));
    }

    /**
     * 分页查询客户
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param pageable 分页
     * @return 分页结果
     */
    public PageResult<SalCustomer> listCustomers(Long tenantId, Integer status, Pageable pageable) {
        Page<SalCustomer> page;
        if (status != null) {
            page = customerRepository.findByTenantIdAndStatusAndIsDeletedFalse(tenantId, status, pageable);
        } else {
            page = customerRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("tenantId"), tenantId),
                            cb.equal(root.get("isDeleted"), false)
                    ),
                    pageable);
        }

        return PageResult.<SalCustomer>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 查询启用状态的客户
     *
     * @param tenantId 租户ID
     * @return 客户列表
     */
    public List<SalCustomer> listActiveCustomers(Long tenantId) {
        return customerRepository.findActiveCustomers(tenantId);
    }

    /**
     * 根据分类查询客户
     *
     * @param categoryId 分类ID
     * @param tenantId   租户ID
     * @return 客户列表
     */
    public List<SalCustomer> listCustomersByCategory(Long categoryId, Long tenantId) {
        return customerRepository.findByCategoryIdAndTenantIdAndIsDeletedFalseOrderByCustomerCodeAsc(
                categoryId, tenantId);
    }

    /**
     * 根据类型查询客户
     *
     * @param customerType 客户类型
     * @param tenantId     租户ID
     * @return 客户列表
     */
    public List<SalCustomer> listCustomersByType(Integer customerType, Long tenantId) {
        return customerRepository.findByCustomerTypeAndTenantIdAndIsDeletedFalseOrderByCustomerCodeAsc(
                customerType, tenantId);
    }

    /**
     * 根据销售员查询客户
     *
     * @param salesPersonId 销售员ID
     * @param tenantId       租户ID
     * @return 客户列表
     */
    public List<SalCustomer> listCustomersBySalesPerson(Long salesPersonId, Long tenantId) {
        return customerRepository.findBySalesPersonIdAndTenantIdAndIsDeletedFalseOrderByCustomerCodeAsc(
                salesPersonId, tenantId);
    }

    /**
     * 搜索客户
     *
     * @param keyword  关键词
     * @param tenantId 租户ID
     * @return 客户列表
     */
    public List<SalCustomer> searchCustomers(String keyword, Long tenantId) {
        return customerRepository.searchCustomers(keyword, tenantId);
    }
}
