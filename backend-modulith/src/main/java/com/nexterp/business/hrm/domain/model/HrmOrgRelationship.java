package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OM 对象关系表 (Organization Management Relationship)
 * 对标 SAP HRP1001
 *
 * 存储任意对象间的关系，如：
 * - 组织单元之间的上下级关系 (002/003)
 * - 职位与组织单元的归属关系 (003)
 * - 人员与职位的担任关系 (008)
 * - 直线汇报关系 (009)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_om_relationship", uniqueConstraints = {
    @UniqueConstraint(name = "uk_om_rel_plan_type_a_b_vf",
                      columnNames = {"tenant_id", "plan_version", "relation_type",
                                     "object_type_a", "object_id_a", "object_type_b", "object_id_b", "valid_from"})
}, indexes = {
    @Index(name = "idx_om_rel_a_object", columnList = "tenant_id, object_type_a, object_id_a, valid_from, valid_to"),
    @Index(name = "idx_om_rel_b_object", columnList = "tenant_id, object_type_b, object_id_b, valid_from, valid_to"),
    @Index(name = "idx_om_rel_type", columnList = "tenant_id, relation_type, valid_from")
})
public class HrmOrgRelationship extends TimeValidEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 规划版本 (01-当前计划)
     * 对标 SAP PLVAR
     */
    @Column(name = "plan_version", nullable = false, length = 2)
    @Builder.Default
    private String planVersion = "01";

    /**
     * 关系类型
     * 002-隶属于 003-包含 004-担任 005-下级 007-描述 008-持有者
     * 009-直线汇报 010-成本中心分配 011-职位专指 012-拥有任务
     * 013-人员到职位 014-下级组织 015-组织到成本中心 020-矩阵汇报
     * 030-拥有资格 031-需要资格 040-继任者 041-参与了 042-参考职位
     * 045-代理人 050-团队领导 A/B-A/B关系
     *
     * 对标 SAP RELAT
     */
    @Column(name = "relation_type", nullable = false, length = 3)
    private String relationType;

    /**
     * 前向对象类型 (A方向)
     * 对标 SAP OTYPE (A sign)
     */
    @Column(name = "object_type_a", nullable = false, length = 2)
    private String objectTypeA;

    /**
     * 前向对象ID (A方向)
     * 对标 SAP OBJID (A sign)
     */
    @Column(name = "object_id_a", nullable = false, length = 8)
    private String objectIdA;

    /**
     * 前向对象内码 (关联 HrmOrgObject.id)
     */
    @Column(name = "object_pk_a")
    private Long objectPkA;

    /**
     * 后向对象类型 (B方向)
     * 对标 SAP OTYPE (B sign)
     */
    @Column(name = "object_type_b", nullable = false, length = 2)
    private String objectTypeB;

    /**
     * 后向对象ID (B方向)
     * 对标 SAP OBJID (B sign)
     */
    @Column(name = "object_id_b", nullable = false, length = 8)
    private String objectIdB;

    /**
     * 后向对象内码 (关联 HrmOrgObject.id)
     */
    @Column(name = "object_pk_b")
    private Long objectPkB;

    /**
     * 比例 (用于分摊，如矩阵汇报50%)
     */
    @Column(name = "percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal percentage = BigDecimal.valueOf(100);

    /**
     * 优先级 (用于多汇报线)
     */
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 1;

    /**
     * 是否主要关系
     */
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = true;

    /**
     * 获取关系类型名称
     */
    public String getRelationTypeName() {
        return switch (relationType) {
            case "002" -> "隶属于";
            case "003" -> "包含";
            case "004" -> "担任";
            case "005" -> "下级";
            case "007" -> "描述";
            case "008" -> "持有者";
            case "009" -> "直线汇报";
            case "010" -> "成本中心分配";
            case "011" -> "职位专指";
            case "012" -> "拥有任务";
            case "013" -> "人员到职位";
            case "014" -> "下级组织";
            case "015" -> "组织到成本中心";
            case "020" -> "矩阵汇报";
            case "030" -> "拥有资格";
            case "031" -> "需要资格";
            case "040" -> "继任者";
            case "041" -> "参与了";
            case "042" -> "参考职位";
            case "045" -> "代理人";
            case "050" -> "团队领导";
            case "A/B" -> "A/B关系";
            default -> "未知";
        };
    }
}
