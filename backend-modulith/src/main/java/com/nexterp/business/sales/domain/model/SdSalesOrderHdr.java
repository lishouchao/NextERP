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
 * 销售订单头 (对标 SAP VBAK)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sd_sales_order_hdr")
public class SdSalesOrderHdr extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单号 (VBELN) */
    @Column(name = "order_number", nullable = false, length = 10, unique = true)
    private String orderNumber;

    /** 订单类型 (AUART): OR-标准订单, CR-贷项, DR-借项, RE-退货, NB-无库存, CS-现金 */
    @Column(name = "order_type", nullable = false, length = 4)
    private String orderType;

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

    /** 送达方 (KUNWE) */
    @Column(name = "ship_to_party")
    private Long shipToParty;

    /** 开票方 (KUNRE) */
    @Column(name = "bill_to_party")
    private Long billToParty;

    /** 付款方 (KUNRG) */
    @Column(name = "payer_party")
    private Long payerParty;

    /** 凭证日期 (AUDAT) */
    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    /** 要求交货日期 (VDATU) */
    @Column(name = "requested_delivery_date")
    private LocalDate requestedDeliveryDate;

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

    /** 订单状态: 01-创建, 02-审批, 03-处理中, 04-已完成, 05-关闭 */
    @Column(name = "order_status", nullable = false, length = 2)
    @Builder.Default
    private String orderStatus = "01";

    /** 交货状态 (LFSTA): A-未交货, B-部分交货, C-已交货 */
    @Column(name = "delivery_status", nullable = false, length = 2)
    @Builder.Default
    private String deliveryStatus = "A";

    /** 开票状态 (FKSTK): A-未开票, B-部分开票, C-已开票 */
    @Column(name = "billing_status", nullable = false, length = 2)
    @Builder.Default
    private String billingStatus = "A";

    /** 客户采购单号 */
    @Column(name = "purchase_order", length = 35)
    private String purchaseOrder;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;

    /** 订单项 */
    @OneToMany(mappedBy = "orderHdr", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("itemNumber ASC")
    @Builder.Default
    private List<SdSalesOrderItm> items = new ArrayList<>();
}
