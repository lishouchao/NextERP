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
 * 物料库存 (对标 SAP MARD)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_stock", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stock_material_plant_sloc_tenant",
                columnNames = {"material_id", "plant_id", "sloc_id", "tenant_id"})
})
public class MmStock extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物料ID */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /** 物料编码 */
    @Column(name = "material_code", nullable = false, length = 18)
    private String materialCode;

    /** 工厂ID */
    @Column(name = "plant_id", nullable = false)
    private Long plantId;

    /** 工厂代码 */
    @Column(name = "plant_code", length = 4)
    private String plantCode;

    /** 库存地点ID */
    @Column(name = "sloc_id", nullable = false)
    private Long slocId;

    /** 库存地点代码 */
    @Column(name = "sloc_code", length = 4)
    private String slocCode;

    /** 非限制库存 (LABST) */
    @Column(name = "unrestricted_stock", precision = 13, scale = 3)
    @Builder.Default
    private BigDecimal unrestrictedStock = BigDecimal.ZERO;

    /** 质检库存 (INSME) */
    @Column(name = "quality_stock", precision = 13, scale = 3)
    @Builder.Default
    private BigDecimal qualityStock = BigDecimal.ZERO;

    /** 冻结库存 (SPEME) */
    @Column(name = "blocked_stock", precision = 13, scale = 3)
    @Builder.Default
    private BigDecimal blockedStock = BigDecimal.ZERO;

    /** 非限制库存价值 */
    @Column(name = "unrestricted_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal unrestrictedValue = BigDecimal.ZERO;

    /** 质检库存价值 */
    @Column(name = "quality_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal qualityValue = BigDecimal.ZERO;

    /** 冻结库存价值 */
    @Column(name = "blocked_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal blockedValue = BigDecimal.ZERO;

    /** 币种 (WAERS) */
    @Column(name = "currency", length = 3)
    private String currency;
}
