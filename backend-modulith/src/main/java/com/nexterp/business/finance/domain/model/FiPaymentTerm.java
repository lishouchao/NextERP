package com.nexterp.business.finance.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 付款条件 (Payment Term)
 * 对标 SAP T052 (付款条件)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fi_payment_term", uniqueConstraints = {
    @UniqueConstraint(name = "uk_payterm_tenant_code", columnNames = {"tenant_id", "payment_term_code"})
})
public class FiPaymentTerm extends TenantAwareEntity {

    /**
     * 付款条件ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 付款条件代码 (4位)
     * 对标 SAP T052-ZTERM
     */
    @Column(name = "payment_term_code", nullable = false, length = 4)
    private String paymentTermCode;

    /**
     * 付款条件名称
     */
    @Column(name = "payment_term_name", nullable = false, length = 100)
    private String paymentTermName;

    /**
     * 付款条件名称 (英文)
     */
    @Column(name = "payment_term_name_en", length = 100)
    private String paymentTermNameEn;

    /**
     * 付款条件类型
     * 01-立即付款 02-固定天数 03-月结天数 04-分期付款
     */
    @Column(name = "term_type", length = 2, nullable = false)
    private String termType;

    /**
     * 基准日期类型
     * 01-凭证日期 02-过账日期 03-收货日期 04-发票日期
     */
    @Column(name = "baseline_date_type", length = 2)
    @Builder.Default
    private String baselineDateType = "02";

    /**
     * 固定天数 (从基准日期起)
     */
    @Column(name = "fixed_days")
    private Integer fixedDays;

    /**
     * 月结日 (如: 月结30天 = 30)
     */
    @Column(name = "month_end_days")
    private Integer monthEndDays;

    /**
     * 现金折扣天数1
     */
    @Column(name = "discount_days1")
    private Integer discountDays1;

    /**
     * 现金折扣率1 (%)
     */
    @Column(name = "discount_rate1", precision = 5, scale = 2)
    private BigDecimal discountRate1;

    /**
     * 现金折扣天数2
     */
    @Column(name = "discount_days2")
    private Integer discountDays2;

    /**
     * 现金折扣率2 (%)
     */
    @Column(name = "discount_rate2", precision = 5, scale = 2)
    private BigDecimal discountRate2;

    /**
     * 净付款天数 (无折扣最后期限)
     */
    @Column(name = "net_payment_days")
    private Integer netPaymentDays;

    /**
     * 科目ID (现金折扣)
     */
    @Column(name = "discount_account_id")
    private Long discountAccountId;

    /**
     * 科目代码 (现金折扣)
     */
    @Column(name = "discount_account_code", length = 10)
    private String discountAccountCode;

    /**
     * 是否默认付款条件
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * 适用范围 (01-供应商 02-客户 03-全部)
     */
    @Column(name = "apply_scope", length = 2)
    @Builder.Default
    private String applyScope = "03";

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
     * 获取付款条件类型名称
     */
    public String getTermTypeName() {
        return switch (termType) {
            case "01" -> "立即付款";
            case "02" -> "固定天数";
            case "03" -> "月结天数";
            case "04" -> "分期付款";
            default -> "未知";
        };
    }

    /**
     * 获取基准日期类型名称
     */
    public String getBaselineDateTypeName() {
        return switch (baselineDateType) {
            case "01" -> "凭证日期";
            case "02" -> "过账日期";
            case "03" -> "收货日期";
            case "04" -> "发票日期";
            default -> "未知";
        };
    }

    /**
     * 计算到期日
     *
     * @param baselineDate 基准日期
     * @return 到期日
     */
    public java.time.LocalDate calculateDueDate(java.time.LocalDate baselineDate) {
        if (baselineDate == null) {
            return null;
        }

        if ("01".equals(termType)) {
            // 立即付款
            return baselineDate;
        } else if ("02".equals(termType) && fixedDays != null) {
            // 固定天数
            return baselineDate.plusDays(fixedDays);
        } else if ("03".equals(termType) && monthEndDays != null) {
            // 月结天数
            return baselineDate.plusMonths(1).withDayOfMonth(monthEndDays);
        }

        return netPaymentDays != null ? baselineDate.plusDays(netPaymentDays) : baselineDate;
    }
}
