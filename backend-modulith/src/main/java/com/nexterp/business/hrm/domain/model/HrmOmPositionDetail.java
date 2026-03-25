package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 职位详情表 (Position Detail)
 * 对标 SAP HRP1000 OTYPE='S' + 扩展字段
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_om_position_detail")
public class HrmOmPositionDetail extends TimeValidEntity {

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
     * 职位编码 (业务主键)
     */
    @Column(name = "position_code", nullable = false, length = 8)
    private String positionCode;

    /**
     * 所属组织 OM 对象ID
     */
    @Column(name = "org_object_pk")
    private Long orgObjectPk;

    /**
     * 所属组织ID (业务)
     */
    @Column(name = "org_object_id", length = 8)
    private String orgObjectId;

    /**
     * 所属组织名称 (冗余)
     */
    @Column(name = "org_name", length = 100)
    private String orgName;

    /**
     * 关联职务 OM 对象ID
     */
    @Column(name = "job_object_pk", nullable = false)
    private Long jobObjectPk;

    /**
     * 关联职务代码
     */
    @Column(name = "job_code", length = 8)
    private String jobCode;

    /**
     * 关联职务名称 (冗余)
     */
    @Column(name = "job_name", length = 100)
    private String jobName;

    /**
     * 当前任职者 OM 对象ID (P类型)
     */
    @Column(name = "holder_object_pk")
    private Long holderObjectPk;

    /**
     * 当前任职者工号
     */
    @Column(name = "holder_employee_no", length = 8)
    private String holderEmployeeNo;

    /**
     * 当前任职者姓名 (冗余)
     */
    @Column(name = "holder_name", length = 50)
    private String holderName;

    /**
     * 成本中心代码 (可覆盖组织成本中心)
     */
    @Column(name = "cost_center_code", length = 10)
    private String costCenterCode;

    /**
     * 编制数 (1=单人职位 >1=多人职位)
     */
    @Column(name = "headcount", nullable = false)
    @Builder.Default
    private Integer headcount = 1;

    /**
     * 当前人数
     */
    @Column(name = "current_count", nullable = false)
    @Builder.Default
    private Integer currentCount = 0;

    /**
     * 职位状态 (VACANT-空缺 OCCUPIED-已填充 FROZEN-冻结 ABOLISHED-废除)
     */
    @Column(name = "position_status", nullable = false, length = 10)
    @Builder.Default
    private String positionStatus = "VACANT";

    /**
     * 职位类型 (FULL-全职 PART-兼职 TEMP-临时)
     */
    @Column(name = "position_type", length = 10)
    @Builder.Default
    private String positionType = "FULL";

    /**
     * 职级 (可覆盖职务职级)
     */
    @Column(name = "grade", length = 4)
    private String grade;

    /**
     * 职等
     */
    @Column(name = "job_level")
    private Integer jobLevel;

    /**
     * 标准薪资范围-下限
     */
    @Column(name = "salary_min", precision = 23, scale = 2)
    private BigDecimal salaryMin;

    /**
     * 标准薪资范围-上限
     */
    @Column(name = "salary_max", precision = 23, scale = 2)
    private BigDecimal salaryMax;

    /**
     * 直线经理标识
     */
    @Column(name = "is_manager", nullable = false)
    @Builder.Default
    private Boolean isManager = false;

    /**
     * 关键岗位标识
     */
    @Column(name = "is_key_position", nullable = false)
    @Builder.Default
    private Boolean isKeyPosition = false;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取职位状态名称
     */
    public String getPositionStatusName() {
        return switch (positionStatus) {
            case "VACANT" -> "空缺";
            case "OCCUPIED" -> "已填充";
            case "FROZEN" -> "冻结";
            case "ABOLISHED" -> "废除";
            default -> "未知";
        };
    }

    /**
     * 是否有空缺
     */
    public boolean hasVacancy() {
        return currentCount < headcount;
    }

    /**
     * 获取空缺数
     */
    public int getVacancyCount() {
        return Math.max(0, headcount - currentCount);
    }

    /**
     * 是否单人职位
     */
    public boolean isSinglePosition() {
        return headcount == 1;
    }
}
