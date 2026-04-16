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
 * 采购申请项
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_purchase_req_itm")
public class MmPurchaseReqItm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 采购申请头ID (FK) */
    @Column(name = "req_hdr_id", nullable = false)
    private Long reqHdrId;

    /** 采购申请行号 */
    @Column(name = "pr_item", nullable = false)
    private Integer prItem;

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

    /** 交货日期 */
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

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

    /** 成本中心 */
    @Column(name = "cost_center", length = 10)
    private String costCenter;

    /** 行类别 */
    @Column(name = "item_category", length = 1)
    private String itemCategory;

    /** 状态: 0-草稿, 1-已审批, 2-已转订单, 3-已关闭 */
    @Column(name = "status", length = 1)
    @Builder.Default
    private String status = "0";

    /** 采购申请头 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "req_hdr_id", insertable = false, updatable = false)
    private MmPurchaseReqHdr reqHdr;
}
