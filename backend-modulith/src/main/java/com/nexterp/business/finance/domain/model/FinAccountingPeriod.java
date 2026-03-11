package com.nexterp.business.finance.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 会计期间
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fin_accounting_period")
public class FinAccountingPeriod extends TenantAwareEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 会计年度
     */
    @Column(name = "accounting_year", nullable = false)
    private Integer accountingYear;

    /**
     * 会计期间
     */
    @Column(name = "accounting_period", nullable = false, length = 10)
    private String accountingPeriod;

    /**
     * 期间起始日期
     */
    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    /**
     * 期间结束日期
     */
    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    /**
     * 期间状态 (0-未开启 1-已开启 2-已结账)
     */
    @Column(name = "period_status", nullable = false)
    private Integer periodStatus;

    /**
     * 凭证起始号
     */
    @Column(name = "voucher_start_no")
    private Integer voucherStartNo;

    /**
     * 凭证结束号
     */
    @Column(name = "voucher_end_no")
    private Integer voucherEndNo;

    /**
     * 凭证数量
     */
    @Column(name = "voucher_count")
    private Integer voucherCount;

    /**
     * 借方发生额合计
     */
    @Column(name = "total_debit", precision = 19, scale = 2)
    private BigDecimal totalDebit;

    /**
     * 贷方发生额合计
     */
    @Column(name = "total_credit", precision = 19, scale = 2)
    private BigDecimal totalCredit;

    /**
     * 结账人ID
     */
    @Column(name = "closed_by_id")
    private Long closedById;

    /**
     * 结账人姓名
     */
    @Column(name = "closed_by_name", length = 50)
    private String closedByName;

    /**
     * 结账时间
     */
    @Column(name = "closed_at")
    private java.time.LocalDateTime closedAt;

    /**
     * 反结账人ID
     */
    @Column(name = "reopened_by_id")
    private Long reopenedById;

    /**
     * 反结账人姓名
     */
    @Column(name = "reopened_by_name", length = 50)
    private String reopenedByName;

    /**
     * 反结账时间
     */
    @Column(name = "reopened_at")
    private java.time.LocalDateTime reopenedAt;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取期间状态名称
     */
    public String getPeriodStatusName() {
        return switch (periodStatus) {
            case 0 -> "未开启";
            case 1 -> "已开启";
            case 2 -> "已结账";
            default -> "未知";
        };
    }

    /**
     * 判断是否已开启
     */
    public boolean isOpened() {
        return periodStatus >= 1;
    }

    /**
     * 判断是否已结账
     */
    public boolean isClosed() {
        return periodStatus == 2;
    }

    /**
     * 判断是否可以录入凭证
     */
    public boolean canInputVoucher() {
        return periodStatus == 1;
    }

    /**
     * 判断是否可以结账
     */
    public boolean canClose() {
        return periodStatus == 1;
    }

    /**
     * 判断是否可以反结账
     */
    public boolean canReopen() {
        return periodStatus == 2;
    }

    /**
     * 获取年月
     */
    public YearMonth getYearMonth() {
        return YearMonth.of(accountingYear, Integer.parseInt(accountingPeriod.split("-")[1]));
    }
}
