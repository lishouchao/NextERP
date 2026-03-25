package com.nexterp.business.finance.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 凭证项 (Journal Entry Item)
 * 对标 SAP BSEG，按 fiscal_year 分区
 *
 * @author NextERP
 */
@Data
@Entity
@IdClass(com.nexterp.business.finance.domain.model.JournalEntryItemId.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "fi_journal_entry_itm", indexes = {
    @Index(name = "idx_je_itm_header", columnList = "fiscal_year, header_id"),
    @Index(name = "idx_je_itm_account", columnList = "tenant_id, account_id"),
    @Index(name = "idx_je_itm_partner", columnList = "tenant_id, partner_id")
})
public class FiJournalEntryItm {

    /**
     * 凭证项ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 会计年度 (分区键，冗余存储)
     */
    @Id
    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * 凭证头ID
     */
    @Column(name = "header_id", nullable = false)
    private Long headerId;

    /**
     * 行号
     */
    @Column(name = "line_item", nullable = false)
    private Integer lineItem;

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
     * 科目名称 (冗余)
     */
    @Column(name = "account_name", length = 100)
    private String accountName;

    /**
     * 业务伙伴ID
     */
    @Column(name = "partner_id")
    private Long partnerId;

    /**
     * 业务伙伴类型 (C-客户 V-供应商 E-员工)
     */
    @Column(name = "partner_type", length = 1)
    private String partnerType;

    /**
     * 业务伙伴代码 (冗余)
     */
    @Column(name = "partner_code", length = 20)
    private String partnerCode;

    /**
     * 业务伙伴名称 (冗余)
     */
    @Column(name = "partner_name", length = 100)
    private String partnerName;

    /**
     * 借贷标识 (D-借 C-贷)
     */
    @Column(name = "debit_credit", nullable = false, length = 1)
    private String debitCredit;

    /**
     * 本位币金额
     */
    @Column(name = "amount", nullable = false, precision = 23, scale = 2)
    private BigDecimal amount;

    /**
     * 凭证币金额
     */
    @Column(name = "amount_dc", precision = 23, scale = 2)
    private BigDecimal amountDc;

    /**
     * 货币代码
     */
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    /**
     * 汇率
     */
    @Column(name = "exchange_rate", precision = 12, scale = 6)
    private BigDecimal exchangeRate;

    /**
     * 数量
     */
    @Column(name = "quantity", precision = 23, scale = 4)
    private BigDecimal quantity;

    /**
     * 单位
     */
    @Column(name = "quantity_unit", length = 3)
    private String quantityUnit;

    /**
     * 单价
     */
    @Column(name = "unit_price", precision = 23, scale = 4)
    private BigDecimal unitPrice;

    /**
     * 成本中心ID
     */
    @Column(name = "cost_center_id")
    private Long costCenterId;

    /**
     * 成本中心代码
     */
    @Column(name = "cost_center_code", length = 10)
    private String costCenterCode;

    /**
     * 利润中心ID
     */
    @Column(name = "profit_center_id")
    private Long profitCenterId;

    /**
     * 利润中心代码
     */
    @Column(name = "profit_center_code", length = 10)
    private String profitCenterCode;

    /**
     * 内部订单号
     */
    @Column(name = "internal_order", length = 12)
    private String internalOrder;

    /**
     * 项目ID
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 项目代码
     */
    @Column(name = "project_code", length = 24)
    private String projectCode;

    /**
     * 业务范围
     */
    @Column(name = "business_area", length = 4)
    private String businessArea;

    /**
     * 报表段
     */
    @Column(name = "segment", length = 10)
    private String segment;

    /**
     * 税码
     */
    @Column(name = "tax_code", length = 2)
    private String taxCode;

    /**
     * 税额
     */
    @Column(name = "tax_amount", precision = 23, scale = 2)
    private BigDecimal taxAmount;

    /**
     * 计税基数
     */
    @Column(name = "tax_base_amount", precision = 23, scale = 2)
    private BigDecimal taxBaseAmount;

    /**
     * 付款条款
     */
    @Column(name = "payment_term", length = 4)
    private String paymentTerm;

    /**
     * 基准日期
     */
    @Column(name = "baseline_date")
    private LocalDate baselineDate;

    /**
     * 到期日
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * 清算日期
     */
    @Column(name = "clearing_date")
    private LocalDate clearingDate;

    /**
     * 清算凭证ID
     */
    @Column(name = "clearing_doc_id")
    private Long clearingDocId;

    /**
     * 清算凭证年度
     */
    @Column(name = "clearing_fiscal_year")
    private Integer clearingFiscalYear;

    /**
     * 行项目文本
     */
    @Column(name = "item_text", length = 100)
    private String itemText;

    /**
     * 分配号
     */
    @Column(name = "assignment", length = 18)
    private String assignment;

    /**
     * 参考键1
     */
    @Column(name = "reference_key_1", length = 20)
    private String referenceKey1;

    /**
     * 参考键2
     */
    @Column(name = "reference_key_2", length = 20)
    private String referenceKey2;

    /**
     * 参考键3
     */
    @Column(name = "reference_key_3", length = 20)
    private String referenceKey3;

    /**
     * 辅助核算-客户ID
     */
    @Column(name = "aux_customer_id")
    private Long auxCustomerId;

    /**
     * 辅助核算-供应商ID
     */
    @Column(name = "aux_supplier_id")
    private Long auxSupplierId;

    /**
     * 辅助核算-部门ID
     */
    @Column(name = "aux_dept_id")
    private Long auxDeptId;

    /**
     * 辅助核算-员工ID
     */
    @Column(name = "aux_employee_id")
    private Long auxEmployeeId;

    /**
     * 辅助核算-项目ID
     */
    @Column(name = "aux_project_id")
    private Long auxProjectId;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 乐观锁版本号
     */
    @Version
    @Column(name = "lock_version")
    @Builder.Default
    private Integer lockVersion = 0;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * 判断是否借方
     */
    public boolean isDebit() {
        return "D".equals(debitCredit);
    }

    /**
     * 判断是否贷方
     */
    public boolean isCredit() {
        return "C".equals(debitCredit);
    }

    /**
     * 判断是否已清算
     */
    public boolean isCleared() {
        return clearingDate != null;
    }
}
