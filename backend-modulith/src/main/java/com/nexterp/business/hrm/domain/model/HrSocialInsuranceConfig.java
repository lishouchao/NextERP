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

/**
 * 社保配置 (Social Insurance Config)
 *
 * 定义各地区社保缴纳比例和基数
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_social_insurance_config", indexes = {
    @Index(name = "idx_social_config_city", columnList = "tenant_id, city_code, valid_from")
})
public class HrSocialInsuranceConfig extends TenantAwareEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配置名称
     */
    @Column(name = "config_name", nullable = false, length = 100)
    private String configName;

    /**
     * 城市代码
     */
    @Column(name = "city_code", nullable = false, length = 6)
    private String cityCode;

    /**
     * 城市名称
     */
    @Column(name = "city_name", length = 50)
    private String cityName;

    /**
     * 生效日期
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    /**
     * 失效日期
     */
    @Column(name = "valid_to")
    private LocalDate validTo;

    /**
     * 养老保险-个人比例 (%)
     */
    @Column(name = "pension_personal_rate", precision = 5, scale = 2)
    private BigDecimal pensionPersonalRate;

    /**
     * 养老保险-企业比例 (%)
     */
    @Column(name = "pension_company_rate", precision = 5, scale = 2)
    private BigDecimal pensionCompanyRate;

    /**
     * 医疗保险-个人比例 (%)
     */
    @Column(name = "medical_personal_rate", precision = 5, scale = 2)
    private BigDecimal medicalPersonalRate;

    /**
     * 医疗保险-企业比例 (%)
     */
    @Column(name = "medical_company_rate", precision = 5, scale = 2)
    private BigDecimal medicalCompanyRate;

    /**
     * 失业保险-个人比例 (%)
     */
    @Column(name = "unemployment_personal_rate", precision = 5, scale = 2)
    private BigDecimal unemploymentPersonalRate;

    /**
     * 失业保险-企业比例 (%)
     */
    @Column(name = "unemployment_company_rate", precision = 5, scale = 2)
    private BigDecimal unemploymentCompanyRate;

    /**
     * 工伤保险-企业比例 (%)
     */
    @Column(name = "injury_company_rate", precision = 5, scale = 2)
    private BigDecimal injuryCompanyRate;

    /**
     * 生育保险-企业比例 (%)
     */
    @Column(name = "maternity_company_rate", precision = 5, scale = 2)
    private BigDecimal maternityCompanyRate;

    /**
     * 大病医疗-个人比例 (%)
     */
    @Column(name = "critical_illness_personal_rate", precision = 5, scale = 2)
    private BigDecimal criticalIllnessPersonalRate;

    /**
     * 大病医疗-企业比例 (%)
     */
    @Column(name = "critical_illness_company_rate", precision = 5, scale = 2)
    private BigDecimal criticalIllnessCompanyRate;

    /**
     * 基数下限
     */
    @Column(name = "base_min", precision = 19, scale = 2)
    private BigDecimal baseMin;

    /**
     * 基数上限
     */
    @Column(name = "base_max", precision = 19, scale = 2)
    private BigDecimal baseMax;

    /**
     * 状态 (0-禁用 1-启用)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 计算个人社保合计比例
     */
    public BigDecimal getTotalPersonalRate() {
        BigDecimal total = BigDecimal.ZERO;
        if (pensionPersonalRate != null) total = total.add(pensionPersonalRate);
        if (medicalPersonalRate != null) total = total.add(medicalPersonalRate);
        if (unemploymentPersonalRate != null) total = total.add(unemploymentPersonalRate);
        if (criticalIllnessPersonalRate != null) total = total.add(criticalIllnessPersonalRate);
        return total;
    }

    /**
     * 计算企业社保合计比例
     */
    public BigDecimal getTotalCompanyRate() {
        BigDecimal total = BigDecimal.ZERO;
        if (pensionCompanyRate != null) total = total.add(pensionCompanyRate);
        if (medicalCompanyRate != null) total = total.add(medicalCompanyRate);
        if (unemploymentCompanyRate != null) total = total.add(unemploymentCompanyRate);
        if (injuryCompanyRate != null) total = total.add(injuryCompanyRate);
        if (maternityCompanyRate != null) total = total.add(maternityCompanyRate);
        if (criticalIllnessCompanyRate != null) total = total.add(criticalIllnessCompanyRate);
        return total;
    }
}
