package com.nexterp.business.supply.domain.model;

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
 * 物料主记录 (对标 SAP MARA)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_material")
public class MmMaterial extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物料编码 */
    @Column(name = "material_number", nullable = false, length = 18, unique = true)
    private String materialNumber;

    /** 物料类型 (ROH/HALB/FERT/VERP/HIBE/DIEN/NLAG) */
    @Column(name = "material_type", nullable = false, length = 4)
    private String materialType;

    /** 行业部门 */
    @Column(name = "industry_sector", length = 1)
    private String industrySector;

    /** 物料组 */
    @Column(name = "material_group", length = 9)
    private String materialGroup;

    /** 物料描述 */
    @Column(name = "description", length = 40)
    private String description;

    /** 英文描述 */
    @Column(name = "description_en", length = 40)
    private String descriptionEn;

    /** 基本单位 */
    @Column(name = "base_uom", length = 3)
    private String baseUom;

    /** 订单单位 */
    @Column(name = "order_uom", length = 3)
    private String orderUom;

    /** 毛重 */
    @Column(name = "gross_weight", precision = 13, scale = 3)
    private BigDecimal grossWeight;

    /** 净重 */
    @Column(name = "net_weight", precision = 13, scale = 3)
    private BigDecimal netWeight;

    /** 重量单位 */
    @Column(name = "weight_unit", length = 3)
    private String weightUnit;

    /** 体积 */
    @Column(name = "volume", precision = 13, scale = 3)
    private BigDecimal volume;

    /** 体积单位 */
    @Column(name = "volume_unit", length = 3)
    private String volumeUnit;

    /** 条码 */
    @Column(name = "ean_upc", length = 18)
    private String eanUpc;

    /** 旧物料号 */
    @Column(name = "old_mat_no", length = 40)
    private String oldMatNo;

    /** 产品组 */
    @Column(name = "division", length = 2)
    private String division;

    /** 产品层次 */
    @Column(name = "product_hierarchy", length = 18)
    private String productHierarchy;

    /** 跨工厂状态 */
    @Column(name = "cross_plant_status", length = 1)
    @Builder.Default
    private String crossPlantStatus = "A";

    /** 生效日期 */
    @Column(name = "valid_from")
    private LocalDate validFrom;

    /** 失效日期 */
    @Column(name = "valid_to")
    private LocalDate validTo;

    /** 物料工厂数据 */
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MmMaterialPlant> plantData = new ArrayList<>();

    /** 物料销售数据 */
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MmMaterialSales> salesData = new ArrayList<>();

    /** 物料评估数据 */
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MmMaterialValuation> valuationData = new ArrayList<>();
}
