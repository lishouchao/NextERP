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
 * 物料凭证项 (对标 SAP MSEG)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_material_doc_itm")
public class MmMaterialDocItm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 凭证头ID */
    @Column(name = "doc_hdr_id", nullable = false)
    private Long docHdrId;

    /** 行项目 (ZEILE) */
    @Column(name = "line_item", nullable = false)
    private Integer lineItem;

    /** 物料ID (MATNR) */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /** 物料编码 */
    @Column(name = "material_code", nullable = false, length = 18)
    private String materialCode;

    /** 工厂ID (WERKS) */
    @Column(name = "plant_id")
    private Long plantId;

    /** 工厂代码 */
    @Column(name = "plant_code", length = 4)
    private String plantCode;

    /** 库存地点ID (LGORT) */
    @Column(name = "sloc_id")
    private Long slocId;

    /** 库存地点代码 */
    @Column(name = "sloc_code", length = 4)
    private String slocCode;

    /** 批次 (CHARG) */
    @Column(name = "batch", length = 10)
    private String batch;

    /** 移动类型 (BWART) */
    @Column(name = "movement_type", nullable = false, length = 3)
    private String movementType;

    /** 库存类型 (INSMK): 1-非限制, 2-质检, 3-冻结 */
    @Column(name = "stock_type", length = 1)
    private String stockType;

    /** 数量 (MENGE) */
    @Column(name = "quantity", precision = 13, scale = 3)
    private BigDecimal quantity;

    /** 单位 (MEINS) */
    @Column(name = "unit", length = 3)
    private String unit;

    /** 金额 (DMBTR) */
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    /** 采购订单号 (EBELN) */
    @Column(name = "purchase_order", length = 10)
    private String purchaseOrder;

    /** 采购订单项号 (EBELP) */
    @Column(name = "po_item")
    private Integer poItem;

    /** 供应商ID (LIFNR) */
    @Column(name = "vendor_id")
    private Long vendorId;

    /** 客户ID (KUNNR) */
    @Column(name = "customer_id")
    private Long customerId;

    /** 销售订单号 (KDAUF) */
    @Column(name = "sales_order", length = 10)
    private String salesOrder;

    /** 成本中心 (KOSTL) */
    @Column(name = "cost_center", length = 10)
    private String costCenter;

    /** 移动原因 (GRUND) */
    @Column(name = "reason_for_movement", length = 4)
    private String reasonForMovement;

    /** 行项目文本 (SGTXT) */
    @Column(name = "item_text", length = 50)
    private String itemText;

    /** 凭证头关联 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_hdr_id", insertable = false, updatable = false)
    private MmMaterialDocHdr docHdr;
}
