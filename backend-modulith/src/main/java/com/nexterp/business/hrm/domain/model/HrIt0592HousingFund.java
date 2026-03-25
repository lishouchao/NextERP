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
 * InfoType 0592 - 公积金信息 (Housing Fund)
 *
 * 员工住房公积金缴纳信息
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it0592_housing_fund", indexes = {
    @Index(name = "idx_it0592_employee", columnList = "tenant_id, employee_id, valid_from")
})
public class HrIt0592HousingFund extends TimeValidEntity {

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
     * 公积金类型 (1-住房公积金 2-补充公积金)
     */
    @Column(name = "fund_type", nullable = false, length = 1)
    @Builder.Default
    private String fundType = "1";

    /**
     * 公积金中心代码
     */
    @Column(name = "fund_center_code", length = 10)
    private String fundCenterCode;

    /**
     * 公积金中心名称
     */
    @Column(name = "fund_center_name", length = 100)
    private String fundCenterName;

    /**
     * 公积金基数
     */
    @Column(name = "fund_base", nullable = false, precision = 19, scale = 2)
    private BigDecimal fundBase;

    /**
     * 个人比例 (%)
     */
    @Column(name = "personal_rate", precision = 5, scale = 2)
    private BigDecimal personalRate;

    /**
     * 企业比例 (%)
     */
    @Column(name = "company_rate", precision = 5, scale = 2)
    private BigDecimal companyRate;

    /**
     * 个人缴纳金额
     */
    @Column(name = "personal_amount", precision = 19, scale = 2)
    private BigDecimal personalAmount;

    /**
     * 企业缴纳金额
     */
    @Column(name = "company_amount", precision = 19, scale = 2)
    private BigDecimal companyAmount;

    /**
     * 合计金额
     */
    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 公积金账号
     */
    @Column(name = "fund_account", length = 30)
    private String fundAccount;

    /**
     * 缴存状态 (1-正常缴存 2-封存 3-销户)
     */
    @Column(name = "fund_status", length = 1)
    @Builder.Default
    private String fundStatus = "1";

    /**
     * 首次缴存日期
     */
    @Column(name = "first_deposit_date")
    private LocalDate firstDepositDate;

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
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 计算个人缴纳金额
     */
    public BigDecimal calculatePersonalAmount() {
        if (fundBase == null || personalRate == null) {
            return BigDecimal.ZERO;
        }
        return fundBase.multiply(personalRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 计算企业缴纳金额
     */
    public BigDecimal calculateCompanyAmount() {
        if (fundBase == null || companyRate == null) {
            return BigDecimal.ZERO;
        }
        return fundBase.multiply(companyRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 计算合计
     */
    public BigDecimal calculateTotalAmount() {
        return calculatePersonalAmount().add(calculateCompanyAmount());
    }

    /**
     * 获取公积金类型名称
     */
    public String getFundTypeName() {
        return "1".equals(fundType) ? "住房公积金" : "补充公积金";
    }
}
