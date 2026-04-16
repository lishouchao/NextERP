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

/**
 * 物料评估数据 (对标 SAP MBEW)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_material_valuation")
public class MmMaterialValuation extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物料ID (FK to MmMaterial) */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /** 物料编码 */
    @Column(name = "material_number", nullable = false, length = 18)
    private String materialNumber;

    /** 评估范围(工厂) */
    @Column(name = "valuation_area", nullable = false, length = 4)
    private String valuationArea;

    /** 评估类型 */
    @Column(name = "valuation_type", length = 10)
    private String valuationType;

    /** 价格控制 (S:标准/V:移动平均) */
    @Column(name = "price_control", length = 1)
    private String priceControl;

    /** 移动平均价 */
    @Column(name = "moving_price", precision = 12, scale = 2)
    private BigDecimal movingPrice;

    /** 标准价 */
    @Column(name = "standard_price", precision = 12, scale = 2)
    private BigDecimal standardPrice;

    /** 评估类 */
    @Column(name = "valuation_class", length = 4)
    private String valuationClass;

    /** 价格单位 */
    @Column(name = "price_unit")
    @Builder.Default
    private Integer priceUnit = 1;

    /** 未来价格 */
    @Column(name = "future_price", precision = 12, scale = 2)
    private BigDecimal futurePrice;

    /** 未来价格生效日期 */
    @Column(name = "future_price_valid_from")
    private LocalDate futurePriceValidFrom;

    /** 生效日期 */
    @Column(name = "valid_from")
    private LocalDate validFrom;

    /** 币种 */
    @Column(name = "currency", length = 3)
    private String currency;

    /** 物料主记录 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", insertable = false, updatable = false)
    private MmMaterial material;
}
