package com.nexterp.business.supply.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 物料销售数据 (对标 SAP MVKE)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_material_sales")
public class MmMaterialSales extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物料ID (FK to MmMaterial) */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /** 物料编码 */
    @Column(name = "material_number", nullable = false, length = 18)
    private String materialNumber;

    /** 销售组织ID */
    @Column(name = "sales_org_id", nullable = false)
    private Long salesOrgId;

    /** 销售组织代码 */
    @Column(name = "sales_org_code", nullable = false, length = 4)
    private String salesOrgCode;

    /** 分销渠道 */
    @Column(name = "distr_channel", nullable = false, length = 2)
    private String distrChannel;

    /** 销售状态 */
    @Column(name = "status_sales", length = 1)
    @Builder.Default
    private String statusSales = "A";

    /** 交货工厂 */
    @Column(name = "delivering_plant", length = 4)
    private String deliveringPlant;

    /** 销售单位 */
    @Column(name = "sales_unit", length = 3)
    private String salesUnit;

    /** 最小订单量 */
    @Column(name = "min_order_qty", precision = 13, scale = 3)
    private BigDecimal minOrderQty;

    /** 最小交货量 */
    @Column(name = "min_deliv_qty", precision = 13, scale = 3)
    private BigDecimal minDelivQty;

    /** 定价组 */
    @Column(name = "pricing_group", length = 2)
    private String pricingGroup;

    /** 项目类别组 */
    @Column(name = "item_category_group", length = 4)
    private String itemCategoryGroup;

    /** 科目分配组 */
    @Column(name = "account_assignment_group", length = 2)
    private String accountAssignmentGroup;

    /** 产品层次 */
    @Column(name = "product_hierarchy", length = 18)
    private String productHierarchy;

    /** 物料定价组 */
    @Column(name = "material_pricing_group", length = 2)
    private String materialPricingGroup;

    /** 物料主记录 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", insertable = false, updatable = false)
    private MmMaterial material;
}
