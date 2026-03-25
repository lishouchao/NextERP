package com.nexterp.business.finance.application.service;

import com.nexterp.business.finance.domain.model.FiGlAccount;
import com.nexterp.business.finance.domain.repository.FiGlAccountRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 总账科目服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FiGlAccountService {

    private final FiGlAccountRepository glAccountRepository;

    /**
     * 创建总账科目
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createGlAccount(FiGlAccount account) {
        // 检查代码是否已存在
        if (glAccountRepository.existsByAccountCodeAndCoaIdAndTenantIdAndIsDeletedFalse(
                account.getAccountCode(), account.getCoaId(), account.getTenantId())) {
            throw new BusinessException("科目代码已存在: " + account.getAccountCode());
        }

        // 设置默认值
        if (account.getStatus() == null) {
            account.setStatus(1);
        }
        if (account.getValidFrom() == null) {
            account.setValidFrom(LocalDate.now());
        }
        if (account.getValidTo() == null) {
            account.setValidTo(LocalDate.of(9999, 12, 31));
        }

        // 计算层级和处理父科目
        if (account.getParentId() != null) {
            FiGlAccount parent = glAccountRepository.findById(account.getParentId())
                    .orElseThrow(() -> new BusinessException("父科目不存在"));

            account.setAccountLevel(parent.getAccountLevel() + 1);

            // 更新父科目的叶子节点状态
            if (parent.getIsLeaf()) {
                parent.setIsLeaf(false);
                glAccountRepository.save(parent);
            }
        } else {
            account.setAccountLevel(1);
        }

        FiGlAccount saved = glAccountRepository.save(account);
        log.info("创建总账科目成功: accountCode={}, name={}", saved.getAccountCode(), saved.getAccountName());
        return saved.getId();
    }

    /**
     * 更新总账科目
     */
    @Transactional(rollbackFor = Exception.class)
    public FiGlAccount updateGlAccount(Long id, FiGlAccount account) {
        FiGlAccount existing = glAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目不存在"));

        existing.setAccountName(account.getAccountName());
        existing.setAccountNameEn(account.getAccountNameEn());
        existing.setFieldStatusGroup(account.getFieldStatusGroup());
        existing.setCashFlowType(account.getCashFlowType());
        existing.setTaxCategory(account.getTaxCategory());
        existing.setFunctionalArea(account.getFunctionalArea());
        existing.setRemark(account.getRemark());

        return glAccountRepository.save(existing);
    }

    /**
     * 删除总账科目
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteGlAccount(Long id) {
        FiGlAccount account = glAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目不存在"));

        // 检查是否有子科目
        List<FiGlAccount> children = glAccountRepository
                .findByParentIdAndTenantIdAndIsDeletedFalseOrderByAccountCode(id, account.getTenantId());
        if (!children.isEmpty()) {
            throw new BusinessException("该科目存在子科目，无法删除");
        }

        account.setIsDeleted(true);
        glAccountRepository.save(account);
        log.info("删除总账科目成功: id={}", id);
    }

    /**
     * 获取科目详情
     */
    public FiGlAccount getGlAccountById(Long id) {
        return glAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目不存在"));
    }

    /**
     * 根据代码获取
     */
    public FiGlAccount getGlAccountByCode(String accountCode, Long coaId, Long tenantId) {
        return glAccountRepository.findByAccountCodeAndCoaIdAndTenantIdAndIsDeletedFalse(accountCode, coaId, tenantId)
                .orElseThrow(() -> new BusinessException("科目不存在: " + accountCode));
    }

    /**
     * 获取科目树
     */
    public List<FiGlAccount> getGlAccountTree(Long coaId, Long tenantId) {
        List<FiGlAccount> rootAccounts = glAccountRepository
                .findByParentIdIsNullAndTenantIdAndIsDeletedFalseOrderByAccountCode(tenantId);
        return buildAccountTree(rootAccounts, tenantId);
    }

    /**
     * 构建科目树结构
     */
    private List<FiGlAccount> buildAccountTree(List<FiGlAccount> accounts, Long tenantId) {
        return accounts.stream()
                .peek(account -> {
                    List<FiGlAccount> children = glAccountRepository
                            .findByParentIdAndTenantIdAndIsDeletedFalseOrderByAccountCode(account.getId(), tenantId);
                    if (!children.isEmpty()) {
                        account.setChildren(children);
                        buildAccountTree(children, tenantId);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取当前有效的可记账科目
     */
    public List<FiGlAccount> getValidPostableAccounts(Long tenantId) {
        return glAccountRepository.findValidPostableAccountsByTenantId(tenantId);
    }

    /**
     * 根据科目类型查询
     */
    public List<FiGlAccount> listByAccountType(String accountType, Long tenantId) {
        return glAccountRepository.findByAccountTypeAndTenantIdAndIsDeletedFalseOrderByAccountCode(accountType, tenantId);
    }

    /**
     * 根据科目表查询
     */
    public List<FiGlAccount> listByCoaId(Long coaId, Long tenantId) {
        return glAccountRepository.findByCoaIdAndTenantIdAndIsDeletedFalseOrderBySortOrder(coaId, tenantId);
    }

    /**
     * 分页查询
     */
    public Page<FiGlAccount> listGlAccounts(Long tenantId, Pageable pageable) {
        return glAccountRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }
}
