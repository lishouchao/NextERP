package com.nexterp.business.supply.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购订单项 (EKPO)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_purchase_order_itm")
public class MmPurchaseOrderItm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 采购订单头ID (FK) */
    @Column(name = "po_hdr_id", nullable = false)
    private Long poHdrId;

    /** 采购订单行号 */
    @Column(name = "po_item", nullable = false)
    private Integer poItem;

    /** 物料ID */
    @Column(name = "material_id")
    private Long materialId;

    /** 物料代码 */
    @Column(name = "material_code", length = 18)
    private String materialCode;

    /** 短文本 */
    @Column(name = "short_text", length = 40)
    private String shortText;

    /** 物料组 */
    @Column(name = "material_group", length = 9)
    private String materialGroup;

    /** 数量 */
    @Column(name = "quantity", precision = 13, scale = 3)
    private BigDecimal quantity;

    /** 单位 */
    @Column(name = "unit", length = 3)
    private String unit;

    /** 价格 */
    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    /** 价格单位 */
    @Column(name = "price_unit")
    @Builder.Default
    private Integer priceUnit = 1;

    /** 净价值 */
    @Column(name = "net_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netValue = BigDecimal.ZERO;

    /** 税码 */
    @Column(name = "tax_code", length = 2)
    private String taxCode;

    /** 税额 */
    @Column(name = "tax_amount", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /** 工厂ID */
    @Column(name = "plant_id")
    private Long plantId;

    /** 工厂代码 */
    @Column(name = "plant_code", length = 4)
    private String plantCode;

    /** 库位ID */
    @Column(name = "sloc_id")
    private Long slocId;

    /** 库位代码 */
    @Column(name = "sloc_code", length = 4)
    private String slocCode;

    /** 交货日期 */
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    /** 已交货数量 */
    @Column(name = "quantity_delivered", precision = 13, scale = 3)
    @Builder.Default
    private BigDecimal quantityDelivered = BigDecimal.ZERO;

    /** 已开票数量 */
    @Column(name = "quantity_invoiced", precision = 13, scale = 3)
    @Builder.Default
    private BigDecimal quantityInvoiced = BigDecimal.ZERO;

    /** 行类别 */
    @Column(name = "item_category", length = 1)
    private String itemCategory;

    /** 账户分配类别 */
    @Column(name = "acct_assignment_cat", length = 1)
    private String acctAssignmentCat;

    /** 成本中心 */
    @Column(name = "cost_center", length = 10)
    private String costCenter;

    /** 删除标记 */
    @Column(name = "deletion_flag", length = 1)
    @Builder.Default
    private String deletionFlag = "0";

    /** 采购订单头 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_hdr_id", insertable = false, updatable = false)
    private MmPurchaseOrderHdr poHdr;
}
