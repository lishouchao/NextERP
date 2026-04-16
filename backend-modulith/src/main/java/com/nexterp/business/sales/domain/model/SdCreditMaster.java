package com.nexterp.business.sales.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 客户信用主数据 (对标 SAP FD32)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sd_credit_master")
public class SdCreditMaster extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 客户ID (KUNNR) */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** 公司代码 (BUKRS) */
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    /** 信用额度 (KLIMK) */
    @Column(name = "credit_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditLimit;

    /** 已用额度 (SKFOR) */
    @Column(name = "used_limit", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal usedLimit = BigDecimal.ZERO;

    /** 可用额度 */
    @Column(name = "available_limit", precision = 15, scale = 2)
    private BigDecimal availableLimit;

    /** 风险类别 (CTLPC): 1-低风险, 2-中风险, 3-高风险 */
    @Column(name = "risk_class", nullable = false, length = 1)
    @Builder.Default
    private String riskClass = "2";

    /** 信用组 (KKBER) */
    @Column(name = "credit_group", length = 4)
    private String creditGroup;

    /** 信用状态: 01-正常, 02-预警, 03-冻结 */
    @Column(name = "credit_status", nullable = false, length = 2)
    @Builder.Default
    private String creditStatus = "01";

    /** 检查规则: 1-简单检查, 2-复杂检查 */
    @Column(name = "check_rule", length = 1)
    @Builder.Default
    private String checkRule = "1";

    /** 上次检查日期 */
    @Column(name = "last_check_date")
    private LocalDate lastCheckDate;

    /** 下次检查日期 */
    @Column(name = "next_check_date")
    private LocalDate nextCheckDate;
}
