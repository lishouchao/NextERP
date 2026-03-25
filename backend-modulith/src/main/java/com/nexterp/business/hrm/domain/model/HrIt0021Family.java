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
 * InfoType 0021 - 家庭成员 (Family)
 * 对标 SAP IT0021
 *
 * 存储员工的家庭成员信息
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it0021_family", indexes = {
    @Index(name = "idx_it0021_employee", columnList = "tenant_id, employee_id, valid_from"),
    @Index(name = "idx_it0021_type", columnList = "tenant_id, employee_id, family_type")
})
public class HrIt0021Family extends TimeValidEntity {

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
     * 家庭成员类型 (01-配偶 02-子女 03-父亲 04-母亲 05-岳父/公公 06-岳母/婆婆 07-兄弟 08-姐妹 09-其他)
     */
    @Column(name = "family_type", nullable = false, length = 2)
    private String familyType;

    /**
     * 姓名
     */
    @Column(name = "member_name", nullable = false, length = 50)
    private String memberName;

    /**
     * 性别 (M-男 F-女)
     */
    @Column(name = "gender", length = 1)
    private String gender;

    /**
     * 出生日期
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * 证件类型
     */
    @Column(name = "id_type", length = 1)
    private String idType;

    /**
     * 证件号码
     */
    @Column(name = "id_number", length = 20)
    private String idNumber;

    /**
     * 工作单位
     */
    @Column(name = "work_unit", length = 100)
    private String workUnit;

    /**
     * 职务
     */
    @Column(name = "occupation", length = 50)
    private String occupation;

    /**
     * 联系电话
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 联系地址
     */
    @Column(name = "address", length = 200)
    private String address;

    /**
     * 是否紧急联系人
     */
    @Column(name = "is_emergency_contact", nullable = false)
    @Builder.Default
    private Boolean isEmergencyContact = false;

    /**
     * 与员工关系
     */
    @Column(name = "relationship", length = 20)
    private String relationship;

    /**
     * 是否抚养对象
     */
    @Column(name = "is_dependent", nullable = false)
    @Builder.Default
    private Boolean isDependent = false;

    /**
     * 是否子女 (用于个税专项附加扣除)
     */
    @Column(name = "is_child_for_tax")
    @Builder.Default
    private Boolean isChildForTax = false;

    /**
     * 是否老人 (用于个税专项附加扣除)
     */
    @Column(name = "is_elder_for_tax")
    @Builder.Default
    private Boolean isElderForTax = false;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取家庭成员类型名称
     */
    public String getFamilyTypeName() {
        return switch (familyType) {
            case "01" -> "配偶";
            case "02" -> "子女";
            case "03" -> "父亲";
            case "04" -> "母亲";
            case "05" -> "岳父/公公";
            case "06" -> "岳母/婆婆";
            case "07" -> "兄弟";
            case "08" -> "姐妹";
            case "09" -> "其他";
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
