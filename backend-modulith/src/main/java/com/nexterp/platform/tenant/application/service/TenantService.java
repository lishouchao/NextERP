package com.nexterp.platform.tenant.application.service;

import com.nexterp.platform.auth.domain.repository.SysUserRepository;
import com.nexterp.platform.tenant.domain.model.SysTenant;
import com.nexterp.platform.tenant.domain.repository.SysTenantRepository;
import com.nexterp.platform.tenant.dto.request.TenantCreateRequest;
import com.nexterp.platform.tenant.dto.request.TenantUpdateRequest;
import com.nexterp.platform.tenant.dto.response.TenantResponse;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户管理服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final SysTenantRepository tenantRepository;
    private final SysUserRepository userRepository;

    /**
     * 创建租户
     *
     * @param request 创建请求
     * @return 租户响应
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantResponse createTenant(TenantCreateRequest request) {
        // 检查租户编码是否已存在
        if (tenantRepository.existsByTenantCode(request.getTenantCode())) {
            throw new BusinessException("租户编码已存在");
        }

        // 构建租户实体
        SysTenant tenant = SysTenant.builder()
                .tenantCode(request.getTenantCode())
                .tenantName(request.getTenantName())
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .address(request.getAddress())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .expireTime(request.getExpireTime())
                .maxUsers(request.getMaxUsers() != null ? request.getMaxUsers() : 100)
                .maxStorage(request.getMaxStorage() != null ? request.getMaxStorage() : 10240L)  // 默认10GB
                .remark(request.getRemark())
                .config(request.getConfig())
                .build();

        SysTenant savedTenant = tenantRepository.save(tenant);
        log.info("创建租户成功: tenantCode={}", request.getTenantCode());
        return toResponse(savedTenant);
    }

    /**
     * 更新租户
     *
     * @param id 租户ID
     * @param request 更新请求
     * @return 租户响应
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantResponse updateTenant(Long id, TenantUpdateRequest request) {
        SysTenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new BusinessException("租户不存在"));

        // 更新基本信息
        if (request.getTenantName() != null) {
            tenant.setTenantName(request.getTenantName());
        }
        if (request.getContactName() != null) {
            tenant.setContactName(request.getContactName());
        }
        if (request.getContactPhone() != null) {
            tenant.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            tenant.setContactEmail(request.getContactEmail());
        }
        if (request.getAddress() != null) {
            tenant.setAddress(request.getAddress());
        }
        if (request.getStatus() != null) {
            // 检查是否禁用租户
            if (request.getStatus() == 0 && tenant.getStatus() == 1) {
                log.info("禁用租户: id={}, tenantCode={}", id, tenant.getTenantCode());
            }
            tenant.setStatus(request.getStatus());
        }
        if (request.getExpireTime() != null) {
            tenant.setExpireTime(request.getExpireTime());
        }
        if (request.getMaxUsers() != null) {
            tenant.setMaxUsers(request.getMaxUsers());
        }
        if (request.getMaxStorage() != null) {
            tenant.setMaxStorage(request.getMaxStorage());
        }
        if (request.getRemark() != null) {
            tenant.setRemark(request.getRemark());
        }
        if (request.getConfig() != null) {
            tenant.setConfig(request.getConfig());
        }

        SysTenant updatedTenant = tenantRepository.save(tenant);
        log.info("更新租户成功: id={}", id);
        return toResponse(updatedTenant);
    }

    /**
     * 删除租户
     *
     * @param id 租户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenant(Long id) {
        SysTenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new BusinessException("租户不存在"));

        // 检查是否有用户关联
        long userCount = userRepository.countByTenantIdAndIsDeletedFalse(id);
        if (userCount > 0) {
            throw new BusinessException("该租户仍有用户关联，无法删除");
        }

        // 软删除
        tenant.setIsDeleted(true);
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantRepository.save(tenant);

        log.info("删除租户成功: id={}", id);
    }

    /**
     * 获取租户详情
     *
     * @param id 租户ID
     * @return 租户响应
     */
    public TenantResponse getTenantById(Long id) {
        SysTenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new BusinessException("租户不存在"));
        return toResponse(tenant);
    }

    /**
     * 根据租户编码获取租户
     *
     * @param tenantCode 租户编码
     * @return 租户响应
     */
    public TenantResponse getTenantByCode(String tenantCode) {
        SysTenant tenant = tenantRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new BusinessException("租户不存在"));
        return toResponse(tenant);
    }

    /**
     * 分页查询租户
     *
     * @param tenantCode 租户编码（模糊查询）
     * @param tenantName 租户名称（模糊查询）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    public PageResult<TenantResponse> listTenants(
            String tenantCode,
            String tenantName,
            Integer status,
            Pageable pageable) {
        // 构建查询条件
        Specification<SysTenant> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 未删除
            predicates.add(cb.equal(root.get("isDeleted"), false));

            // 租户编码模糊查询
            if (tenantCode != null && !tenantCode.isEmpty()) {
                predicates.add(cb.like(root.get("tenantCode"), "%" + tenantCode + "%"));
            }

            // 租户名称模糊查询
            if (tenantName != null && !tenantName.isEmpty()) {
                predicates.add(cb.like(root.get("tenantName"), "%" + tenantName + "%"));
            }

            // 状态
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SysTenant> page = tenantRepository.findAll(spec, pageable);

        return PageResult.<TenantResponse>builder()
                .records(page.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 获取所有启用状态的租户
     *
     * @return 租户列表
     */
    public List<TenantResponse> listActiveTenants() {
        List<SysTenant> tenants = tenantRepository.findAllActive();
        return tenants.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 启用/禁用租户
     *
     * @param id 租户ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTenantStatus(Long id, Integer status) {
        SysTenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new BusinessException("租户不存在"));

        tenant.setStatus(status);
        tenantRepository.save(tenant);

        log.info("更新租户状态成功: id={}, status={}", id, status);
    }

    /**
     * 检查租户是否过期
     *
     * @param tenantId 租户ID
     * @return 是否过期
     */
    public boolean isTenantExpired(Long tenantId) {
        SysTenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("租户不存在"));

        if (tenant.getExpireTime() == null) {
            return false;
        }

        return LocalDateTime.now().isAfter(tenant.getExpireTime());
    }

    /**
     * 检查租户是否可用
     *
     * @param tenantId 租户ID
     * @return 是否可用
     */
    public boolean isTenantAvailable(Long tenantId) {
        SysTenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("租户不存在"));

        return tenant.getStatus() == 1 && !isTenantExpired(tenantId);
    }

    /**
     * 获取即将过期的租户
     *
     * @param days 天数
     * @return 租户列表
     */
    public List<TenantResponse> getExpiringTenants(int days) {
        LocalDateTime expireTime = LocalDateTime.now().plusDays(days);
        List<SysTenant> tenants = tenantRepository.findExpiringTenants(expireTime);
        return tenants.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应对象
     *
     * @param tenant 租户实体
     * @return 租户响应
     */
    private TenantResponse toResponse(SysTenant tenant) {
        // 获取当前用户数
        int currentUserCount = (int) userRepository.countByTenantIdAndIsDeletedFalse(tenant.getId());

        return TenantResponse.builder()
                .id(tenant.getId())
                .tenantCode(tenant.getTenantCode())
                .tenantName(tenant.getTenantName())
                .contactName(tenant.getContactName())
                .contactPhone(tenant.getContactPhone())
                .contactEmail(tenant.getContactEmail())
                .address(tenant.getAddress())
                .status(tenant.getStatus())
                .expireTime(tenant.getExpireTime())
                .maxUsers(tenant.getMaxUsers())
                .maxStorage(tenant.getMaxStorage())
                .currentUserCount(currentUserCount)
                .currentStorageUsed(0L)  // TODO: 实现存储使用统计
                .remark(tenant.getRemark())
                .config(tenant.getConfig())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
