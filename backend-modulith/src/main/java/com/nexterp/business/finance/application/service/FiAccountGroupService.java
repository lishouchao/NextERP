package com.nexterp.business.finance.application.service;

import com.nexterp.business.finance.domain.model.FiAccountGroup;
import com.nexterp.business.finance.domain.repository.FiAccountGroupRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 科目组服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FiAccountGroupService {

    private final FiAccountGroupRepository accountGroupRepository;

    /**
     * 创建科目组
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createAccountGroup(FiAccountGroup accountGroup) {
        // 检查代码是否已存在
        if (accountGroupRepository.existsByGroupCodeAndTenantIdAndIsDeletedFalse(
                accountGroup.getGroupCode(), accountGroup.getTenantId())) {
            throw new BusinessException("科目组代码已存在: " + accountGroup.getGroupCode());
        }

        // 设置默认值
        if (accountGroup.getStatus() == null) {
            accountGroup.setStatus(1);
        }

        // 计算层级
        if (accountGroup.getParentId() != null) {
            FiAccountGroup parent = accountGroupRepository.findById(accountGroup.getParentId())
                    .orElseThrow(() -> new BusinessException("父科目组不存在"));
            accountGroup.setGroupLevel(parent.getGroupLevel() + 1);
        } else {
            accountGroup.setGroupLevel(1);
        }

        FiAccountGroup saved = accountGroupRepository.save(accountGroup);
        log.info("创建科目组成功: groupCode={}, name={}", saved.getGroupCode(), saved.getGroupName());
        return saved.getId();
    }

    /**
     * 更新科目组
     */
    @Transactional(rollbackFor = Exception.class)
    public FiAccountGroup updateAccountGroup(Long id, FiAccountGroup accountGroup) {
        FiAccountGroup existing = accountGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目组不存在"));

        existing.setGroupName(accountGroup.getGroupName());
        existing.setGroupNameEn(accountGroup.getGroupNameEn());
        existing.setFieldStatusGroup(accountGroup.getFieldStatusGroup());
        existing.setRemark(accountGroup.getRemark());

        return accountGroupRepository.save(existing);
    }

    /**
     * 删除科目组
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccountGroup(Long id) {
        FiAccountGroup accountGroup = accountGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目组不存在"));

        // 检查是否有子科目组
        List<FiAccountGroup> children = accountGroupRepository
                .findByParentIdAndTenantIdAndIsDeletedFalseOrderBySortOrder(id, accountGroup.getTenantId());
        if (!children.isEmpty()) {
            throw new BusinessException("该科目组存在子科目组，无法删除");
        }

        accountGroup.setIsDeleted(true);
        accountGroupRepository.save(accountGroup);
        log.info("删除科目组成功: id={}", id);
    }

    /**
     * 获取科目组详情
     */
    public FiAccountGroup getAccountGroupById(Long id) {
        return accountGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科目组不存在"));
    }

    /**
     * 获取科目组树
     */
    public List<FiAccountGroup> getAccountGroupTree(Long tenantId) {
        List<FiAccountGroup> allGroups = accountGroupRepository
                .findByParentIdIsNullAndTenantIdAndIsDeletedFalseOrderBySortOrder(tenantId);
        return buildTree(allGroups, tenantId);
    }

    /**
     * 构建树结构
     */
    private List<FiAccountGroup> buildTree(List<FiAccountGroup> groups, Long tenantId) {
        return groups.stream()
                .peek(group -> {
                    List<FiAccountGroup> children = accountGroupRepository
                            .findByParentIdAndTenantIdAndIsDeletedFalseOrderBySortOrder(group.getId(), tenantId);
                    if (!children.isEmpty()) {
                        // 递归构建子树
                        buildTree(children, tenantId);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据科目表ID查询
     */
    public List<FiAccountGroup> listByCoaId(Long coaId, Long tenantId) {
        return accountGroupRepository.findByCoaIdAndTenantIdAndIsDeletedFalseOrderBySortOrder(coaId, tenantId);
    }

    /**
     * 分页查询
     */
    public Page<FiAccountGroup> listAccountGroups(Long tenantId, Pageable pageable) {
        return accountGroupRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }
}
