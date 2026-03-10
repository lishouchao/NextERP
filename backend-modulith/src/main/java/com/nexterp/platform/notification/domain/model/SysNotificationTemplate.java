package com.nexterp.platform.notification.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知模板实体
 *
 * @author NextERP
 */
@Entity
@Table(name = "sys_notification_template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysNotificationTemplate extends BaseEntity {

    /**
     * 模板ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id")
    private Long tenantId;

    /**
     * 模板编码
     */
    @Column(name = "template_code", nullable = false, length = 50)
    private String templateCode;

    /**
     * 模板名称
     */
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    /**
     * 通知类型 (system-系统 email-邮件 sms-短信 push-推送)
     */
    @Column(name = "notification_type", nullable = false, length = 20)
    private String notificationType;

    /**
     * 标题模板
     */
    @Column(name = "title_template", length = 200)
    private String titleTemplate;

    /**
     * 内容模板
     */
    @Column(name = "content_template", columnDefinition = "TEXT")
    private String contentTemplate;

    /**
     * 变量说明
     */
    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables;

    /**
     * 状态 (0-禁用 1-启用)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
}
