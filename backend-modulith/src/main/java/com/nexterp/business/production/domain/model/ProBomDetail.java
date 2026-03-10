package com.nexterp.business.production.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * BOM明细
 *
 * @author NextERP
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pro_bom_detail")
public class ProBomDetail extends BaseEntity {

    /**
     * BOM ID
     */
    @Column(name = "bom_id", nullable = false)
    private Long bomId;

    /**
     * 行号
     */
    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    /**
     * 子件类型 (1-物料 2-虚拟件 3-替代件)
     */
    @Column(name = "component_type", nullable = false)
    private Integer componentType;

    /**
     * 子件物料ID
     */
    @Column(name = "component_id", nullable = false)
    private Long componentId;

    /**
     * 子件物料编码
     */
    @Column(name = "component_code", length = 50)
    private String componentCode;

    /**
     * 子件物料名称
     */
    @Column(name = "component_name", length = 100)
    private String componentName;

    /**
     * 规格型号
     */
    @Column(name = "specification", length = 200)
    private String specification;

    /**
     * 单位
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * 用量
     */
    @Column(name = "quantity", precision = 19, scale = 4)
    private BigDecimal quantity;

    /**
     * 损耗率
     */
    @Column(name = "scrap_rate", precision = 5, scale = 2)
    private BigDecimal scrapRate;

    /**
     * 有效开始日期
     */
    @Column(name = "effective_start_date")
    private java.time.LocalDate effectiveStartDate;

    /**
     * 有效结束日期
     */
    @Column(name = "effective_end_date")
    private java.time.LocalDate effectiveEndDate;

    /**
     * 是否关键件
     */
    @Column(name = "is_key_component", nullable = false)
    private Boolean isKeyComponent;

    /**
     * 是否逆向替代
     */
    @Column(name = "is_reverse_substitute", nullable = false)
    private Boolean isReverseSubstitute;

    /**
     * 替代组
     */
    @Column(name = "substitute_group", length = 50)
    private String substituteGroup;

    /**
     * 供应类型 (1-库存 2-生产 3-外协 4-采购)
     */
    @Column(name = "supply_type")
    private Integer supplyType;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 关联BOM
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", insertable = false, updatable = false)
    private ProBom bom;

    /**
     * 计算实际用量（考虑损耗率）
     *
     * @return 实际用量
     */
    public BigDecimal getActualQuantity() {
        if (quantity == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal qty = quantity;
        if (scrapRate != null && scrapRate.compareTo(BigDecimal.ZERO) > 0) {
            qty = qty.multiply(BigDecimal.ONE.add(scrapRate.divide(new BigDecimal("100"))));
        }
        return qty;
    }
}
