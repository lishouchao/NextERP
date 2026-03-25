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
import java.time.LocalDate;

/**
 * InfoType 0591 - 社保信息 (Social Insurance)
 *
 * 员工社保缴纳信息
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it0591_social_insurance", indexes = {
    @Index(name = "idx_it0591_employee", columnList = "tenant_id, employee_id, valid_from")
})
public class HrIt0591SocialInsurance extends TimeValidEntity {

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
     * 社保配置ID
     */
    @Column(name = "config_id", nullable = false)
    private Long configId;

    /**
     * 城市代码
     */
    @Column(name = "city_code", length = 6)
    private String cityCode;

    /**
     * 城市名称
     */
    @Column(name = "city_name", length = 50)
    private String cityName;

    /**
     * 社保基数
     */
    @Column(name = "social_base", nullable = false, precision = 19, scale = 2)
    private BigDecimal socialBase;

    /**
     * 养老保险-个人金额
     */
    @Column(name = "pension_personal", precision = 19, scale = 2)
    private BigDecimal pensionPersonal;

    /**
     * 养老保险-企业金额
     */
    @Column(name = "pension_company", precision = 19, scale = 2)
    private BigDecimal pensionCompany;

    /**
     * 医疗保险-个人金额
     */
    @Column(name = "medical_personal", precision = 19, scale = 2)
    private BigDecimal medicalPersonal;

    /**
     * 医疗保险-企业金额
     */
    @Column(name = "medical_company", precision = 19, scale = 2)
    private BigDecimal medicalCompany;

    /**
     * 失业保险-个人金额
     */
    @Column(name = "unemployment_personal", precision = 19, scale = 2)
    private BigDecimal unemploymentPersonal;

    /**
     * 失业保险-企业金额
     */
    @Column(name = "unemployment_company", precision = 19, scale = 2)
    private BigDecimal unemploymentCompany;

    /**
     * 工伤保险-企业金额
     */
    @Column(name = "injury_company", precision = 19, scale = 2)
    private BigDecimal injuryCompany;

    /**
     * 生育保险-企业金额
     */
    @Column(name = "maternity_company", precision = 19, scale = 2)
    private BigDecimal maternityCompany;

    /**
     * 大病医疗-个人金额
     */
    @Column(name = "critical_illness_personal", precision = 19, scale = 2)
    private BigDecimal criticalIllnessPersonal;

    /**
     * 大病医疗-企业金额
     */
    @Column(name = "critical_illness_company", precision = 19, scale = 2)
    private BigDecimal criticalIllnessCompany;

    /**
     * 个人社保合计
     */
    @Column(name = "total_personal", precision = 19, scale = 2)
    private BigDecimal totalPersonal;

    /**
     * 企业社保合计
     */
    @Column(name = "total_company", precision = 19, scale = 2)
    private BigDecimal totalCompany;

    /**
     * 社保号
     */
    @Column(name = "social_no", length = 30)
    private String socialNo;

    /**
     * 参保状态 (1-参保 2-停保 3-终止)
     */
    @Column(name = "insurance_status", length = 1)
    @Builder.Default
    private String insuranceStatus = "1";

    /**
     * 首次参保日期
     */
    @Column(name = "first_insurance_date")
    private LocalDate firstInsuranceDate;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 计算个人社保合计
     */
    public BigDecimal calculateTotalPersonal() {
        BigDecimal total = BigDecimal.ZERO;
        if (pensionPersonal != null) total = total.add(pensionPersonal);
        if (medicalPersonal != null) total = total.add(medicalPersonal);
        if (unemploymentPersonal != null) total = total.add(unemploymentPersonal);
        if (criticalIllnessPersonal != null) total = total.add(criticalIllnessPersonal);
        return total;
    }

    /**
     * 计算企业社保合计
     */
    public BigDecimal calculateTotalCompany() {
        BigDecimal total = BigDecimal.ZERO;
        if (pensionCompany != null) total = total.add(pensionCompany);
        if (medicalCompany != null) total = total.add(medicalCompany);
        if (unemploymentCompany != null) total = total.add(unemploymentCompany);
        if (injuryCompany != null) total = total.add(injuryCompany);
        if (maternityCompany != null) total = total.add(maternityCompany);
        if (criticalIllnessCompany != null) total = total.add(criticalIllnessCompany);
        return total;
    }
}
