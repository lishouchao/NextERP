package com.nexterp.platform.auth.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体
 *
 * @author NextERP
 */
@Entity
@Table(name = "sys_user", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_email", columnList = "email")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysUser extends TenantAwareEntity {

    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    /**
     * 密码
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 真实姓名
     */
    @Column(name = "real_name", length = 100)
    private String realName;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 性别 (0-女 1-男 2-未知)
     */
    @Column(name = "gender")
    private Integer gender;

    /**
     * 头像URL
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * 用户状态 (0-禁用 1-正常)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 用户类型 (0-系统用户 1-租户用户)
     */
    @Column(name = "user_type", nullable = false)
    @Builder.Default
    private Integer userType = 1;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * 部门ID
     */
    @Column(name = "dept_id")
    private Long deptId;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    /**
     * 密码最后修改时间
     */
    @Column(name = "pwd_update_time")
    private LocalDateTime pwdUpdateTime;

    /**
     * 账号过期时间
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 用户角色关联
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "sys_user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<SysRole> roles = new HashSet<>();

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * 检查用户是否启用
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 检查账号是否过期
     *
     * @return 是否过期
     */
    public boolean isAccountExpired() {
        return expireTime != null && expireTime.isBefore(LocalDateTime.now());
    }

    /**
     * 检查密码是否需要修改
     * 默认密码需要90天修改一次
     *
     * @return 是否需要修改
     */
    public boolean isPasswordExpired() {
        if (pwdUpdateTime == null) {
            return true;
        }
        LocalDateTime expireTime = pwdUpdateTime.plusDays(90);
        return expireTime.isBefore(LocalDateTime.now());
    }
}
