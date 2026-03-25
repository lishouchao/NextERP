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
 * InfoType 0002 - 个人数据 (Personal Data)
 * 对标 SAP IT0002
 *
 * 存储员工的个人信息，包括姓名、性别、出生日期、婚姻状况等
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it0002_personal_data", indexes = {
    @Index(name = "idx_it0002_employee", columnList = "tenant_id, employee_id, valid_from")
})
public class HrIt0002PersonalData extends TimeValidEntity {

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
     * 姓氏
     */
    @Column(name = "last_name", length = 50)
    private String lastName;

    /**
     * 名字
     */
    @Column(name = "first_name", length = 50)
    private String firstName;

    /**
     * 全名
     */
    @Column(name = "full_name", nullable = false, length = 50)
    private String fullName;

    /**
     * 拼音/英文名
     */
    @Column(name = "name_pinyin", length = 100)
    private String namePinyin;

    /**
     * 曾用名
     */
    @Column(name = "former_name", length = 50)
    private String formerName;

    /**
     * 性别 (M-男 F-女)
     */
    @Column(name = "gender", nullable = false, length = 1)
    private String gender;

    /**
     * 出生日期
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * 出生地
     */
    @Column(name = "birth_place", length = 100)
    private String birthPlace;

    /**
     * 国籍
     */
    @Column(name = "nationality", length = 3)
    @Builder.Default
    private String nationality = "CHN";

    /**
     * 民族
     */
    @Column(name = "ethnicity", length = 2)
    private String ethnicity;

    /**
     * 婚姻状况 (1-未婚 2-已婚 3-离异 4-丧偶)
     */
    @Column(name = "marital_status", length = 1)
    private String maritalStatus;

    /**
     * 婚姻状况变更日期
     */
    @Column(name = "marital_status_date")
    private LocalDate maritalStatusDate;

    /**
     * 子女数量
     */
    @Column(name = "children_count")
    @Builder.Default
    private Integer childrenCount = 0;

    /**
     * 证件类型 (1-身份证 2-护照 3-港澳通行证 4-台湾通行证 5-其他)
     */
    @Column(name = "id_type", length = 1)
    @Builder.Default
    private String idType = "1";

    /**
     * 证件号码
     */
    @Column(name = "id_number", length = 20)
    private String idNumber;

    /**
     * 证件签发日期
     */
    @Column(name = "id_issue_date")
    private LocalDate idIssueDate;

    /**
     * 证件有效期
     */
    @Column(name = "id_expiry_date")
    private LocalDate idExpiryDate;

    /**
     * 证件签发地
     */
    @Column(name = "id_issue_place", length = 50)
    private String idIssuePlace;

    /**
     * 政治面貌 (01-中共党员 02-共青团员 03-民主党派 04-群众)
     */
    @Column(name = "political_status", length = 2)
    private String politicalStatus;

    /**
     * 入党/团日期
     */
    @Column(name = "political_join_date")
    private LocalDate politicalJoinDate;

    /**
     * 宗教信仰
     */
    @Column(name = "religion", length = 20)
    private String religion;

    /**
     * 健康状况 (1-良好 2-一般 3-较差)
     */
    @Column(name = "health_status", length = 1)
    @Builder.Default
    private String healthStatus = "1";

    /**
     * 最高学历 (1-高中及以下 2-大专 3-本科 4-硕士 5-博士)
     */
    @Column(name = "highest_education", length = 1)
    private String highestEducation;

    /**
     * 最高学位 (1-无 2-学士 3-硕士 4-博士)
     */
    @Column(name = "highest_degree", length = 1)
    private String highestDegree;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取性别名称
     */
    public String getGenderName() {
        return "M".equals(gender) ? "男" : "女";
    }

    /**
     * 获取婚姻状况名称
     */
    public String getMaritalStatusName() {
        return switch (maritalStatus) {
            case "1" -> "未婚";
            case "2" -> "已婚";
            case "3" -> "离异";
            case "4" -> "丧偶";
            default -> "未知";
        };
    }

    /**
     * 获取学历名称
     */
    public String getHighestEducationName() {
        return switch (highestEducation) {
            case "1" -> "高中及以下";
            case "2" -> "大专";
            case "3" -> "本科";
            case "4" -> "硕士";
            case "5" -> "博士";
            default -> "未知";
        };
    }

    /**
     * 获取年龄
     */
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return LocalDate.now().getYear() - birthDate.getYear();
    }
}
