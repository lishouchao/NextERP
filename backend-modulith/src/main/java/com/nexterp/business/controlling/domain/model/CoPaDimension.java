package com.nexterp.business.controlling.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * PA维度
 * 对标: SAP CEPC
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_pa_dimension")
public class CoPaDimension extends TenantAwareEntity {

    /**
     * PA维度ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 维度代码
     */
    @Column(name = "dimension_code", nullable = false, length = 10)
    private String dimensionCode;

    /**
     * 维度名称
     */
    @Column(name = "dimension_name", nullable = false, length = 100)
    private String dimensionName;

    /**
     * 经营范围
     */
    @Column(name = "operating_concern", length = 4)
    private String operatingConcern;

    /**
     * 维度类型 (01-特征 02-值字段)
     */
    @Column(name = "dimension_type", nullable = false, length = 2)
    private String dimensionType;

    /**
     * 数据类型 (01-字符 02-数字 03-日期)
     */
    @Column(name = "data_type", nullable = false, length = 2)
    private String dataType;

    /**
     * 字段长度
     */
    @Column(name = "field_length")
    private Integer fieldLength;

    /**
     * 小数位数
     */
    @Column(name = "decimal_places")
    private Integer decimalPlaces;

    /**
     * 货币代码 (值字段)
     */
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    /**
     * 单位 (值字段)
     */
    @Column(name = "unit", length = 3)
    private String unit;

    /**
     * 关联的表/字段
     */
    @Column(name = "source_table", length = 30)
    private String sourceTable;

    /**
     * 关联字段
     */
    @Column(name = "source_field", length = 30)
    private String sourceField;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 描述
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 状态 (0-禁用 1-启用)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 获取维度类型名称
     */
    public String getDimensionTypeName() {
        return switch (dimensionType) {
            case "01" -> "特征";
            case "02" -> "值字段";
            default -> "未知";
        };
    }

    /**
     * 获取数据类型名称
     */
    public String getDataTypeName() {
        return switch (dataType) {
            case "01" -> "字符";
            case "02" -> "数字";
            case "03" -> "日期";
            default -> "未知";
        };
    }

    /**
     * 判断是否为特征维度
     */
    public boolean isCharacteristic() {
        return "01".equals(dimensionType);
    }

    /**
     * 判断是否为值字段
     */
    public boolean isValueField() {
        return "02".equals(dimensionType);
    }
}
