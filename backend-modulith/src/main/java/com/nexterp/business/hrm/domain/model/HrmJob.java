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

/**
 * 职务 (对标 SAP HRP1000 - Object Type C)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hrm_job")
public class HrmJob extends TenantAwareEntity {

    /**
     * 职务ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 职务编码
     */
    @Column(name = "job_code", nullable = false, length = 8)
    private String jobCode;

    /**
     * 职务名称
     */
    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    /**
     * 职务简称
     */
    @Column(name = "job_short_name", length = 50)
    private String jobShortName;

    /**
     * 职务类别 (01-管理类 02-专业类 03-技术类 04-操作类 05-服务类)
     */
    @Column(name = "job_category", length = 2)
    private String jobCategory;

    /**
     * 职务族ID
     */
    @Column(name = "job_family_id")
    private Long jobFamilyId;

    /**
     * 职等范围-起始
     */
    @Column(name = "grade_from", length = 4)
    private String gradeFrom;

    /**
     * 职等范围-结束
     */
    @Column(name = "grade_to", length = 4)
    private String gradeTo;

    /**
     * 上级职务ID
     */
    @Column(name = "parent_job_id")
    private Long parentJobId;

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
     * 职责描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 任职资格
     */
    @Column(name = "qualification", columnDefinition = "TEXT")
    private String qualification;

    /**
     * 获取职务类别名称
     */
    public String getJobCategoryName() {
        return switch (jobCategory) {
            case "01" -> "管理类";
            case "02" -> "专业类";
            case "03" -> "技术类";
            case "04" -> "操作类";
            case "05" -> "服务类";
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
