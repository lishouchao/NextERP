package com.nexterp.business.finance.application.service;

import com.nexterp.business.finance.domain.model.FinVoucher;
import com.nexterp.business.finance.domain.model.FinVoucherEntry;
import com.nexterp.business.finance.domain.repository.FinVoucherRepository;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 财务凭证服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinVoucherService {

    private final FinVoucherRepository voucherRepository;
    private final FinAccountService accountService;

    /**
     * 创建凭证
     *
     * @param voucher 凭证
     * @return 凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucher(FinVoucher voucher) {
        // 生成凭证号
        if (voucher.getVoucherNo() == null || voucher.getVoucherNo().isEmpty()) {
            voucher.setVoucherNo(generateVoucherNo(voucher.getAccountingPeriod(), voucher.getTenantId()));
        } else {
            // 检查凭证号是否已存在
            if (voucherRepository.existsByVoucherNoAndTenantIdAndIsDeletedFalse(
                    voucher.getVoucherNo(), voucher.getTenantId())) {
                throw new BusinessException("凭证号已存在");
            }
        }

        // 设置默认值
        if (voucher.getVoucherStatus() == null) {
            voucher.setVoucherStatus(0); // 草稿
        }
        if (voucher.getAttachmentCount() == null) {
            voucher.setAttachmentCount(0);
        }

        // 设置制单人信息
        Long currentUserId = UserContext.getUserId();
        voucher.setCreatedById(currentUserId);
        // TODO: 从用户服务获取用户名
        voucher.setCreatedByName("当前用户");

        // 验证并计算分录金额
        validateAndCalculateEntries(voucher);

        FinVoucher saved = voucherRepository.save(voucher);
        log.info("创建凭证成功: voucherNo={}", voucher.getVoucherNo());
        return saved.getId();
    }

    /**
     * 更新凭证
     *
     * @param id      凭证ID
     * @param voucher 凭证
     * @return 更新后的凭证
     */
    @Transactional(rollbackFor = Exception.class)
    public FinVoucher updateVoucher(Long id, FinVoucher voucher) {
        FinVoucher existing = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("凭证不存在"));

        // 检查是否可以编辑
        if (!existing.canEdit()) {
            throw new BusinessException("凭证状态不允许编辑");
        }

        // 检查凭证号是否被其他凭证使用
        if (!existing.getVoucherNo().equals(voucher.getVoucherNo()) &&
            voucherRepository.existsByVoucherNoAndTenantIdAndIsDeletedFalse(
                    voucher.getVoucherNo(), existing.getTenantId())) {
            throw new BusinessException("凭证号已被其他凭证使用");
        }

        // 更新基本信息
        existing.setVoucherDate(voucher.getVoucherDate());
        existing.setAccountingPeriod(voucher.getAccountingPeriod());
        existing.setVoucherType(voucher.getVoucherType());
        existing.setVoucherWord(voucher.getVoucherWord());
        existing.setAttachmentCount(voucher.getAttachmentCount());
        existing.setSummary(voucher.getSummary());
        existing.setRemark(voucher.getRemark());

        // 更新分录
        existing.getEntries().clear();
        if (voucher.getEntries() != null) {
            voucher.getEntries().forEach(entry -> {
                entry.setVoucher(existing);
                existing.getEntries().add(entry);
            });
        }

        // 验证并计算分录金额
        validateAndCalculateEntries(existing);

        return voucherRepository.save(existing);
    }

    /**
     * 删除凭证
     *
     * @param id 凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteVoucher(Long id) {
        FinVoucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("凭证不存在"));

        // 检查是否可以删除
        if (!voucher.canEdit()) {
            throw new BusinessException("凭证状态不允许删除");
        }

        // 软删除
        voucher.setIsDeleted(true);
        voucherRepository.save(voucher);

        log.info("删除凭证成功: id={}", id);
    }

    /**
     * 提交审核
     *
     * @param id 凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitForApproval(Long id) {
        FinVoucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("凭证不存在"));

        if (voucher.getVoucherStatus() != 0) {
            throw new BusinessException("只有草稿状态的凭证才能提交审核");
        }

        voucher.setVoucherStatus(1); // 待审核
        voucherRepository.save(voucher);

        log.info("提交审核成功: id={}", id);
    }

    /**
     * 审核凭证
     *
     * @param id          凭证ID
     * @param approved    是否通过
     * @param rejectReason 驳回原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveVoucher(Long id, boolean approved, String rejectReason) {
        FinVoucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("凭证不存在"));

        if (!voucher.canApprove()) {
            throw new BusinessException("凭证状态不允许审核");
        }

        Long currentUserId = UserContext.getUserId();

        if (approved) {
            voucher.setVoucherStatus(2); // 已审核
            voucher.setApprovedById(currentUserId);
            // TODO: 从用户服务获取用户名
            voucher.setApprovedByName("审核人");
            voucher.setApprovedAt(LocalDateTime.now());
            log.info("审核通过: id={}", id);
        } else {
            voucher.setVoucherStatus(4); // 已驳回
            voucher.setRejectReason(rejectReason);
            log.info("审核驳回: id={}, reason={}", id, rejectReason);
        }

        voucherRepository.save(voucher);
    }

    /**
     * 记账
     *
     * @param id 凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void postVoucher(Long id) {
        FinVoucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("凭证不存在"));

        if (!voucher.canPost()) {
            throw new BusinessException("凭证状态不允许记账");
        }

        // 更新科目余额
        updateAccountBalances(voucher);

        voucher.setVoucherStatus(3); // 已记账
        Long currentUserId = UserContext.getUserId();
        voucher.setPostedById(currentUserId);
        // TODO: 从用户服务获取用户名
        voucher.setPostedByName("记账人");
        voucher.setPostedAt(LocalDateTime.now());

        voucherRepository.save(voucher);
        log.info("凭证记账成功: id={}", id);
    }

    /**
     * 反记账
     *
     * @param id 凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void unpostVoucher(Long id) {
        FinVoucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("凭证不存在"));

        if (!voucher.canUnpost()) {
            throw new BusinessException("凭证状态不允许反记账");
        }

        // 恢复科目余额
        restoreAccountBalances(voucher);

        voucher.setVoucherStatus(2); // 恢复为已审核状态
        voucher.setPostedById(null);
        voucher.setPostedByName(null);
        voucher.setPostedAt(null);

        voucherRepository.save(voucher);
        log.info("凭证反记账成功: id={}", id);
    }

    /**
     * 获取凭证详情
     *
     * @param id 凭证ID
     * @return 凭证
     */
    public FinVoucher getVoucherById(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("凭证不存在"));
    }

    /**
     * 分页查询凭证
     *
     * @param tenantId 租户ID
     * @param status   状态
     * @param pageable 分页
     * @return 分页结果
     */
    public PageResult<FinVoucher> listVouchers(Long tenantId, Integer status, Pageable pageable) {
        Page<FinVoucher> page;
        if (status != null) {
            page = voucherRepository.findByVoucherStatusAndTenantIdAndIsDeletedFalse(status, tenantId, pageable);
        } else {
            page = voucherRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("tenantId"), tenantId),
                            cb.equal(root.get("isDeleted"), false)
                    ),
                    pageable);
        }

        return PageResult.<FinVoucher>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 查询待审核凭证
     *
     * @param tenantId 租户ID
     * @return 凭证列表
     */
    public List<FinVoucher> listPendingApprovalVouchers(Long tenantId) {
        return voucherRepository.findPendingApprovalVouchers(tenantId);
    }

    /**
     * 生成凭证号
     *
     * @param period  会计期间
     * @param tenantId 租户ID
     * @return 凭证号
     */
    private String generateVoucherNo(String period, Long tenantId) {
        Integer nextNo = voucherRepository.findNextVoucherNo(period, tenantId);
        if (nextNo == null) {
            nextNo = 1;
        }
        return period.replace("-", "") + String.format("%04d", nextNo);
    }

    /**
     * 验证并计算分录金额
     *
     * @param voucher 凭证
     */
    private void validateAndCalculateEntries(FinVoucher voucher) {
        List<FinVoucherEntry> entries = voucher.getEntries();
        if (entries == null || entries.isEmpty()) {
            throw new BusinessException("凭证分录不能为空");
        }

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (int i = 0; i < entries.size(); i++) {
            FinVoucherEntry entry = entries.get(i);
            entry.setLineNo(i + 1);
            entry.setVoucherId(voucher.getId());

            // 验证科目
            // TODO: 验证科目是否存在且有效

            // 验证借贷方向
            if ((entry.getDebitAmount() == null || entry.getDebitAmount().compareTo(BigDecimal.ZERO) <= 0) &&
                (entry.getCreditAmount() == null || entry.getCreditAmount().compareTo(BigDecimal.ZERO) <= 0)) {
                throw new BusinessException("第" + (i + 1) + "行分录：借方金额和贷方金额必须至少填写一个");
            }

            if (entry.getDebitAmount() != null && entry.getCreditAmount() != null &&
                entry.getDebitAmount().compareTo(BigDecimal.ZERO) > 0 &&
                entry.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessException("第" + (i + 1) + "行分录：借方金额和贷方金额不能同时填写");
            }

            if (entry.getDebitAmount() != null) {
                totalDebit = totalDebit.add(entry.getDebitAmount());
            }
            if (entry.getCreditAmount() != null) {
                totalCredit = totalCredit.add(entry.getCreditAmount());
            }
        }

        voucher.setDebitAmount(totalDebit);
        voucher.setCreditAmount(totalCredit);

        // 验证借贷平衡
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException("借贷不平衡：借方合计=" + totalDebit + "，贷方合计=" + totalCredit);
        }
    }

    /**
     * 更新科目余额
     *
     * @param voucher 凭证
     */
    private void updateAccountBalances(FinVoucher voucher) {
        for (FinVoucherEntry entry : voucher.getEntries()) {
            // TODO: 更新科目余额
            log.debug("更新科目余额: accountId={}, debit={}, credit={}",
                    entry.getAccountId(), entry.getDebitAmount(), entry.getCreditAmount());
        }
    }

    /**
     * 恢复科目余额
     *
     * @param voucher 凭证
     */
    private void restoreAccountBalances(FinVoucher voucher) {
        for (FinVoucherEntry entry : voucher.getEntries()) {
            // TODO: 恢复科目余额
            log.debug("恢复科目余额: accountId={}, debit={}, credit={}",
                    entry.getAccountId(), entry.getDebitAmount(), entry.getCreditAmount());
        }
    }
}
