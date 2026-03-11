package com.nexterp.business.finance.controller;

import com.nexterp.business.finance.application.service.FinVoucherService;
import com.nexterp.business.finance.domain.model.FinVoucher;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 财务凭证控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/finance/vouchers")
@RequiredArgsConstructor
public class FinVoucherController {

    private final FinVoucherService voucherService;

    /**
     * 创建凭证
     *
     * @param voucher 凭证
     * @return 凭证ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('finance:voucher:add')")
    public Result<Long> createVoucher(@Valid @RequestBody FinVoucher voucher) {
        Long id = voucherService.createVoucher(voucher);
        return Result.success(id);
    }

    /**
     * 更新凭证
     *
     * @param id      凭证ID
     * @param voucher 凭证
     * @return 更新后的凭证
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:voucher:edit')")
    public Result<FinVoucher> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody FinVoucher voucher) {
        FinVoucher updated = voucherService.updateVoucher(id, voucher);
        return Result.success(updated);
    }

    /**
     * 删除凭证
     *
     * @param id 凭证ID
     * @return 成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:voucher:delete')")
    public Result<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return Result.success();
    }

    /**
     * 提交审核
     *
     * @param id 凭证ID
     * @return 成功
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('finance:voucher:submit')")
    public Result<Void> submitForApproval(@PathVariable Long id) {
        voucherService.submitForApproval(id);
        return Result.success();
    }

    /**
     * 审核凭证
     *
     * @param id          凭证ID
     * @param approved    是否通过
     * @param rejectReason 驳回原因
     * @return 成功
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('finance:voucher:approve')")
    public Result<Void> approveVoucher(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectReason) {
        voucherService.approveVoucher(id, approved, rejectReason);
        return Result.success();
    }

    /**
     * 记账
     *
     * @param id 凭证ID
     * @return 成功
     */
    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('finance:voucher:post')")
    public Result<Void> postVoucher(@PathVariable Long id) {
        voucherService.postVoucher(id);
        return Result.success();
    }

    /**
     * 反记账
     *
     * @param id 凭证ID
     * @return 成功
     */
    @PostMapping("/{id}/unpost")
    @PreAuthorize("hasAuthority('finance:voucher:unpost')")
    public Result<Void> unpostVoucher(@PathVariable Long id) {
        voucherService.unpostVoucher(id);
        return Result.success();
    }

    /**
     * 获取凭证详情
     *
     * @param id 凭证ID
     * @return 凭证
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:voucher:view')")
    public Result<FinVoucher> getVoucherById(@PathVariable Long id) {
        FinVoucher voucher = voucherService.getVoucherById(id);
        return Result.success(voucher);
    }

    /**
     * 分页查询凭证
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param current  当前页
     * @param size     每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('finance:voucher:view')")
    public Result<PageResult<FinVoucher>> listVouchers(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(current - 1, size);
        PageResult<FinVoucher> result = voucherService.listVouchers(tenantId, status, pageable);
        return Result.success(result);
    }

    /**
     * 查询待审核凭证
     *
     * @param tenantId 租户ID
     * @return 凭证列表
     */
    @GetMapping("/pending-approval")
    @PreAuthorize("hasAuthority('finance:voucher:view')")
    public Result<List<FinVoucher>> listPendingApprovalVouchers(@RequestParam Long tenantId) {
        List<FinVoucher> vouchers = voucherService.listPendingApprovalVouchers(tenantId);
        return Result.success(vouchers);
    }
}
