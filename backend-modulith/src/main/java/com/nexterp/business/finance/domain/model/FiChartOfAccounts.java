package com.nexterp.business.finance.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 科目表 (Chart of Accounts)
 * 对标 SAP SKA1-KTOPL (科目表)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fi_chart_of_accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_coa_tenant_code", columnNames = {"tenant_id", "coa_code"})
})
public class FiChartOfAccounts extends TimeValidEntity {

    /**
     * 科目表ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 科目表代码 (4位)
     * 对标 SAP SKA1-KTOPL
     */
    @Column(name = "coa_code", nullable = false, length = 4)
    private String coaCode;

    /**
     * 科目表名称
     */
    @Column(name = "coa_name", nullable = false, length = 100)
    private String coaName;

    /**
     * 科目表名称 (英文)
     */
    @Column(name = "coa_name_en", length = 100)
    private String coaNameEn;

    /**
     * 维护语言 (ISO 639-1)
     */
    @Column(name = "language_iso", length = 2)
    @Builder.Default
    private String languageIso = "zh";

    /**
     * 科目表类型
     * 01-运营科目表 02-集团科目表 03-国家科目表 04-合并科目表
     */
    @Column(name = "coa_type", length = 2, nullable = false)
    private String coaType;

    /**
     * 是否集团科目表
     */
    @Column(name = "is_group_coa", nullable = false)
    @Builder.Default
    private Boolean isGroupCoa = false;

    /**
     * 公司代码 (科目表所属公司, 国家科目表可为空)
     */
    @Column(name = "company_code", length = 4)
    private String companyCode;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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
     * 获取科目表类型名称
     */
    public String getCoaTypeName() {
        return switch (coaType) {
            case "01" -> "运营科目表";
            case "02" -> "集团科目表";
            case "03" -> "国家科目表";
            case "04" -> "合并科目表";
            default -> "未知";
        };
    }
}
