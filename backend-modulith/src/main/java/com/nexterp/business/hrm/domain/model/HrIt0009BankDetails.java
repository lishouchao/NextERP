package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * InfoType 0009 - 银行信息 (Bank Details)
 * 对标 SAP IT0009
 *
 * 存储员工的银行账户信息，用于薪资发放
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it0009_bank_details", indexes = {
    @Index(name = "idx_it0009_employee", columnList = "tenant_id, employee_id, valid_from"),
    @Index(name = "idx_it0009_type", columnList = "tenant_id, employee_id, bank_type")
})
public class HrIt0009BankDetails extends TimeValidEntity {

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
     * 银行类型 (1-工资卡 2-奖金卡 3-报销卡 4-其他)
     */
    @Column(name = "bank_type", nullable = false, length = 1)
    @Builder.Default
    private String bankType = "1";

    /**
     * 银行代码 (央行联行号前3位)
     */
    @Column(name = "bank_code", length = 10)
    private String bankCode;

    /**
     * 银行名称
     */
    @Column(name = "bank_name", length = 100)
    private String bankName;

    /**
     * 银行联行号 (12位)
     */
    @Column(name = "bank_cnaps", length = 12)
    private String bankCnaps;

    /**
     * 开户行名称
     */
    @Column(name = "branch_name", length = 200)
    private String branchName;

    /**
     * 开户行所在省份
     */
    @Column(name = "branch_province", length = 50)
    private String branchProvince;

    /**
     * 开户行所在城市
     */
    @Column(name = "branch_city", length = 50)
    private String branchCity;

    /**
     * 银行账户类型 (1-借记卡 2-信用卡 3-存折)
     */
    @Column(name = "account_type", length = 1)
    @Builder.Default
    private String accountType = "1";

    /**
     * 银行账号
     */
    @Column(name = "bank_account", nullable = false, length = 30)
    private String bankAccount;

    /**
     * 户名
     */
    @Column(name = "account_holder", length = 50)
    private String accountHolder;

    /**
     * 币种
     */
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "CNY";

    /**
     * 是否主要账户 (用于薪资发放)
     */
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = true;

    /**
     * 验证状态 (0-未验证 1-已验证 2-验证失败)
     */
    @Column(name = "verify_status", length = 1)
    @Builder.Default
    private String verifyStatus = "0";

    /**
     * 验证时间
     */
    @Column(name = "verify_time")
    private java.time.LocalDateTime verifyTime;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取银行类型名称
     */
    public String getBankTypeName() {
        return switch (bankType) {
            case "1" -> "工资卡";
            case "2" -> "奖金卡";
            case "3" -> "报销卡";
            case "4" -> "其他";
            default -> "未知";
        };
    }

    /**
     * 获取脱敏账号 (仅显示后4位)
     */
    public String getMaskedAccount() {
        if (bankAccount == null || bankAccount.length() < 4) {
            return bankAccount;
        }
        return "****" + bankAccount.substring(bankAccount.length() - 4);
    }
}
