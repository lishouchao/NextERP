package com.nexterp.shared.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 租户感知实体基类
 * 所有需要租户隔离的实体都应继承此基类
 *
 * @author NextERP
 */
@MappedSuperclass
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public abstract class TenantAwareEntity extends BaseEntity {

    /**
     * 租户ID
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * 租户ID感知的AuditorProvider
     * 在保存和更新时自动设置租户ID
     */
    @Component
    public static class TenantAwareAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            // 从用户上下文获取当前用户名
            return Optional.ofNullable("system");
        }
    }
}
