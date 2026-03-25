package com.nexterp.business.finance.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 科目余额表 (Account Balance)
 * 对标 SAP GLT0/FAGLFLEXT
 *
 * 使用独立字段存储12期间借贷发生额，便于索引和查询
 *
 * @author NextERP
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fi_account_balance", uniqueConstraints = {
    @UniqueConstraint(name = "uk_balance_company_account_year_curr",
                      columnNames = {"company_id", "account_id", "fiscal_year", "currency_code"})
})
public class FiAccountBalance {

    /**
     * 余额ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * 公司ID
     */
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    /**
     * 科目ID
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /**
     * 科目代码 (冗余)
     */
    @Column(name = "account_code", length = 10)
    private String accountCode;

    /**
     * 会计年度
     */
    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    /**
     * 货币代码
     */
    @Column(name = "currency_code", nullable = false, length = 3)
    @Builder.Default
    private String currencyCode = "CNY";

    // ==================== 期间借贷发生额 (12个期间 × 2字段 = 24个字段) ====================

    /** 期间1 - 借方 */
    @Column(name = "period_01_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period01Debit = BigDecimal.ZERO;

    /** 期间1 - 贷方 */
    @Column(name = "period_01_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period01Credit = BigDecimal.ZERO;

    /** 期间2 - 借方 */
    @Column(name = "period_02_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period02Debit = BigDecimal.ZERO;

    /** 期间2 - 贷方 */
    @Column(name = "period_02_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period02Credit = BigDecimal.ZERO;

    /** 期间3 - 借方 */
    @Column(name = "period_03_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period03Debit = BigDecimal.ZERO;

    /** 期间3 - 贷方 */
    @Column(name = "period_03_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period03Credit = BigDecimal.ZERO;

    /** 期间4 - 借方 */
    @Column(name = "period_04_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period04Debit = BigDecimal.ZERO;

    /** 期间4 - 贷方 */
    @Column(name = "period_04_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period04Credit = BigDecimal.ZERO;

    /** 期间5 - 借方 */
    @Column(name = "period_05_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period05Debit = BigDecimal.ZERO;

    /** 期间5 - 贷方 */
    @Column(name = "period_05_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period05Credit = BigDecimal.ZERO;

    /** 期间6 - 借方 */
    @Column(name = "period_06_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period06Debit = BigDecimal.ZERO;

    /** 期间6 - 贷方 */
    @Column(name = "period_06_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period06Credit = BigDecimal.ZERO;

    /** 期间7 - 借方 */
    @Column(name = "period_07_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period07Debit = BigDecimal.ZERO;

    /** 期间7 - 贷方 */
    @Column(name = "period_07_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period07Credit = BigDecimal.ZERO;

    /** 期间8 - 借方 */
    @Column(name = "period_08_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period08Debit = BigDecimal.ZERO;

    /** 期间8 - 贷方 */
    @Column(name = "period_08_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period08Credit = BigDecimal.ZERO;

    /** 期间9 - 借方 */
    @Column(name = "period_09_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period09Debit = BigDecimal.ZERO;

    /** 期间9 - 贷方 */
    @Column(name = "period_09_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period09Credit = BigDecimal.ZERO;

    /** 期间10 - 借方 */
    @Column(name = "period_10_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period10Debit = BigDecimal.ZERO;

    /** 期间10 - 贷方 */
    @Column(name = "period_10_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period10Credit = BigDecimal.ZERO;

    /** 期间11 - 借方 */
    @Column(name = "period_11_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period11Debit = BigDecimal.ZERO;

    /** 期间11 - 贷方 */
    @Column(name = "period_11_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period11Credit = BigDecimal.ZERO;

    /** 期间12 - 借方 */
    @Column(name = "period_12_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period12Debit = BigDecimal.ZERO;

    /** 期间12 - 贷方 */
    @Column(name = "period_12_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal period12Credit = BigDecimal.ZERO;

    // ==================== 年度累计 ====================

    /** 年度借方累计 */
    @Column(name = "year_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal yearDebit = BigDecimal.ZERO;

    /** 年度贷方累计 */
    @Column(name = "year_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal yearCredit = BigDecimal.ZERO;

    /** 期初余额 */
    @Column(name = "opening_balance", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;

    /** 期末余额 */
    @Column(name = "ending_balance", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal endingBalance = BigDecimal.ZERO;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 乐观锁版本号
     */
    @Version
    @Column(name = "lock_version")
    @Builder.Default
    private Integer lockVersion = 0;

    /**
     * 获取指定期间的借方发生额
     */
    public BigDecimal getPeriodDebit(int period) {
        return switch (period) {
            case 1 -> period01Debit;
            case 2 -> period02Debit;
            case 3 -> period03Debit;
            case 4 -> period04Debit;
            case 5 -> period05Debit;
            case 6 -> period06Debit;
            case 7 -> period07Debit;
            case 8 -> period08Debit;
            case 9 -> period09Debit;
            case 10 -> period10Debit;
            case 11 -> period11Debit;
            case 12 -> period12Debit;
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 获取指定期间的贷方发生额
     */
    public BigDecimal getPeriodCredit(int period) {
        return switch (period) {
            case 1 -> period01Credit;
            case 2 -> period02Credit;
            case 3 -> period03Credit;
            case 4 -> period04Credit;
            case 5 -> period05Credit;
            case 6 -> period06Credit;
            case 7 -> period07Credit;
            case 8 -> period08Credit;
            case 9 -> period09Credit;
            case 10 -> period10Credit;
            case 11 -> period11Credit;
            case 12 -> period12Credit;
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 获取指定期间的净额 (借-贷)
     */
    public BigDecimal getPeriodNetAmount(int period) {
        BigDecimal debit = getPeriodDebit(period);
        BigDecimal credit = getPeriodCredit(period);
        if (debit == null) debit = BigDecimal.ZERO;
        if (credit == null) credit = BigDecimal.ZERO;
        return debit.subtract(credit);
    }

    /**
     * 计算期末余额
     */
    public void calculateEndingBalance() {
        BigDecimal yearNet = (yearDebit != null ? yearDebit : BigDecimal.ZERO)
                .subtract(yearCredit != null ? yearCredit : BigDecimal.ZERO);
        this.endingBalance = (openingBalance != null ? openingBalance : BigDecimal.ZERO).add(yearNet);
    }

    /**
     * 计算年度累计
     */
    public void calculateYearTotals() {
        this.yearDebit = sumAllDebits();
        this.yearCredit = sumAllCredits();
    }

    private BigDecimal sumAllDebits() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 1; i <= 12; i++) {
            BigDecimal debit = getPeriodDebit(i);
            if (debit != null) {
                total = total.add(debit);
            }
        }
        return total;
    }

    private BigDecimal sumAllCredits() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 1; i <= 12; i++) {
            BigDecimal credit = getPeriodCredit(i);
            if (credit != null) {
                total = total.add(credit);
            }
        }
        return total;
    }
}
