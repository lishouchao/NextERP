package com.nexterp.platform.tenant.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 租户实体
 *
 * @author NextERP
 */
@Entity
@Table(name = "sys_tenant")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysTenant extends BaseEntity {

    /**
     * 租户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 租户编码
     */
    @Column(name = "tenant_code", nullable = false, unique = true, length = 50)
    private String tenantCode;

    /**
     * 租户名称
     */
    @Column(name = "tenant_name", nullable = false, length = 100)
    private String tenantName;

    /**
     * 联系人
     */
    @Column(name = "contact_name", length = 50)
    private String contactName;

    /**
     * 联系电话
     */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    /**
     * 租户地址
     */
    @Column(name = "address", length = 255)
    private String address;

    /**
     * 租户状态 (0-禁用 1-正常)
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 过期时间
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 最大用户数
     */
    @Column(name = "max_users")
    private Integer maxUsers;

    /**
     * 最大存储空间(MB)
     */
    @Column(name = "max_storage")
    private Long maxStorage;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 租户配置 (JSON格式)
     */
    @Column(name = "config", columnDefinition = "TEXT")
    private String config;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
