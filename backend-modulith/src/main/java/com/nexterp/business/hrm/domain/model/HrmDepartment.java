package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
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
    @Column(name = "dept_code", nullable = false, length = 50, unique = true)
    private String deptCode;

    /**
     * 部门名称
     */
    @Column(name = "dept_name", nullable = false, length = 100)
    private String deptName;

    /**
     * 上级部门ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 排序
     */
    @Column(name = "sort")
    private Integer sort;

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
     * 状态 (1-启用 0-禁用)
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
}
