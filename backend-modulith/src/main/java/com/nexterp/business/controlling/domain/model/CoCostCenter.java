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
 * 成本中心
 * 对标: SAP CSKS
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_cost_center")
public class CoCostCenter extends TenantAwareEntity {

    /**
     * 成本中心ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 成本中心代码
     */
    @Column(name = "cost_center_code", nullable = false, length = 10)
    private String costCenterCode;

    /**
     * 成本中心名称
     */
    @Column(name = "cost_center_name", nullable = false, length = 100)
    private String costCenterName;

    /**
     * 成本中心组ID
     */
    @Column(name = "cost_center_group_id")
    private Long costCenterGroupId;

    /**
     * 公司代码
     */
    @Column(name = "company_code", length = 4)
    private String companyCode;

    /**
     * 成本控制范围
     */
    @Column(name = "controlling_area", length = 4)
    private String controllingArea;

    /**
     * 负责人ID
     */
    @Column(name = "person_responsible_id")
    private Long personResponsibleId;

    /**
     * 负责人姓名
     */
    @Column(name = "person_responsible_name", length = 50)
    private String personResponsibleName;

    /**
     * 成本中心类型 (01-生产 02-服务 03-行政 04-销售)
     */
    @Column(name = "cost_center_type", length = 2)
    private String costCenterType;

    /**
     * 部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 标准层次区域
     */
    @Column(name = "standard_hierarchy_area", length = 12)
    private String standardHierarchyArea;

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
     * 获取成本中心类型名称
     */
    public String getCostCenterTypeName() {
        return switch (costCenterType) {
            case "01" -> "生产";
            case "02" -> "服务";
            case "03" -> "行政";
            case "04" -> "销售";
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
