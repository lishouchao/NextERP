package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 组织单元详情表 (Organization Unit Detail)
 * 对标 SAP HRP1000 + 自定义扩展字段
 *
 * 存储组织单元的扩展属性，如编制、成本中心、公司代码等
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_om_org_unit_detail")
public class HrmOmOrgUnitDetail extends TimeValidEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联 OM 对象内码
     */
    @Column(name = "object_pk", nullable = false)
    private Long objectPk;

    /**
     * 组织编码 (业务主键)
     */
    @Column(name = "org_code", nullable = false, length = 12)
    private String orgCode;

    /**
     * 父组织 OM 对象ID
     */
    @Column(name = "parent_object_pk")
    private Long parentObjectPk;

    /**
     * 父组织对象ID (业务)
     */
    @Column(name = "parent_object_id", length = 8)
    private String parentObjectId;

    /**
     * 组织分类 (01-公司 02-事业部 03-部门 04-组 05-团队 06-科室)
     */
    @Column(name = "org_category", length = 2)
    private String orgCategory;

    /**
     * 组织层级深度
     */
    @Column(name = "org_level")
    @Builder.Default
    private Integer orgLevel = 1;

    /**
     * 层级路径 (如: /ROOT/BR/DE)
     */
    @Column(name = "org_path", length = 500)
    private String orgPath;

    /**
     * 公司代码 (对应 FI 公司)
     */
    @Column(name = "company_code", length = 4)
    private String companyCode;

    /**
     * 成本中心代码 (对应 CO 成本中心)
     */
    @Column(name = "cost_center_code", length = 10)
    private String costCenterCode;

    /**
     * 当前人数
     */
    @Column(name = "headcount")
    @Builder.Default
    private Integer headcount = 0;

    /**
     * 编制上限
     */
    @Column(name = "max_headcount")
    private Integer maxHeadcount;

    /**
     * 预算人数
     */
    @Column(name = "budget_headcount")
    private Integer budgetHeadcount;

    /**
     * 负责人ID (人员 OM 对象内码)
     */
    @Column(name = "manager_pk")
    private Long managerPk;

    /**
     * 负责人工号
     */
    @Column(name = "manager_employee_no", length = 8)
    private String managerEmployeeNo;

    /**
     * 负责人姓名
     */
    @Column(name = "manager_name", length = 50)
    private String managerName;

    /**
     * 联系电话
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 电子邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 办公地址
     */
    @Column(name = "office_address", length = 200)
    private String officeAddress;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取组织分类名称
     */
    public String getOrgCategoryName() {
        return switch (orgCategory) {
            case "01" -> "公司";
            case "02" -> "事业部";
            case "03" -> "部门";
            case "04" -> "组";
            case "05" -> "团队";
            case "06" -> "科室";
            default -> "未知";
        };
    }

    /**
     * 判断是否超编
     */
    public boolean isOverstaffed() {
        return maxHeadcount != null && headcount != null && headcount > maxHeadcount;
    }

    /**
     * 获取空缺人数
     */
    public int getVacancyCount() {
        if (maxHeadcount == null || headcount == null) {
            return 0;
        }
        return Math.max(0, maxHeadcount - headcount);
    }
}
