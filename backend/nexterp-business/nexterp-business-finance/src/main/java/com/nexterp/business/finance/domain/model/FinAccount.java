package com.nexterp.business.finance.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 财务科目
 *
 * @author NextERP
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fin_account")
public class FinAccount extends TenantAwareEntity {

    /**
     * 科目编码
     */
    @Column(name = "account_code", nullable = false, length = 50)
    private String accountCode;

    /**
     * 科目名称
     */
    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    /**
     * 科目类型 (1-资产 2-负债 3-所有者权益 4-成本 5-损益)
     */
    @Column(name = "account_type", nullable = false)
    private Integer accountType;

    /**
     * 科目方向 (1-借方 2-贷方)
     */
    @Column(name = "account_direction", nullable = false)
    private Integer accountDirection;

    /**
     * 父科目ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 科目层级
     */
    @Column(name = "account_level", nullable = false)
    private Integer accountLevel;

    /**
     * 是否叶子节点
     */
    @Column(name = "is_leaf", nullable = false)
    private Boolean isLeaf;

    /**
     * 是否现金科目
     */
    @Column(name = "is_cash", nullable = false)
    private Boolean isCash;

    /**
     * 是否银行科目
     */
    @Column(name = "is_bank", nullable = false)
    private Boolean isBank;

    /**
     * 是否数量核算
     */
    @Column(name = "is_quantity", nullable = false)
    private Boolean isQuantity;

    /**
     * 数量单位
     */
    @Column(name = "quantity_unit", length = 20)
    private String quantityUnit;

    /**
     * 是否外币核算
     */
    @Column(name = "is_foreign_currency", nullable = false)
    private Boolean isForeignCurrency;

    /**
     * 币种
     */
    @Column(name = "currency", length = 10)
    private String currency;

    /**
     * 是否辅助核算
     */
    @Column(name = "is_auxiliary", nullable = false)
    private Boolean isAuxiliary;

    /**
     * 辅助核算类型 (JSON数组: ["customer", "supplier", "department", "employee", "project"])
     */
    @Column(name = "auxiliary_type", columnDefinition = "TEXT")
    private String auxiliaryType;

    /**
     * 期初余额
     */
    @Column(name = "opening_balance", precision = 19, scale = 2)
    private BigDecimal openingBalance;

    /**
     * 期初数量
     */
    @Column(name = "opening_quantity", precision = 19, scale = 4)
    private BigDecimal openingQuantity;

    /**
     * 本期借方发生额
     */
    @Column(name = "current_debit", precision = 19, scale = 2)
    private BigDecimal currentDebit;

    /**
     * 本期贷方发生额
     */
    @Column(name = "current_credit", precision = 19, scale = 2)
    private BigDecimal currentCredit;

    /**
     * 本年借方累计
     */
    @Column(name = "year_debit", precision = 19, scale = 2)
    private BigDecimal yearDebit;

    /**
     * 本年贷方累计
     */
    @Column(name = "year_credit", precision = 19, scale = 2)
    private BigDecimal yearCredit;

    /**
     * 期末余额
     */
    @Column(name = "ending_balance", precision = 19, scale = 2)
    private BigDecimal endingBalance;

    /**
     * 状态 (0-禁用 1-启用)
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 子科目列表 (不持久化)
     */
    @Transient
    private List<FinAccount> children = new ArrayList<>();

    /**
     * 获取科目类型名称
     */
    public String getAccountTypeName() {
        return switch (accountType) {
            case 1 -> "资产";
            case 2 -> "负债";
            case 3 -> "所有者权益";
            case 4 -> "成本";
            case 5 -> "损益";
            default -> "未知";
        };
    }

    /**
     * 获取科目方向名称
     */
    public String getAccountDirectionName() {
        return accountDirection == 1 ? "借" : "贷";
    }

    /**
     * 判断是否借方科目
     */
    public boolean isDebitAccount() {
        return accountDirection == 1;
    }

    /**
     * 判断是否贷方科目
     */
    public boolean isCreditAccount() {
        return accountDirection == 2;
    }
}
