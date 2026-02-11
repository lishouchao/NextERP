package com.nexterp.business.finance.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 财务凭证分录
 *
 * @author NextERP
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fin_voucher_entry")
public class FinVoucherEntry extends BaseEntity {

    /**
     * 凭证ID
     */
    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    /**
     * 行号
     */
    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    /**
     * 科目ID
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /**
     * 科目编码
     */
    @Column(name = "account_code", length = 50)
    private String accountCode;

    /**
     * 科目名称
     */
    @Column(name = "account_name", length = 100)
    private String accountName;

    /**
     * 摘要
     */
    @Column(name = "summary", length = 500)
    private String summary;

    /**
     * 借方金额
     */
    @Column(name = "debit_amount", precision = 19, scale = 2)
    private BigDecimal debitAmount;

    /**
     * 贷方金额
     */
    @Column(name = "credit_amount", precision = 19, scale = 2)
    private BigDecimal creditAmount;

    /**
     * 数量
     */
    @Column(name = "quantity", precision = 19, scale = 4)
    private BigDecimal quantity;

    /**
     * 单价
     */
    @Column(name = "unit_price", precision = 19, scale = 4)
    private BigDecimal unitPrice;

    /**
     * 币种
     */
    @Column(name = "currency", length = 10)
    private String currency;

    /**
     * 原币金额
     */
    @Column(name = "foreign_amount", precision = 19, scale = 2)
    private BigDecimal foreignAmount;

    /**
     * 汇率
     */
    @Column(name = "exchange_rate", precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    /**
     * 辅助核算 - 客户ID
     */
    @Column(name = "aux_customer_id")
    private Long auxCustomerId;

    /**
     * 辅助核算 - 供应商ID
     */
    @Column(name = "aux_supplier_id")
    private Long auxSupplierId;

    /**
     * 辅助核算 - 部门ID
     */
    @Column(name = "aux_dept_id")
    private Long auxDeptId;

    /**
     * 辅助核算 - 员工ID
     */
    @Column(name = "aux_employee_id")
    private Long auxEmployeeId;

    /**
     * 辅助核算 - 项目ID
     */
    @Column(name = "aux_project_id")
    private Long auxProjectId;

    /**
     * 关联凭证
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", insertable = false, updatable = false)
    private FinVoucher voucher;

    /**
     * 获取金额
     *
     * @return 金额（借方为正，贷方为负）
     */
    public BigDecimal getAmount() {
        if (debitAmount != null && debitAmount.compareTo(BigDecimal.ZERO) > 0) {
            return debitAmount;
        }
        if (creditAmount != null && creditAmount.compareTo(BigDecimal.ZERO) > 0) {
            return creditAmount.negate();
        }
        return BigDecimal.ZERO;
    }

    /**
     * 判断是否借方分录
     */
    public boolean isDebit() {
        return debitAmount != null && debitAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否贷方分录
     */
    public boolean isCredit() {
        return creditAmount != null && creditAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}
