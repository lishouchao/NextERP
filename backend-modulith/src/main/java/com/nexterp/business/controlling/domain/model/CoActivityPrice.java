package com.nexterp.business.controlling.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 作业价格
 * 对标: SAP KBED
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_activity_price")
public class CoActivityPrice extends TenantAwareEntity {

    /**
     * 作业价格ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 成本中心ID
     */
    @Column(name = "cost_center_id", nullable = false)
    private Long costCenterId;

    /**
     * 成本中心代码
     */
    @Column(name = "cost_center_code", length = 10)
    private String costCenterCode;

    /**
     * 作业类型ID
     */
    @Column(name = "activity_type_id", nullable = false)
    private Long activityTypeId;

    /**
     * 作业类型代码
     */
    @Column(name = "activity_type_code", length = 6)
    private String activityTypeCode;

    /**
     * 有效起始日期
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    /**
     * 有效结束日期
     */
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    /**
     * 价格类型 (01-计划价格 02-实际价格)
     */
    @Column(name = "price_type", nullable = false, length = 2)
    private String priceType;

    /**
     * 作业价格
     */
    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    /**
     * 货币代码
     */
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    /**
     * 固定价格比例
     */
    @Column(name = "fixed_portion", precision = 5, scale = 2)
    private BigDecimal fixedPortion;

    /**
     * 变动价格比例
     */
    @Column(name = "variable_portion", precision = 5, scale = 2)
    private BigDecimal variablePortion;

    /**
     * 版本号
     */
    @Column(name = "version", length = 3)
    private String version;

    /**
     * 获取价格类型名称
     */
    public String getPriceTypeName() {
        return switch (priceType) {
            case "01" -> "计划价格";
            case "02" -> "实际价格";
            default -> "未知";
        };
    }
}
