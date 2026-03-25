package com.nexterp.business.finance.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 税码 (Tax Code)
 * 对标 SAP T007A (税码)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fi_tax_code", uniqueConstraints = {
    @UniqueConstraint(name = "uk_taxcode_tenant_code", columnNames = {"tenant_id", "tax_code"})
})
public class FiTaxCode extends TenantAwareEntity {

    /**
     * 税码ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 税码 (2位)
     * 对标 SAP T007A-MWSKZ
     */
    @Column(name = "tax_code", nullable = false, length = 2)
    private String taxCode;

    /**
     * 税码名称
     */
    @Column(name = "tax_name", nullable = false, length = 100)
    private String taxName;

    /**
     * 税码名称 (英文)
     */
    @Column(name = "tax_name_en", length = 100)
    private String taxNameEn;

    /**
     * 税类型 (01-进项税 02-销项税 03-非课税 04-免税)
     * 对标 SAP T007A-KTOSL
     */
    @Column(name = "tax_type", length = 2, nullable = false)
    private String taxType;

    /**
     * 税率 (%)
     */
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    /**
     * 国家代码
     */
    @Column(name = "country_code", length = 2)
    private String countryCode;

    /**
     * 税分类 (V-VAT C-消费税 B-营业税)
     */
    @Column(name = "tax_category", length = 1)
    private String taxCategory;

    /**
     * 税科目ID (进项税)
     */
    @Column(name = "input_tax_account_id")
    private Long inputTaxAccountId;

    /**
     * 税科目代码 (进项税)
     */
    @Column(name = "input_tax_account_code", length = 10)
    private String inputTaxAccountCode;

    /**
     * 税科目ID (销项税)
     */
    @Column(name = "output_tax_account_id")
    private Long outputTaxAccountId;

    /**
     * 税科目代码 (销项税)
     */
    @Column(name = "output_tax_account_code", length = 10)
    private String outputTaxAccountCode;

    /**
     * 计税方式 (01-含税价 02-不含税价)
     */
    @Column(name = "calc_method", length = 2)
    @Builder.Default
    private String calcMethod = "02";

    /**
     * 是否允许手工输入税额
     */
    @Column(name = "allow_manual_tax", nullable = false)
    @Builder.Default
    private Boolean allowManualTax = false;

    /**
     * 生效日期
     */
    @Column(name = "valid_from")
    private java.time.LocalDate validFrom;

    /**
     * 失效日期
     */
    @Column(name = "valid_to")
    private java.time.LocalDate validTo;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 状态 (1-启用 0-禁用)
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
     * 获取税类型名称
     */
    public String getTaxTypeName() {
        return switch (taxType) {
            case "01" -> "进项税";
            case "02" -> "销项税";
            case "03" -> "非课税";
            case "04" -> "免税";
            default -> "未知";
        };
    }

    /**
     * 判断当前是否有效
     */
    public boolean isValid() {
        java.time.LocalDate now = java.time.LocalDate.now();
        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }
        if (validTo != null && now.isAfter(validTo)) {
            return false;
        }
        return true;
    }
}
