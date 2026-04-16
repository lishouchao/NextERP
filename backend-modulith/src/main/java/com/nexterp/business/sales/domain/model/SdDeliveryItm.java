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
 * 交货单项 (对标 SAP LIPS)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sd_delivery_itm")
public class SdDeliveryItm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 交货头ID */
    @Column(name = "delivery_hdr_id", nullable = false)
    private Long deliveryHdrId;

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

    /** 交货数量 (LFIMG) */
    @Column(name = "delivery_qty", nullable = false, precision = 13, scale = 3)
    private BigDecimal deliveryQty;

    /** 拣配数量 (PIKMG) */
    @Column(name = "picked_qty", precision = 13, scale = 3)
    @Builder.Default
    private BigDecimal pickedQty = BigDecimal.ZERO;

    /** 销售单位 (VRKME) */
    @Column(name = "sales_unit", nullable = false, length = 3)
    private String salesUnit;

    /** 基本单位 (MEINS) */
    @Column(name = "base_unit", length = 3)
    private String baseUnit;

    /** 批次号 (CHARG) */
    @Column(name = "batch_number", length = 10)
    private String batchNumber;

    /** 工厂 (WERKS) */
    @Column(name = "plant_id")
    private Long plantId;

    /** 库存地点 (LGORT) */
    @Column(name = "sloc_id")
    private Long slocId;

    /** 来源订单 (VGBEL) */
    @Column(name = "order_id")
    private Long orderId;

    /** 来源订单项 (VGPOS) */
    @Column(name = "order_item_id")
    private Long orderItemId;

    /** 交货头关联 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_hdr_id", insertable = false, updatable = false)
    private SdDeliveryHdr deliveryHdr;
}
