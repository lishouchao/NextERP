package com.nexterp.business.hrm.domain.model;

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
import java.time.LocalDateTime;

/**
 * 招聘需求 (对标 SAP PB40)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hrm_recruitment")
public class HrmRecruitment extends TenantAwareEntity {

    /**
     * 招聘需求ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 需求单号
     */
    @Column(name = "requisition_no", nullable = false, length = 20)
    private String requisitionNo;

    /**
     * 招聘标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Column(name = "department_name", length = 100)
    private String departmentName;

    /**
     * 职位ID
     */
    @Column(name = "position_id")
    private Long positionId;

    /**
     * 职位名称
     */
    @Column(name = "position_name", length = 100)
    private String positionName;

    /**
     * 招聘人数
     */
    @Column(name = "head_count", nullable = false)
    private Integer headCount;

    /**
     * 已招聘人数
     */
    @Column(name = "hired_count")
    @Builder.Default
    private Integer hiredCount = 0;

    /**
     * 工作地点
     */
    @Column(name = "work_location", length = 100)
    private String workLocation;

    /**
     * 薪酬范围-下限
     */
    @Column(name = "salary_min", precision = 19, scale = 2)
    private BigDecimal salaryMin;

    /**
     * 薪酬范围-上限
     */
    @Column(name = "salary_max", precision = 19, scale = 2)
    private BigDecimal salaryMax;

    /**
     * 学历要求 (1-高中及以下 2-大专 3-本科 4-硕士 5-博士)
     */
    @Column(name = "education_requirement")
    private Integer educationRequirement;

    /**
     * 工作年限要求(年)
     */
    @Column(name = "experience_years")
    private Integer experienceYears;

    /**
     * 需求日期
     */
    @Column(name = "requirement_date", nullable = false)
    private LocalDate requirementDate;

    /**
     * 期望到岗日期
     */
    @Column(name = "expected_date")
    private LocalDate expectedDate;

    /**
     * 截止日期
     */
    @Column(name = "deadline")
    private LocalDate deadline;

    /**
     * 招聘类型 (01-社招 02-校招 03-内推)
     */
    @Column(name = "recruitment_type", length = 2)
    private String recruitmentType;

    /**
     * 招聘渠道 (01-招聘网站 02-猎头 03-内部推荐 04-校园招聘)
     */
    @Column(name = "channel", length = 2)
    private String channel;

    /**
     * 需求申请人ID
     */
    @Column(name = "requester_id")
    private Long requesterId;

    /**
     * 需求申请人姓名
     */
    @Column(name = "requester_name", length = 50)
    private String requesterName;

    /**
     * 状态 (01-草稿 02-审批中 03-已发布 04-进行中 05-已完成 06-已关闭)
     */
    @Column(name = "status", nullable = false, length = 2)
    @Builder.Default
    private String status = "01";

    /**
     * 发布时间
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 职位描述
     */
    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    /**
     * 任职要求
     */
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取招聘类型名称
     */
    public String getRecruitmentTypeName() {
        return switch (recruitmentType) {
            case "01" -> "社招";
            case "02" -> "校招";
            case "03" -> "内推";
            default -> "未知";
        };
    }

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        return switch (status) {
            case "01" -> "草稿";
            case "02" -> "审批中";
            case "03" -> "已发布";
            case "04" -> "进行中";
            case "05" -> "已完成";
            case "06" -> "已关闭";
            default -> "未知";
        };
    }

    /**
     * 判断是否有空缺
     */
    public boolean hasVacancy() {
        return hiredCount < headCount;
    }

    /**
     * 获取剩余招聘数
     */
    public int getRemainingCount() {
        return headCount - hiredCount;
    }
}
