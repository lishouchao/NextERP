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
 * 销售订单项 (对标 SAP VBAP)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sd_sales_order_itm")
public class SdSalesOrderItm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单头ID */
    @Column(name = "order_hdr_id", nullable = false)
    private Long orderHdrId;

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

    /** 订购数量 (KWMENG) */
    @Column(name = "ordered_qty", nullable = false, precision = 13, scale = 3)
    private BigDecimal orderedQty;

    /** 已交货数量 (LFIMG) */
    @Column(name = "delivered_qty", precision = 13, scale = 3)
    @Builder.Default
    private BigDecimal deliveredQty = BigDecimal.ZERO;

    /** 已开票数量 (FKLIMG) */
    @Column(name = "invoiced_qty", precision = 13, scale = 3)
    @Builder.Default
    private BigDecimal invoicedQty = BigDecimal.ZERO;

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

    /** 工厂 (WERKS) */
    @Column(name = "plant_id")
    private Long plantId;

    /** 库存地点 (LGORT) */
    @Column(name = "sloc_id")
    private Long slocId;

    /** 项目类别 (PSTYV): TAN-标准, TANN-免费, TAS-服务, TAB-包裹, TAP-定制 */
    @Column(name = "item_category", length = 4)
    @Builder.Default
    private String itemCategory = "TAN";

    /** 订单头关联 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_hdr_id", insertable = false, updatable = false)
    private SdSalesOrderHdr orderHdr;
}
