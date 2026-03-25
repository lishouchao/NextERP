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
 * 作业类型
 * 对标: SAP CSLA
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_activity_type")
public class CoActivityType extends TenantAwareEntity {

    /**
     * 作业类型ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 作业类型代码
     */
    @Column(name = "activity_type_code", nullable = false, length = 6)
    private String activityTypeCode;

    /**
     * 作业类型名称
     */
    @Column(name = "activity_type_name", nullable = false, length = 100)
    private String activityTypeName;

    /**
     * 成本控制范围
     */
    @Column(name = "controlling_area", length = 4)
    private String controllingArea;

    /**
     * 作业单位
     */
    @Column(name = "activity_unit", length = 3)
    private String activityUnit;

    /**
     * 作业类别 (1-人工 2-机器 3-物料 4-其他)
     */
    @Column(name = "activity_category", length = 1)
    private String activityCategory;

    /**
     * 成本要素ID
     */
    @Column(name = "cost_element_id")
    private Long costElementId;

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
     * 获取作业类别名称
     */
    public String getActivityCategoryName() {
        return switch (activityCategory) {
            case "1" -> "人工";
            case "2" -> "机器";
            case "3" -> "物料";
            case "4" -> "其他";
            default -> "未知";
        };
    }

    /**
     * 判断当前是否有效
     */
    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(validFrom) && !now.isAfter(validTo);
    }
}
