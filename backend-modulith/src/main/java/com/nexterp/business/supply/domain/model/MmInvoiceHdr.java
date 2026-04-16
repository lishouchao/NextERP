package com.nexterp.business.supply.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 发票头 (对标 SAP RBKP)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_invoice_hdr")
public class MmInvoiceHdr extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发票号 (BELNR) */
    @Column(name = "invoice_number", nullable = false, length = 10, unique = true)
    private String invoiceNumber;

    /** 会计年度 (GJAHR) */
    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    /** 发票类型 (BLART): RE-发票, G2-贷项 */
    @Column(name = "invoice_type", nullable = false, length = 2)
    private String invoiceType;

    /** 凭证日期 (BLDAT) */
    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    /** 过账日期 (BUDAT) */
    @Column(name = "posting_date", nullable = false)
    private LocalDate postingDate;

    /** 供应商ID (LIFNR) */
    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    /** 供应商代码 */
    @Column(name = "vendor_code", length = 10)
    private String vendorCode;

    /** 公司ID (BUKRS) */
    @Column(name = "company_id")
    private Long companyId;

    /** 公司代码 */
    @Column(name = "company_code", length = 4)
    private String companyCode;

    /** 币种 (WAERS) */
    @Column(name = "currency", length = 3)
    private String currency;

    /** 供应商发票号 (XBLNR) */
    @Column(name = "supplier_invoice", length = 16)
    private String supplierInvoice;

    /** 供应商发票日期 */
    @Column(name = "supplier_invoice_date")
    private LocalDate supplierInvoiceDate;

    /** 总额 (RBWR) */
    @Column(name = "gross_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal grossAmount = BigDecimal.ZERO;

    /** 净额 (NETWR) */
    @Column(name = "net_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO;

    /** 税额 (MWSBK) */
    @Column(name = "tax_amount", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /** 折扣金额 (SKFBT) */
    @Column(name = "discount_amount", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** 付款条件 (ZTERM) */
    @Column(name = "payment_terms", length = 4)
    private String paymentTerms;

    /** 到期日 (NETDT) */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /** 状态: 0-草稿, 1-已校验, 2-已过账, 3-已冲销 */
    @Column(name = "status", length = 1)
    @Builder.Default
    private String status = "0";

    /** 阻塞原因 (ZLSPR) */
    @Column(name = "blocking_reason", length = 2)
    private String blockingReason;

    /** 发票项 */
    @OneToMany(mappedBy = "invoiceHdr", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineItem ASC")
    @Builder.Default
    private List<MmInvoiceItm> items = new ArrayList<>();
}
