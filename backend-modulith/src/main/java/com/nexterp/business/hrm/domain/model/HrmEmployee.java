package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hrm_employee")
public class HrmEmployee extends TenantAwareEntity {

    /**
     * 员工ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工编号
     */
    @Column(name = "employee_no", nullable = false, length = 50)
    private String employeeNo;

    /**
     * 员工姓名
     */
    @Column(name = "employee_name", nullable = false, length = 50)
    private String employeeName;

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
     * 民族
     */
    @Column(name = "nation", length = 20)
    private String nation;

    /**
     * 身份证号
     */
    @Column(name = "id_card", length = 50)
    private String idCard;

    /**
     * 籍贯
     */
    @Column(name = "native_place", length = 100)
    private String nativePlace;

    /**
     * 政治面貌
     */
    @Column(name = "political_status", length = 20)
    private String politicalStatus;

    /**
     * 婚姻状况 (1-未婚 2-已婚 3-离异 4-丧偶)
     */
    @Column(name = "marital_status")
    private Integer maritalStatus;

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
     * 入职日期
     */
    @Column(name = "hire_date")
    private LocalDate hireDate;

    /**
     * 转正日期
     */
    @Column(name = "regular_date")
    private LocalDate regularDate;

    /**
     * 离职日期
     */
    @Column(name = "resign_date")
    private LocalDate resignDate;

    /**
     * 工号
     */
    @Column(name = "job_no", length = 50)
    private String jobNo;

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
     * 岗位ID
     */
    @Column(name = "position_id")
    private Long positionId;

    /**
     * 岗位名称
     */
    @Column(name = "position_name", length = 100)
    private String positionName;

    /**
     * 职级ID
     */
    @Column(name = "rank_id")
    private Long rankId;

    /**
     * 职级名称
     */
    @Column(name = "rank_name", length = 50)
    private String rankName;

    /**
     * 直属上级ID
     */
    @Column(name = "supervisor_id")
    private Long supervisorId;

    /**
     * 直属上级姓名
     */
    @Column(name = "supervisor_name", length = 50)
    private String supervisorName;

    /**
     * 工作地点
     */
    @Column(name = "work_location", length = 100)
    private String workLocation;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(name = "mobile", length = 20)
    private String mobile;

    /**
     * 紧急联系电话
     */
    @Column(name = "emergency_contact", length = 50)
    private String emergencyContact;

    /**
     * 紧急联系人电话
     */
    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;

    /**
     * 家庭住址
     */
    @Column(name = "home_address", length = 200)
    private String homeAddress;

    /**
     * 户口所在地
     */
    @Column(name = "registered_address", length = 200)
    private String registeredAddress;

    /**
     * 银行账号
     */
    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    /**
     * 开户银行
     */
    @Column(name = "bank_name", length = 100)
    private String bankName;

    /**
     * 基本工资
     */
    @Column(name = "base_salary", precision = 19, scale = 2)
    private BigDecimal baseSalary;

    /**
     * 岗位工资
     */
    @Column(name = "position_salary", precision = 19, scale = 2)
    private BigDecimal positionSalary;

    /**
     * 绩效工资
     */
    @Column(name = "performance_salary", precision = 19, scale = 2)
    private BigDecimal performanceSalary;

    /**
     * 津贴
     */
    @Column(name = "allowance", precision = 19, scale = 2)
    private BigDecimal allowance;

    /**
     * 社保个人部分
     */
    @Column(name = "social_security_personal", precision = 19, scale = 2)
    private BigDecimal socialSecurityPersonal;

    /**
     * 公积金个人部分
     */
    @Column(name = "housing_fund_personal", precision = 19, scale = 2)
    private BigDecimal housingFundPersonal;

    /**
     * 工作状态 (1-在职 2-试用 3-离职 4-停薪留职)
     */
    @Column(name = "work_status", nullable = false)
    private Integer workStatus;

    /**
     * 状态 (0-禁用 1-启用)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * 照片
     */
    @Column(name = "photo", length = 500)
    private String photo;

    /**
     * 附件 (JSON格式)
     */
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 自定义字段1
     */
    @Column(name = "custom_field1", length = 100)
    private String customField1;

    /**
     * 自定义字段2
     */
    @Column(name = "custom_field2", length = 100)
    private String customField2;

    /**
     * 自定义字段3
     */
    @Column(name = "custom_field3", length = 100)
    private String customField3;

    /**
     * 获取性别名称
     */
    public String getGenderName() {
        return gender == 1 ? "男" : "女";
    }

    /**
     * 获取婚姻状况名称
     */
    public String getMaritalStatusName() {
        return switch (maritalStatus) {
            case 1 -> "未婚";
            case 2 -> "已婚";
            case 3 -> "离异";
            case 4 -> "丧偶";
            default -> "未知";
        };
    }

    /**
     * 获取学历名称
     */
    public String getEducationName() {
        return switch (education) {
            case 1 -> "高中及以下";
            case 2 -> "大专";
            case 3 -> "本科";
            case 4 -> "硕士";
            case 5 -> "博士";
            default -> "未知";
        };
    }

    /**
     * 获取工作状态名称
     */
    public String getWorkStatusName() {
        return switch (workStatus) {
            case 1 -> "在职";
            case 2 -> "试用";
            case 3 -> "离职";
            case 4 -> "停薪留职";
            default -> "未知";
        };
    }

    /**
     * 判断是否在职
     */
    public boolean isActive() {
        return workStatus != null && workStatus == 1;
    }

    /**
     * 判断是否可用
     */
    public boolean isAvailable() {
        return isActive() && status != null && status == 1 && !getIsDeleted();
    }
}
