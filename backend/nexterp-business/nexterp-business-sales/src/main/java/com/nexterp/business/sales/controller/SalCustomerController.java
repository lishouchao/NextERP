package com.nexterp.business.sales.controller;

import com.nexterp.business.sales.application.service.SalCustomerService;
import com.nexterp.business.sales.domain.model.SalCustomer;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sales/customers")
@RequiredArgsConstructor
public class SalCustomerController {

    private final SalCustomerService customerService;

    /**
     * 创建客户
     *
     * @param customer 客户
     * @return 客户ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sales:customer:add')")
    public Result<Long> createCustomer(@Valid @RequestBody SalCustomer customer) {
        Long id = customerService.createCustomer(customer);
        return Result.success(id);
    }

    /**
     * 更新客户
     *
     * @param id       客户ID
     * @param customer 客户
     * @return 更新后的客户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:customer:edit')")
    public Result<SalCustomer> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody SalCustomer customer) {
        SalCustomer updated = customerService.updateCustomer(id, customer);
        return Result.success(updated);
    }

    /**
     * 删除客户
     *
     * @param id 客户ID
     * @return 成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:customer:delete')")
    public Result<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success();
    }

    /**
     * 启用/禁用客户
     *
     * @param id     客户ID
     * @param status 状态
     * @return 成功
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sales:customer:edit')")
    public Result<Void> updateCustomerStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        customerService.updateCustomerStatus(id, status);
        return Result.success();
    }

    /**
     * 获取客户详情
     *
     * @param id 客户ID
     * @return 客户
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:customer:view')")
    public Result<SalCustomer> getCustomerById(@PathVariable Long id) {
        SalCustomer customer = customerService.getCustomerById(id);
        return Result.success(customer);
    }

    /**
     * 分页查询客户
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('sales:customer:view')")
    public Result<PageResult<SalCustomer>> listCustomers(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(current - 1, size);
        PageResult<SalCustomer> result = customerService.listCustomers(tenantId, status, pageable);
        return Result.success(result);
    }

    /**
     * 查询启用状态的客户
     *
     * @param tenantId 租户ID
     * @return 客户列表
     */
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('sales:customer:view')")
    public Result<List<SalCustomer>> listActiveCustomers(@RequestParam Long tenantId) {
        List<SalCustomer> customers = customerService.listActiveCustomers(tenantId);
        return Result.success(customers);
    }

    /**
     * 根据分类查询客户
     *
     * @param categoryId 分类ID
     * @param tenantId   租户ID
     * @return 客户列表
     */
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAuthority('sales:customer:view')")
    public Result<List<SalCustomer>> listCustomersByCategory(
            @PathVariable Long categoryId,
            @RequestParam Long tenantId) {
        List<SalCustomer> customers = customerService.listCustomersByCategory(categoryId, tenantId);
        return Result.success(customers);
    }

    /**
     * 根据类型查询客户
     *
     * @param customerType 客户类型
     * @param tenantId     租户ID
     * @return 客户列表
     */
    @GetMapping("/type/{customerType}")
    @PreAuthorize("hasAuthority('sales:customer:view')")
    public Result<List<SalCustomer>> listCustomersByType(
            @PathVariable Integer customerType,
            @RequestParam Long tenantId) {
        List<SalCustomer> customers = customerService.listCustomersByType(customerType, tenantId);
        return Result.success(customers);
    }

    /**
     * 根据销售员查询客户
     *
     * @param salesPersonId 销售员ID
     * @param tenantId       租户ID
     * @return 客户列表
     */
    @GetMapping("/sales-person/{salesPersonId}")
    @PreAuthorize("hasAuthority('sales:customer:view')")
    public Result<List<SalCustomer>> listCustomersBySalesPerson(
            @PathVariable Long salesPersonId,
            @RequestParam Long tenantId) {
        List<SalCustomer> customers = customerService.listCustomersBySalesPerson(salesPersonId, tenantId);
        return Result.success(customers);
    }

    /**
     * 搜索客户
     *
     * @param keyword  关键词
     * @param tenantId 租户ID
     * @return 客户列表
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('sales:customer:view')")
    public Result<List<SalCustomer>> searchCustomers(
            @RequestParam String keyword,
            @RequestParam Long tenantId) {
        List<SalCustomer> customers = customerService.searchCustomers(keyword, tenantId);
        return Result.success(customers);
    }
}
