package com.nexterp.business.sales.domain.model;

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
 * 开票凭证头 (对标 SAP VBRK)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sd_billing_hdr")
public class SdBillingHdr extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 开票号 (VBELN) */
    @Column(name = "billing_number", nullable = false, length = 10, unique = true)
    private String billingNumber;

    /** 开票类型 (FKART): F1-发票(订单), F2-发票(交货), G2-贷项, L2-借项, S1-取消发票, S2-取消贷项 */
    @Column(name = "billing_type", nullable = false, length = 4)
    private String billingType;

    /** 销售组织 (VKORG) */
    @Column(name = "sales_org_id", nullable = false)
    private Long salesOrgId;

    /** 分销渠道 (VTWEG) */
    @Column(name = "distribution_channel", nullable = false, length = 2)
    private String distributionChannel;

    /** 产品组 (SPART) */
    @Column(name = "division", nullable = false, length = 2)
    private String division;

    /** 售达方 (KUNAG) */
    @Column(name = "sold_to_party", nullable = false)
    private Long soldToParty;

    /** 开票方 (KUNRE) */
    @Column(name = "bill_to_party")
    private Long billToParty;

    /** 付款方 (KUNRG) */
    @Column(name = "payer_party")
    private Long payerParty;

    /** 凭证日期 (ERDAT) */
    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    /** 开票日期 (FKDAT) */
    @Column(name = "billing_date", nullable = false)
    private LocalDate billingDate;

    /** 净值 (NETWR) */
    @Column(name = "net_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netValue = BigDecimal.ZERO;

    /** 税额 (MWSBK) */
    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /** 含税金额 */
    @Column(name = "gross_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal grossValue = BigDecimal.ZERO;

    /** 付款条款 (ZTERM) */
    @Column(name = "payment_term", length = 4)
    private String paymentTerm;

    /** 到期日 (FDATV) */
    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;

    /** 开票状态 (RFBSK): 01-已创建, 02-已过账, 03-已取消 */
    @Column(name = "billing_status", nullable = false, length = 2)
    @Builder.Default
    private String billingStatus = "01";

    /** 来源交货ID */
    @Column(name = "delivery_id")
    private Long deliveryId;

    /** 来源订单ID */
    @Column(name = "order_id")
    private Long orderId;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;

    /** 开票项 */
    @OneToMany(mappedBy = "billingHdr", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("itemNumber ASC")
    @Builder.Default
    private List<SdBillingItm> items = new ArrayList<>();
}
