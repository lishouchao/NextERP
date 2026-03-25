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
import java.time.LocalDateTime;

/**
 * 候选人 (对标 SAP PB40)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hrm_candidate")
public class HrmCandidate extends TenantAwareEntity {

    /**
     * 候选人ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 候选人编号
     */
    @Column(name = "candidate_no", nullable = false, length = 20)
    private String candidateNo;

    /**
     * 姓名
     */
    @Column(name = "candidate_name", nullable = false, length = 50)
    private String candidateName;

    /**
     * 英文名
     */
    @Column(name = "english_name", length = 100)
    private String englishName;

    /**
     * 性别 (1-男 2-女)
     */
    @Column(name = "gender", nullable = false)
    private Integer gender;

    /**
     * 出生日期
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * 手机号
     */
    @Column(name = "mobile", length = 20)
    private String mobile;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 学历 (1-高中及以下 2-大专 3-本科 4-硕士 5-博士)
     */
    @Column(name = "education")
    private Integer education;

    /**
     * 毕业院校
     */
    @Column(name = "graduate_school", length = 100)
    private String graduateSchool;

    /**
     * 专业
     */
    @Column(name = "major", length = 50)
    private String major;

    /**
     * 毕业年份
     */
    @Column(name = "graduate_year", length = 4)
    private String graduateYear;

    /**
     * 工作年限(年)
     */
    @Column(name = "work_years")
    private Integer workYears;

    /**
     * 当前公司
     */
    @Column(name = "current_company", length = 100)
    private String currentCompany;

    /**
     * 当前职位
     */
    @Column(name = "current_position", length = 100)
    private String currentPosition;

    /**
     * 期望薪资
     */
    @Column(name = "expected_salary", precision = 19, scale = 2)
    private java.math.BigDecimal expectedSalary;

    /**
     * 期望工作地点
     */
    @Column(name = "expected_location", length = 100)
    private String expectedLocation;

    /**
     * 可入职日期
     */
    @Column(name = "available_date")
    private LocalDate availableDate;

    /**
     * 来源 (01-招聘网站 02-猎头 03-内部推荐 04-主动投递)
     */
    @Column(name = "source", length = 2)
    private String source;

    /**
     * 推荐人ID
     */
    @Column(name = "referrer_id")
    private Long referrerId;

    /**
     * 推荐人姓名
     */
    @Column(name = "referrer_name", length = 50)
    private String referrerName;

    /**
     * 应聘招聘需求ID
     */
    @Column(name = "recruitment_id")
    private Long recruitmentId;

    /**
     * 招聘需求单号
     */
    @Column(name = "requisition_no", length = 20)
    private String requisitionNo;

    /**
     * 应聘职位
     */
    @Column(name = "apply_position", length = 100)
    private String applyPosition;

    /**
     * 候选人阶段 (01-简历筛选 02-初试 03-复试 04-终试 05-Offer 06-入职 07-淘汰)
     */
    @Column(name = "stage", nullable = false, length = 2)
    @Builder.Default
    private String stage = "01";

    /**
     * 状态 (01-待处理 02-进行中 03-已录用 04-已淘汰 05-已放弃)
     */
    @Column(name = "status", nullable = false, length = 2)
    @Builder.Default
    private String status = "01";

    /**
     * 综合评分 (1-5分)
     */
    @Column(name = "rating")
    private Integer rating;

    /**
     * 简历附件
     */
    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 最后联系时间
     */
    @Column(name = "last_contact_time")
    private LocalDateTime lastContactTime;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取性别名称
     */
    public String getGenderName() {
        return gender == 1 ? "男" : "女";
    }

    /**
     * 获取阶段名称
     */
    public String getStageName() {
        return switch (stage) {
            case "01" -> "简历筛选";
            case "02" -> "初试";
            case "03" -> "复试";
            case "04" -> "终试";
            case "05" -> "Offer";
            case "06" -> "入职";
            case "07" -> "淘汰";
            default -> "未知";
        };
    }

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        return switch (status) {
            case "01" -> "待处理";
            case "02" -> "进行中";
            case "03" -> "已录用";
            case "04" -> "已淘汰";
            case "05" -> "已放弃";
            default -> "未知";
        };
    }

    /**
     * 获取来源名称
     */
    public String getSourceName() {
        return switch (source) {
            case "01" -> "招聘网站";
            case "02" -> "猎头";
            case "03" -> "内部推荐";
            case "04" -> "主动投递";
            default -> "未知";
        };
    }
}
