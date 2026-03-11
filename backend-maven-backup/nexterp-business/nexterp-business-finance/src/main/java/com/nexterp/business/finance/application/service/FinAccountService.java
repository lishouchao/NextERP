package com.nexterp.business.finance.application.service;

import com.nexterp.business.finance.domain.model.FinAccount;
import com.nexterp.business.finance.domain.repository.FinAccountRepository;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 财务科目服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinAccountService {

    private final FinAccountRepository accountRepository;

    /**
     * 创建科目
     *
     * @param account 科目
     * @return 科目ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createAccount(FinAccount account) {
        // 检查科目编码是否已存在
        if (accountRepository.existsByAccountCodeAndTenantIdAndIsDeletedFalse(
                account.getAccountCode(), account.getTenantId())) {
            throw new BusinessException("科目编码已存在");
        }

        // 设置默认值
        if (account.getIsLeaf() == null) {
            account.setIsLeaf(true);
        }
        if (account.getIsCash() == null) {
            account.setIsCash(false);
        }
        if (account.getIsBank() == null) {
            account.setIsBank(false);
        }
        if (account.getIsQuantity() == null) {
            account.setIsQuantity(false);
        }
        if (account.getIsForeignCurrency() == null) {
            account.setIsForeignCurrency(false);
        }
        if (account.getIsAuxiliary() == null) {
            account.setIsAuxiliary(false);
        }
        if (account.getOpeningBalance() == null) {
            account.setOpeningBalance(BigDecimal.ZERO);
        }
        if (account.getOpeningQuantity() == null) {
            account.setOpeningQuantity(BigDecimal.ZERO);
        }
        if (account.getCurrentDebit() == null) {
            account.setCurrentDebit(BigDecimal.ZERO);
        }
        if (account.getCurrentCredit() == null) {
            account.setCurrentCredit(BigDecimal.ZERO);
        }
        if (account.getYearDebit() == null) {
            account.setYearDebit(BigDecimal.ZERO);
        }
        if (account.getYearCredit() == null) {
            account.setYearCredit(BigDecimal.ZERO);
        }
        if (account.getEndingBalance() == null) {
            account.setEndingBalance(account.getOpeningBalance());
        }
        if (account.getStatus() == null) {
            account.setStatus(1);
        }

        // 如果有父科目，更新父科目的isLeaf状态
        if (account.getParentId() != null) {
            accountRepository.findById(account.getParentId()).ifPresent(parent -> {
                if (parent.getIsLeaf()) {
                    parent.setIsLeaf(false);
                    accountRepository.save(parent);
                }
            });
        }

        FinAccount saved = accountRepository.save(account);
        log.info("创建科目成功: code={}, name={}", account.getAccountCode(), account.getAccountName());
        return saved.getId();
    }

    /**
     * 更新科目
     *
     * @param id      科目ID
     * @param account 科目
     * @return 更新后的科目
     */
    @Transactional(rollbackFor = Exception.class)
    public FinAccount updateAccount(Long id, FinAccount account) {
        FinAccount existing = accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目不存在"));

        // 检查科目编码是否被其他科目使用
        if (!existing.getAccountCode().equals(account.getAccountCode()) &&
            accountRepository.existsByAccountCodeAndTenantIdAndIsDeletedFalse(
                    account.getAccountCode(), account.getTenantId())) {
            throw new BusinessException("科目编码已被其他科目使用");
        }

        // 更新基本信息
        existing.setAccountName(account.getAccountName());
        existing.setAccountType(account.getAccountType());
        existing.setAccountDirection(account.getAccountDirection());
        existing.setIsCash(account.getIsCash());
        existing.setIsBank(account.getIsBank());
        existing.setIsQuantity(account.getIsQuantity());
        existing.setQuantityUnit(account.getQuantityUnit());
        existing.setIsForeignCurrency(account.getIsForeignCurrency());
        existing.setCurrency(account.getCurrency());
        existing.setIsAuxiliary(account.getIsAuxiliary());
        existing.setAuxiliaryType(account.getAuxiliaryType());
        existing.setRemark(account.getRemark());

        return accountRepository.save(existing);
    }

    /**
     * 删除科目
     *
     * @param id 科目ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long id) {
        FinAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目不存在"));

        // 检查是否有子科目
        if (accountRepository.existsByParentIdAndTenantIdAndIsDeletedFalse(id, account.getTenantId())) {
            throw new BusinessException("该科目存在子科目，无法删除");
        }

        // 软删除
        account.setIsDeleted(true);
        accountRepository.save(account);

        log.info("删除科目成功: id={}", id);
    }

    /**
     * 获取科目详情
     *
     * @param id 科目ID
     * @return 科目
     */
    public FinAccount getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目不存在"));
    }

    /**
     * 根据科目编码获取科目
     *
     * @param accountCode 科目编码
     * @param tenantId    租户ID
     * @return 科目
     */
    public FinAccount getAccountByCode(String accountCode, Long tenantId) {
        return accountRepository.findByAccountCodeAndTenantIdAndIsDeletedFalse(accountCode, tenantId)
                .orElseThrow(() -> new BusinessException("科目不存在"));
    }

    /**
     * 获取科目树
     *
     * @param tenantId 租户ID
     * @return 科目树
     */
    public List<FinAccount> getAccountTree(Long tenantId) {
        List<FinAccount> allAccounts = accountRepository.findByTenantIdAndIsDeletedFalseOrderBySortOrderAscAccountIdAsc(tenantId);
        return buildAccountTree(allAccounts, null);
    }

    /**
     * 获取指定类型的科目树
     *
     * @param accountType 科目类型
     * @param tenantId    租户ID
     * @return 科目树
     */
    public List<FinAccount> getAccountTreeByType(Integer accountType, Long tenantId) {
        List<FinAccount> accounts = accountRepository.findByAccountTypeAndTenantIdAndIsDeletedFalseOrderByAccountCodeAsc(
                accountType, tenantId);
        return buildAccountTree(accounts, null);
    }

    /**
     * 分页查询科目
     *
     * @param tenantId 租户ID
     * @param pageable 分页
     * @return 分页结果
     */
    public PageResult<FinAccount> listAccounts(Long tenantId, Pageable pageable) {
        Page<FinAccount> page = accountRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);

        return PageResult.<FinAccount>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 查询叶子科目
     *
     * @param tenantId 租户ID
     * @return 科目列表
     */
    public List<FinAccount> listLeafAccounts(Long tenantId) {
        return accountRepository.findByIsLeafTrueAndTenantIdAndIsDeletedFalseOrderByAccountCodeAsc(tenantId);
    }

    /**
     * 查询现金科目
     *
     * @param tenantId 租户ID
     * @return 科目列表
     */
    public List<FinAccount> listCashAccounts(Long tenantId) {
        return accountRepository.findByIsCashTrueAndTenantIdAndIsDeletedFalse(tenantId);
    }

    /**
     * 查询银行科目
     *
     * @param tenantId 租户ID
     * @return 科目列表
     */
    public List<FinAccount> listBankAccounts(Long tenantId) {
        return accountRepository.findByIsBankTrueAndTenantIdAndIsDeletedFalse(tenantId);
    }

    /**
     * 构建科目树
     *
     * @param accounts  所有科目
     * @param parentId  父科目ID
     * @return 科目树
     */
    private List<FinAccount> buildAccountTree(List<FinAccount> accounts, Long parentId) {
        return accounts.stream()
                .filter(account -> {
                    if (parentId == null) {
                        return account.getParentId() == null || account.getParentId() == 0;
                    }
                    return parentId.equals(account.getParentId());
                })
                .peek(account -> {
                    List<FinAccount> children = buildAccountTree(accounts, account.getId());
                    if (!children.isEmpty()) {
                        account.setChildren(children);
                    }
                })
                .collect(Collectors.toList());
    }
}
