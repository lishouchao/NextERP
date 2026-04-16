package com.nexterp.business.sales.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 交货单头 (对标 SAP LIKP)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sd_delivery_hdr")
public class SdDeliveryHdr extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 交货单号 (VBELN) */
    @Column(name = "delivery_number", nullable = false, length = 10, unique = true)
    private String deliveryNumber;

    /** 交货类型 (LFART): LF-外向交货, LR-退货交货, LO-无订单交货, NL-补货交货 */
    @Column(name = "delivery_type", nullable = false, length = 4)
    private String deliveryType;

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
    @Column(name = "ship_to_party", nullable = false)
    private Long shipToParty;

    /** 凭证日期 (ERDAT) */
    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    /** 计划发货日期 (LFDAT) */
    @Column(name = "planned_gi_date")
    private LocalDate plannedGiDate;

    /** 实际发货日期 (WADAT) */
    @Column(name = "actual_gi_date")
    private LocalDate actualGiDate;

    /** 装运点 (VSTEL) */
    @Column(name = "shipping_point", length = 4)
    private String shippingPoint;

    /** 交货状态: 01-未处理, 02-拣配中, 03-已拣配, 04-已发货 */
    @Column(name = "delivery_status", nullable = false, length = 2)
    @Builder.Default
    private String deliveryStatus = "01";

    /** 拣配状态 (KOSTA): A-未拣配, B-部分拣配, C-完全拣配 */
    @Column(name = "picking_status", nullable = false, length = 2)
    @Builder.Default
    private String pickingStatus = "A";

    /** 发货状态 (WBSTA): A-未发货, B-已发货 */
    @Column(name = "gi_status", nullable = false, length = 2)
    @Builder.Default
    private String giStatus = "A";

    /** 来源订单ID */
    @Column(name = "order_id")
    private Long orderId;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;

    /** 交货单项 */
    @OneToMany(mappedBy = "deliveryHdr", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("itemNumber ASC")
    @Builder.Default
    private List<SdDeliveryItm> items = new ArrayList<>();
}
