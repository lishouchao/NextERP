package com.nexterp.business.finance.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 总账科目 (GL Account)
 * 对标 SAP SKA1 + SKB1 (科目主数据)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fi_gl_account", uniqueConstraints = {
    @UniqueConstraint(name = "uk_gl_account_tenant_coa_code", columnNames = {"tenant_id", "coa_id", "account_code"})
})
public class FiGlAccount extends TimeValidEntity {

    /**
     * 科目ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属科目表ID
     */
    @Column(name = "coa_id", nullable = false)
    private Long coaId;

    /**
     * 科目代码 (10位)
     * 对标 SAP SKA1-SAKNR
     */
    @Column(name = "account_code", nullable = false, length = 10)
    private String accountCode;

    /**
     * 科目名称
     */
    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    /**
     * 科目名称 (英文)
     */
    @Column(name = "account_name_en", length = 100)
    private String accountNameEn;

    /**
     * 科目组ID
     */
    @Column(name = "account_group_id")
    private Long accountGroupId;

    /**
     * 科目类型 (AS-资产 LI-负债 EQ-权益 RE-收入 EX-费用)
     */
    @Column(name = "account_type", nullable = false, length = 2)
    private String accountType;

    /**
     * 科目分类 (CA-流动资产 NCA-非流动资产 CL-流动负债 NCL-非流动负债 EQ-权益 OR-收入 EX-费用 CO-成本)
     */
    @Column(name = "account_class", length = 3)
    private String accountClass;

    /**
     * 余额方向 (D-借方 C-贷方)
     * 对标 SAP SKA1-XSALH
     */
    @Column(name = "balance_indicator", nullable = false, length = 1)
    private String balanceIndicator;

    /**
     * 集团科目代码
     * 对标 SAP SKA1-GLACCOUNT_TYPE
     */
    @Column(name = "group_account", length = 10)
    private String groupAccount;

    /**
     * 是否可记账
     */
    @Column(name = "is_postable", nullable = false)
    @Builder.Default
    private Boolean isPostable = true;

    /**
     * 是否统驭科目 (对标 SAP SKB1-MITKZ)
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
     * 是否资产负债表科目
     */
    @Column(name = "is_balance_sheet", nullable = false)
    @Builder.Default
    private Boolean isBalanceSheet = false;

    /**
     * 是否损益科目
     */
    @Column(name = "is_profit_and_loss", nullable = false)
    @Builder.Default
    private Boolean isProfitAndLoss = false;

    /**
     * 字段状态组 (对标 SAP SKB1-FSTAG)
     */
    @Column(name = "field_status_group", length = 4)
    private String fieldStatusGroup;

    /**
     * 未清项管理 (对标 SAP SKB1-XOPVZ)
     */
    @Column(name = "open_item_mgmt", nullable = false)
    @Builder.Default
    private Boolean openItemMgmt = false;

    /**
     * 行项目管理 (对标 SAP SKB1-XGKON)
     */
    @Column(name = "line_item_mgmt", nullable = false)
    @Builder.Default
    private Boolean lineItemMgmt = true;

    /**
     * 现金流量分类
     */
    @Column(name = "cash_flow_type", length = 2)
    private String cashFlowType;

    /**
     * 税分类
     */
    @Column(name = "tax_category", length = 2)
    private String taxCategory;

    /**
     * 功能范围 (对标 SAP FAGLFLEXT)
     */
    @Column(name = "functional_area", length = 16)
    private String functionalArea;

    /**
     * 父科目ID (支持科目层级)
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 科目层级
     */
    @Column(name = "account_level")
    @Builder.Default
    private Integer accountLevel = 1;

    /**
     * 是否叶子节点
     */
    @Column(name = "is_leaf", nullable = false)
    @Builder.Default
    private Boolean isLeaf = true;

    /**
     * 是否现金科目
     */
    @Column(name = "is_cash", nullable = false)
    @Builder.Default
    private Boolean isCash = false;

    /**
     * 是否银行科目
     */
    @Column(name = "is_bank", nullable = false)
    @Builder.Default
    private Boolean isBank = false;

    /**
     * 是否数量核算
     */
    @Column(name = "is_quantity", nullable = false)
    @Builder.Default
    private Boolean isQuantity = false;

    /**
     * 数量单位
     */
    @Column(name = "quantity_unit", length = 3)
    private String quantityUnit;

    /**
     * 是否外币核算
     */
    @Column(name = "is_foreign_currency", nullable = false)
    @Builder.Default
    private Boolean isForeignCurrency = false;

    /**
     * 货币代码
     */
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

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
     * 子科目列表 (不持久化)
     */
    @Transient
    @Builder.Default
    private List<FiGlAccount> children = new ArrayList<>();

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

    /**
     * 获取科目分类名称
     */
    public String getAccountClassName() {
        return switch (accountClass) {
            case "CA" -> "流动资产";
            case "NCA" -> "非流动资产";
            case "CL" -> "流动负债";
            case "NCL" -> "非流动负债";
            case "EQ" -> "权益";
            case "OR" -> "收入";
            case "EX" -> "费用";
            case "CO" -> "成本";
            default -> "未知";
        };
    }

    /**
     * 获取余额方向名称
     */
    public String getBalanceIndicatorName() {
        return "D".equals(balanceIndicator) ? "借方" : "贷方";
    }

    /**
     * 判断是否借方科目
     */
    public boolean isDebitAccount() {
        return "D".equals(balanceIndicator);
    }

    /**
     * 判断是否贷方科目
     */
    public boolean isCreditAccount() {
        return "C".equals(balanceIndicator);
    }
}
