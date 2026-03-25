package com.nexterp.business.controlling.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 成本要素
 * 对标: SAP CSKS/CSKU
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_cost_element")
public class CoCostElement extends TenantAwareEntity {

    /**
     * 成本要素ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 成本要素代码
     */
    @Column(name = "element_code", nullable = false, length = 10)
    private String elementCode;

    /**
     * 成本要素名称
     */
    @Column(name = "element_name", nullable = false, length = 100)
    private String elementName;

    /**
     * 成本要素类型 (01-初级成本要素 02-次级成本要素)
     */
    @Column(name = "element_type", nullable = false, length = 2)
    private String elementType;

    /**
     * 成本要素类别 (01-初级成本 02-收入 11-折旧 21-内部作业分配 41-间接费用 42-订单结算)
     */
    @Column(name = "element_category", nullable = false, length = 2)
    private String elementCategory;

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
     * 关联的GL科目代码 (初级成本要素)
     */
    @Column(name = "gl_account_code", length = 10)
    private String glAccountCode;

    /**
     * 成本控制范围
     */
    @Column(name = "controlling_area", length = 4)
    private String controllingArea;

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
     * 获取成本要素类型名称
     */
    public String getElementTypeName() {
        return switch (elementType) {
            case "01" -> "初级成本要素";
            case "02" -> "次级成本要素";
            default -> "未知";
        };
    }

    /**
     * 获取成本要素类别名称
     */
    public String getElementCategoryName() {
        return switch (elementCategory) {
            case "01" -> "初级成本";
            case "02" -> "收入";
            case "11" -> "折旧";
            case "21" -> "内部作业分配";
            case "41" -> "间接费用";
            case "42" -> "订单结算";
            default -> "未知";
        };
    }

    /**
     * 判断是否为初级成本要素
     */
    public boolean isPrimaryElement() {
        return "01".equals(elementType);
    }

    /**
     * 判断是否为次级成本要素
     */
    public boolean isSecondaryElement() {
        return "02".equals(elementType);
    }

    /**
     * 判断当前是否有效
     */
    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(validFrom) && !now.isAfter(validTo);
    }
}
