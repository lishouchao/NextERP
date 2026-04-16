package com.nexterp.business.sales.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 开票凭证项 (对标 SAP VBRP)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sd_billing_itm")
public class SdBillingItm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 开票头ID */
    @Column(name = "billing_hdr_id", nullable = false)
    private Long billingHdrId;

    /** 行号 (POSNR) */
    @Column(name = "item_number", nullable = false)
    private Integer itemNumber;

    /** 物料ID (MATNR) */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /** 物料编码 */
    @Column(name = "material_code", nullable = false, length = 18)
    private String materialCode;

    /** 物料描述 (ARKTX) */
    @Column(name = "description", length = 100)
    private String description;

    /** 开票数量 (FKIMG) */
    @Column(name = "billed_qty", nullable = false, precision = 13, scale = 3)
    private BigDecimal billedQty;

    /** 销售单位 (VRKME) */
    @Column(name = "sales_unit", nullable = false, length = 3)
    private String salesUnit;

    /** 净单价 (NETPR) */
    @Column(name = "net_price", precision = 15, scale = 2)
    private BigDecimal netPrice;

    /** 净值 (NETWR) */
    @Column(name = "net_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netValue = BigDecimal.ZERO;

    /** 税码 (MWSKZ) */
    @Column(name = "tax_code", length = 2)
    private String taxCode;

    /** 税额 */
    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /** 含税金额 */
    @Column(name = "gross_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal grossValue = BigDecimal.ZERO;

    /** 成本金额 */
    @Column(name = "cost_value", precision = 15, scale = 2)
    private BigDecimal costValue;

    /** 来源交货单 (VGBEL) */
    @Column(name = "delivery_id")
    private Long deliveryId;

    /** 来源交货项 (VGPOS) */
    @Column(name = "delivery_item_id")
    private Long deliveryItemId;

    /** 开票头关联 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_hdr_id", insertable = false, updatable = false)
    private SdBillingHdr billingHdr;
}
