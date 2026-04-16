package com.nexterp.business.supply.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 发票项 (对标 SAP RSEG)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_invoice_itm")
public class MmInvoiceItm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发票头ID */
    @Column(name = "invoice_hdr_id", nullable = false)
    private Long invoiceHdrId;

    /** 行号 (BUZEI) */
    @Column(name = "line_item", nullable = false)
    private Integer lineItem;

    /** 采购订单号 (EBELN) */
    @Column(name = "purchase_order", length = 10)
    private String purchaseOrder;

    /** 采购订单项号 (EBELP) */
    @Column(name = "po_item")
    private Integer poItem;

    /** 物料ID (MATNR) */
    @Column(name = "material_id")
    private Long materialId;

    /** 物料编码 */
    @Column(name = "material_code", length = 18)
    private String materialCode;

    /** 短文本 (TXZ01) */
    @Column(name = "short_text", length = 40)
    private String shortText;

    /** 数量 (MENGE) */
    @Column(name = "quantity", precision = 13, scale = 3)
    private BigDecimal quantity;

    /** 单位 (MEINS) */
    @Column(name = "unit", length = 3)
    private String unit;

    /** 单价 (PREIS) */
    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    /** 净额 (NETWR) */
    @Column(name = "net_amount", precision = 15, scale = 2)
    private BigDecimal netAmount;

    /** 税码 (MWSKZ) */
    @Column(name = "tax_code", length = 2)
    private String taxCode;

    /** 税额 */
    @Column(name = "tax_amount", precision = 14, scale = 2)
    private BigDecimal taxAmount;

    /** 工厂ID (WERKS) */
    @Column(name = "plant_id")
    private Long plantId;

    /** 工厂代码 */
    @Column(name = "plant_code", length = 4)
    private String plantCode;

    /** 成本中心 (KOSTL) */
    @Column(name = "cost_center", length = 10)
    private String costCenter;

    /** 行文本 (SGTXT) */
    @Column(name = "item_text", length = 50)
    private String itemText;

    /** 发票头关联 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_hdr_id", insertable = false, updatable = false)
    private MmInvoiceHdr invoiceHdr;
}
