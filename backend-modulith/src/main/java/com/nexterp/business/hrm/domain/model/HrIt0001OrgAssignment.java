package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * InfoType 0001 - 组织分配 (Organizational Assignment)
 * 对标 SAP IT0001
 *
 * 记录员工的组织架构分配信息，包括公司、组织、职位、职务等
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it0001_org_assignment", indexes = {
    @Index(name = "idx_it0001_employee", columnList = "tenant_id, employee_id, valid_from"),
    @Index(name = "idx_it0001_position", columnList = "tenant_id, position_pk"),
    @Index(name = "idx_it0001_org", columnList = "tenant_id, org_pk")
})
public class HrIt0001OrgAssignment extends TimeValidEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工内码
     */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /**
     * 员工编号
     */
    @Column(name = "employee_no", nullable = false, length = 8)
    private String employeeNo;

    /**
     * 公司代码 (对应 FI 公司)
     */
    @Column(name = "company_code", nullable = false, length = 4)
    private String companyCode;

    /**
     * 人事范围 (人事管理子范围)
     */
    @Column(name = "personnel_area", length = 4)
    private String personnelArea;

    /**
     * 人事子范围
     */
    @Column(name = "personnel_subarea", length = 4)
    private String personnelSubarea;

    /**
     * 员工组
     */
    @Column(name = "employee_group", length = 1)
    private String employeeGroup;

    /**
     * 员工子组
     */
    @Column(name = "employee_subgroup", length = 2)
    private String employeeSubgroup;

    /**
     * 组织单元 OM 对象内码
     */
    @Column(name = "org_pk", nullable = false)
    private Long orgPk;

    /**
     * 组织单元ID (业务)
     */
    @Column(name = "org_id", length = 8)
    private String orgId;

    /**
     * 组织单元名称 (冗余)
     */
    @Column(name = "org_name", length = 100)
    private String orgName;

    /**
     * 职位 OM 对象内码
     */
    @Column(name = "position_pk", nullable = false)
    private Long positionPk;

    /**
     * 职位代码 (业务)
     */
    @Column(name = "position_id", length = 8)
    private String positionId;

    /**
     * 职位名称 (冗余)
     */
    @Column(name = "position_name", length = 100)
    private String positionName;

    /**
     * 职务 OM 对象内码
     */
    @Column(name = "job_pk")
    private Long jobPk;

    /**
     * 职务代码
     */
    @Column(name = "job_id", length = 8)
    private String jobId;

    /**
     * 职务名称 (冗余)
     */
    @Column(name = "job_name", length = 100)
    private String jobName;

    /**
     * 成本中心代码
     */
    @Column(name = "cost_center_code", length = 10)
    private String costCenterCode;

    /**
     * 直线经理员工编号
     */
    @Column(name = "manager_employee_no", length = 8)
    private String managerEmployeeNo;

    /**
     * 直线经理姓名
     */
    @Column(name = "manager_name", length = 50)
    private String managerName;

    /**
     * 是否主分配
     */
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = true;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取员工组名称
     */
    public String getEmployeeGroupName() {
        return switch (employeeGroup) {
            case "1" -> "正式员工";
            case "2" -> "合同员工";
            case "3" -> "临时员工";
            case "4" -> "实习生";
            default -> "未知";
        };
    }
}
