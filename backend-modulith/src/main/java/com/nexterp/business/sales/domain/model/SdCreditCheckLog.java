package com.nexterp.business.sales.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信用检查日志
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sd_credit_check_log")
public class SdCreditCheckLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 租户ID */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** 客户ID */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** 公司代码 */
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    /** 检查类型: 01-订单检查, 02-交货检查, 03-发货检查, 04-开票检查 */
    @Column(name = "check_type", nullable = false, length = 2)
    private String checkType;

    /** 单据类型: SO-销售订单, DN-交货单 */
    @Column(name = "document_type", length = 2)
    private String documentType;

    /** 单据ID */
    @Column(name = "document_id")
    private Long documentId;

    /** 单据号 */
    @Column(name = "document_number", length = 10)
    private String documentNumber;

    /** 检查金额 */
    @Column(name = "check_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal checkAmount;

    /** 检查前已用额度 */
    @Column(name = "used_before", precision = 15, scale = 2)
    private BigDecimal usedBefore;

    /** 检查后已用额度 */
    @Column(name = "used_after", precision = 15, scale = 2)
    private BigDecimal usedAfter;

    /** 信用额度 */
    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    /** 使用率 */
    @Column(name = "usage_rate", precision = 5, scale = 2)
    private BigDecimal usageRate;

    /** 检查结果: OK-通过, WA-警告, BL-阻止 */
    @Column(name = "check_result", nullable = false, length = 2)
    private String checkResult;

    /** 结果说明 */
    @Column(name = "result_message", length = 500)
    private String resultMessage;

    /** 检查时间 */
    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;
}
