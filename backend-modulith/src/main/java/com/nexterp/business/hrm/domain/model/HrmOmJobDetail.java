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
 * 职务详情表 (Job Detail)
 * 对标 SAP HRP1000 OTYPE='C' + 扩展字段
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_om_job_detail")
public class HrmOmJobDetail extends TimeValidEntity {

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
     * 职务编码 (业务主键)
     */
    @Column(name = "job_code", nullable = false, length = 8)
    private String jobCode;

    /**
     * 职务族ID (归类的职务族)
     */
    @Column(name = "job_family_id")
    private Long jobFamilyId;

    /**
     * 职务族代码
     */
    @Column(name = "job_family_code", length = 4)
    private String jobFamilyCode;

    /**
     * 职务族名称
     */
    @Column(name = "job_family_name", length = 50)
    private String jobFamilyName;

    /**
     * 职能分类 (01-管理 02-技术 03-销售 04-市场 05-财务 06-人事 07-行政 08-生产 09-服务 10-其他)
     */
    @Column(name = "job_function", length = 2)
    private String jobFunction;

    /**
     * 职级范围-起始 (如: P1)
     */
    @Column(name = "grade_from", length = 4)
    private String gradeFrom;

    /**
     * 职级范围-结束 (如: P5)
     */
    @Column(name = "grade_to", length = 4)
    private String gradeTo;

    /**
     * 职等范围-起始 (如: 1)
     */
    @Column(name = "level_from")
    private Integer levelFrom;

    /**
     * 职等范围-结束 (如: 10)
     */
    @Column(name = "level_to")
    private Integer levelTo;

    /**
     * 父职务 OM 对象ID
     */
    @Column(name = "parent_job_pk")
    private Long parentJobPk;

    /**
     * 父职务代码
     */
    @Column(name = "parent_job_code", length = 8)
    private String parentJobCode;

    /**
     * 职责描述
     */
    @Column(name = "responsibility", columnDefinition = "TEXT")
    private String responsibility;

    /**
     * 任职资格要求
     */
    @Column(name = "qualification_req", columnDefinition = "TEXT")
    private String qualificationReq;

    /**
     * 能力素质要求 (JSON格式)
     */
    @Column(name = "competency_req", columnDefinition = "TEXT")
    private String competencyReq;

    /**
     * 学历要求 (1-高中及以下 2-大专 3-本科 4-硕士 5-博士)
     */
    @Column(name = "education_req")
    private Integer educationReq;

    /**
     * 工作年限要求 (年)
     */
    @Column(name = "experience_years")
    private Integer experienceYears;

    /**
     * 所需资格证书 (JSON数组)
     */
    @Column(name = "certificates_req", columnDefinition = "TEXT")
    private String certificatesReq;

    /**
     * 关联职位数
     */
    @Column(name = "position_count")
    @Builder.Default
    private Integer positionCount = 0;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取职能名称
     */
    public String getJobFunctionName() {
        return switch (jobFunction) {
            case "01" -> "管理";
            case "02" -> "技术";
            case "03" -> "销售";
            case "04" -> "市场";
            case "05" -> "财务";
            case "06" -> "人事";
            case "07" -> "行政";
            case "08" -> "生产";
            case "09" -> "服务";
            case "10" -> "其他";
            default -> "未知";
        };
    }
}
