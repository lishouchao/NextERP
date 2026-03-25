package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门 (组织单元)
 * 对标: SAP HRP1000 (Object Type O)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hrm_department")
public class HrmDepartment extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 部门编码
     */
    @Column(name = "dept_code", nullable = false, length = 50)
    private String deptCode;

    /**
     * 部门名称
     */
    @Column(name = "dept_name", nullable = false, length = 100)
    private String deptName;

    /**
     * 部门简称
     */
    @Column(name = "dept_short_name", length = 50)
    private String deptShortName;

    /**
     * 上级部门ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 部门层级
     */
    @Column(name = "dept_level")
    private Integer deptLevel;

    /**
     * 部门路径 (如: /1/2/3)
     */
    @Column(name = "dept_path", length = 500)
    private String deptPath;

    /**
     * 成本中心ID
     */
    @Column(name = "cost_center_id")
    private Long costCenterId;

    /**
     * 成本中心代码
     */
    @Column(name = "cost_center_code", length = 10)
    private String costCenterCode;

    /**
     * 公司代码
     */
    @Column(name = "company_code", length = 4)
    private String companyCode;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 负责人ID
     */
    @Column(name = "leader_id")
    private Long leaderId;

    /**
     * 负责人姓名
     */
    @Column(name = "leader_name", length = 50)
    private String leaderName;

    /**
     * 联系电话
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 部门类型 (01-公司 02-部门 03-组 04-团队)
     */
    @Column(name = "dept_type", length = 2)
    private String deptType;

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
     * 状态 (1-启用 0-禁用)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 是否叶子节点
     */
    @Column(name = "is_leaf", nullable = false)
    @Builder.Default
    private Boolean isLeaf = true;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 子部门列表 (不持久化)
     */
    @Transient
    private List<HrmDepartment> children = new ArrayList<>();

    /**
     * 获取部门类型名称
     */
    public String getDeptTypeName() {
        return switch (deptType) {
            case "01" -> "公司";
            case "02" -> "部门";
            case "03" -> "组";
            case "04" -> "团队";
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
