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
 * 采购订单头 (EKKO)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_purchase_order_hdr")
public class MmPurchaseOrderHdr extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 采购订单号 */
    @Column(name = "po_number", nullable = false, length = 10, unique = true)
    private String poNumber;

    /** 采购订单类型 */
    @Column(name = "po_type", length = 4)
    @Builder.Default
    private String poType = "NB";

    /** 供应商ID */
    @Column(name = "vendor_id")
    private Long vendorId;

    /** 供应商代码 */
    @Column(name = "vendor_code", length = 10)
    private String vendorCode;

    /** 采购组织 */
    @Column(name = "purchasing_org", length = 4)
    private String purchasingOrg;

    /** 采购组 */
    @Column(name = "purchasing_group", length = 3)
    private String purchasingGroup;

    /** 公司ID */
    @Column(name = "company_id")
    private Long companyId;

    /** 公司代码 */
    @Column(name = "company_code", length = 4)
    private String companyCode;

    /** 币种 */
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "CNY";

    /** 汇率 */
    @Column(name = "exchange_rate", precision = 9, scale = 5)
    private BigDecimal exchangeRate;

    /** 凭证日期 */
    @Column(name = "document_date")
    private LocalDate documentDate;

    /** 有效期自 */
    @Column(name = "valid_from")
    private LocalDate validFrom;

    /** 有效期至 */
    @Column(name = "valid_to")
    private LocalDate validTo;

    /** 付款条件 */
    @Column(name = "terms_of_payment", length = 4)
    private String termsOfPayment;

    /** 国际贸易条件1 */
    @Column(name = "incoterms1", length = 3)
    private String incoterms1;

    /** 国际贸易条件2 */
    @Column(name = "incoterms2", length = 28)
    private String incoterms2;

    /** 状态: 0-草稿, 1-已审批, 2-已发货, 3-已完成, 4-已关闭 */
    @Column(name = "status", length = 1)
    @Builder.Default
    private String status = "0";

    /** 释放状态: 0-未释放, 1-释放中, 2-已释放 */
    @Column(name = "release_status", length = 1)
    @Builder.Default
    private String releaseStatus = "0";

    /** 头文本 */
    @Column(name = "header_text", length = 50)
    private String headerText;

    /** 总净价值 */
    @Column(name = "total_net_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalNetValue = BigDecimal.ZERO;

    /** 总税额 */
    @Column(name = "total_tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalTaxAmount = BigDecimal.ZERO;

    /** 总毛价值 */
    @Column(name = "total_gross_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalGrossValue = BigDecimal.ZERO;

    /** 采购订单项 */
    @OneToMany(mappedBy = "poHdr", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MmPurchaseOrderItm> items = new ArrayList<>();
}
