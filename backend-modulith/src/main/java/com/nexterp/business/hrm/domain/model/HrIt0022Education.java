package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * InfoType 0022 - 教育经历 (Education)
 * 对标 SAP IT0022
 *
 * 存储员工的教育背景信息
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it0022_education", indexes = {
    @Index(name = "idx_it0022_employee", columnList = "tenant_id, employee_id, valid_from")
})
public class HrIt0022Education extends TimeValidEntity {

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
     * 教育类型 (1-全日制 2-在职 3-自考 4-成人教育 5-网络教育 6-其他)
     */
    @Column(name = "education_type", length = 1)
    @Builder.Default
    private String educationType = "1";

    /**
     * 学历 (1-高中及以下 2-大专 3-本科 4-硕士 5-博士)
     */
    @Column(name = "education_level", nullable = false, length = 1)
    private String educationLevel;

    /**
     * 学位 (1-无 2-学士 3-硕士 4-博士)
     */
    @Column(name = "degree", length = 1)
    private String degree;

    /**
     * 学校名称
     */
    @Column(name = "school_name", nullable = false, length = 100)
    private String schoolName;

    /**
     * 学校类型 (1-985 2-211 3-普通本科 4-专科 5-海外院校)
     */
    @Column(name = "school_type", length = 1)
    private String schoolType;

    /**
     * 院校所在地
     */
    @Column(name = "school_location", length = 100)
    private String schoolLocation;

    /**
     * 院系
     */
    @Column(name = "department", length = 100)
    private String department;

    /**
     * 专业
     */
    @Column(name = "major", length = 50)
    private String major;

    /**
     * 专业类别 (01-工学 02-理学 03-文学 04-经济学 05-管理学 06-法学 07-教育学 08-医学 09-农学 10-其他)
     */
    @Column(name = "major_category", length = 2)
    private String majorCategory;

    /**
     * 入学日期
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 毕业/结业日期
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 是否最高学历
     */
    @Column(name = "is_highest_education", nullable = false)
    @Builder.Default
    private Boolean isHighestEducation = false;

    /**
     * 是否最高学位
     */
    @Column(name = "is_highest_degree", nullable = false)
    @Builder.Default
    private Boolean isHighestDegree = false;

    /**
     * 是否第一学历
     */
    @Column(name = "is_first_education", nullable = false)
    @Builder.Default
    private Boolean isFirstEducation = false;

    /**
     * 学习形式 (1-普通全日制 2-非全日制)
     */
    @Column(name = "study_mode", length = 1)
    @Builder.Default
    private String studyMode = "1";

    /**
     * 学历证书编号
     */
    @Column(name = "diploma_no", length = 30)
    private String diplomaNo;

    /**
     * 学位证书编号
     */
    @Column(name = "degree_no", length = 30)
    private String degreeNo;

    /**
     * 证书附件
     */
    @Column(name = "certificate_attachment", length = 500)
    private String certificateAttachment;

    /**
     * 学信网验证状态 (0-未验证 1-已验证 2-验证失败)
     */
    @Column(name = "verify_status", length = 1)
    @Builder.Default
    private String verifyStatus = "0";

    /**
     * 学信网验证时间
     */
    @Column(name = "verify_time")
    private LocalDate verifyTime;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取学历名称
     */
    public String getEducationLevelName() {
        return switch (educationLevel) {
            case "1" -> "高中及以下";
            case "2" -> "大专";
            case "3" -> "本科";
            case "4" -> "硕士";
            case "5" -> "博士";
            default -> "未知";
        };
    }

    /**
     * 获取学位名称
     */
    public String getDegreeName() {
        return switch (degree) {
            case "1" -> "无";
            case "2" -> "学士";
            case "3" -> "硕士";
            case "4" -> "博士";
            default -> "未知";
        };
    }

    /**
     * 获取教育时长 (年)
     */
    public Integer getDurationYears() {
        if (startDate == null || endDate == null) {
            return null;
        }
        return endDate.getYear() - startDate.getYear();
    }
}
