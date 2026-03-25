package com.nexterp.business.finance.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * 科目组 (Account Group)
 * 对标 SAP SKA1-KTOKS (科目组)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fi_account_group", uniqueConstraints = {
    @UniqueConstraint(name = "uk_accgrp_tenant_code", columnNames = {"tenant_id", "group_code"})
})
public class FiAccountGroup extends TenantAwareEntity {

    /**
     * 科目组ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 科目组代码 (4位)
     * 对标 SAP SKA1-KTOKS
     */
    @Column(name = "group_code", nullable = false, length = 4)
    private String groupCode;

    /**
     * 科目组名称
     */
    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    /**
     * 科目组名称 (英文)
     */
    @Column(name = "group_name_en", length = 100)
    private String groupNameEn;

    /**
     * 所属科目表ID
     */
    @Column(name = "coa_id")
    private Long coaId;

    /**
     * 科目类型 (AS-资产 LI-负债 EQ-权益 RE-收入 EX-费用)
     */
    @Column(name = "account_type", nullable = false, length = 2)
    private String accountType;

    /**
     * 科目编号范围-起始
     */
    @Column(name = "account_range_from", length = 10)
    private String accountRangeFrom;

    /**
     * 科目编号范围-结束
     */
    @Column(name = "account_range_to", length = 10)
    private String accountRangeTo;

    /**
     * 字段状态组 (对标 SAP SKB1-FSTAG)
     */
    @Column(name = "field_status_group", length = 4)
    private String fieldStatusGroup;

    /**
     * 是否统驭科目组
     */
    @Column(name = "is_reconciliation", nullable = false)
    @Builder.Default
    private Boolean isReconciliation = false;

    /**
     * 统驭科目类型 (D-客户 K-供应商 A-资产 M-物料)
     */
    @Column(name = "reconcil_account_type", length = 1)
    private String reconcilAccountType;

    /**
     * 父科目组ID (支持层级结构)
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 层级深度
     */
    @Column(name = "group_level")
    @Builder.Default
    private Integer groupLevel = 1;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 状态 (1-启用 0-禁用)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取科目类型名称
     */
    public String getAccountTypeName() {
        return switch (accountType) {
            case "AS" -> "资产";
            case "LI" -> "负债";
            case "EQ" -> "权益";
            case "RE" -> "收入";
            case "EX" -> "费用";
            default -> "未知";
        };
    }
}
