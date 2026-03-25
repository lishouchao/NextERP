package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * OM 对象主表 (Organization Management Object)
 * 对标 SAP HRP1000
 *
 * SAP OM 架构核心表，统一管理组织单元(O)、职位(S)、职务(C)、任务(T)、资格(Q)等对象
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_om_object", uniqueConstraints = {
    @UniqueConstraint(name = "uk_om_object_tenant_plan_type_objid",
                      columnNames = {"tenant_id", "plan_version", "object_type", "object_id"})
}, indexes = {
    @Index(name = "idx_om_object_type", columnList = "tenant_id, object_type"),
    @Index(name = "idx_om_object_validity", columnList = "tenant_id, valid_from, valid_to"),
    @Index(name = "idx_om_object_status", columnList = "tenant_id, obj_status")
})
public class HrmOrgObject extends TimeValidEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 规划版本 (01-当前计划 02-组织计划 03-测试计划 99-归档)
     * 对标 SAP PLVAR
     */
    @Column(name = "plan_version", nullable = false, length = 2)
    @Builder.Default
    private String planVersion = "01";

    /**
     * 对象类型 (O-组织单元 S-职位 C-职务 T-任务 K-成本中心 Q-资格 US-用户 P-人员)
     * 对标 SAP OTYPE
     */
    @Column(name = "object_type", nullable = false, length = 2)
    private String objectType;

    /**
     * 对象ID (8位业务编码)
     * 对标 SAP OBJID
     */
    @Column(name = "object_id", nullable = false, length = 8)
    private String objectId;

    /**
     * 语言 (ISO 639-1)
     */
    @Column(name = "language_iso", length = 2)
    @Builder.Default
    private String languageIso = "zh";

    /**
     * 对象短文本 (名称)
     * 对标 SAP STEXT
     */
    @Column(name = "short_text", nullable = false, length = 100)
    private String shortText;

    /**
     * 对象长文本 (描述)
     * 对标 SAP LTEXT
     */
    @Column(name = "long_text", columnDefinition = "TEXT")
    private String longText;

    /**
     * 对象状态 (ACTIVE/INACTIVE/PLANNED)
     */
    @Column(name = "obj_status", nullable = false, length = 10)
    @Builder.Default
    private String objStatus = "ACTIVE";

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 获取对象类型名称
     */
    public String getObjectTypeName() {
        return switch (objectType) {
            case "O" -> "组织单元";
            case "S" -> "职位";
            case "C" -> "职务";
            case "T" -> "任务";
            case "K" -> "成本中心";
            case "Q" -> "资格";
            case "US" -> "用户";
            case "P" -> "人员";
            default -> "未知";
        };
    }
}
