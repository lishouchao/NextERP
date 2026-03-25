package com.nexterp.business.controlling.domain.model;

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
 * 结算规则
 * 对标: SAP COBRB
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_settlement_rule")
public class CoSettlementRule extends TenantAwareEntity {

    /**
     * 结算规则ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 内部订单ID
     */
    @Column(name = "internal_order_id", nullable = false)
    private Long internalOrderId;

    /**
     * 规则序号
     */
    @Column(name = "rule_sequence", nullable = false)
    private Integer ruleSequence;

    /**
     * 结算接收方类型 (01-成本中心 02-内部订单 03-资产 04-物料 05-G/L科目)
     */
    @Column(name = "receiver_type", nullable = false, length = 2)
    private String receiverType;

    /**
     * 结算接收方ID
     */
    @Column(name = "receiver_id")
    private Long receiverId;

    /**
     * 结算接收方代码
     */
    @Column(name = "receiver_code", length = 20)
    private String receiverCode;

    /**
     * 结算比例 (%)
     */
    @Column(name = "settlement_percentage", precision = 5, scale = 2)
    private BigDecimal settlementPercentage;

    /**
     * 结算金额
     */
    @Column(name = "settlement_amount", precision = 19, scale = 2)
    private BigDecimal settlementAmount;

    /**
     * 结算成本要素ID
     */
    @Column(name = "settlement_cost_element_id")
    private Long settlementCostElementId;

    /**
     * 结算成本要素代码
     */
    @Column(name = "settlement_cost_element_code", length = 10)
    private String settlementCostElementCode;

    /**
     * 有效起始
     */
    @Column(name = "valid_from")
    private java.time.LocalDate validFrom;

    /**
     * 有效结束
     */
    @Column(name = "valid_to")
    private java.time.LocalDate validTo;

    /**
     * 内部订单 (关联)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_order_id", insertable = false, updatable = false)
    private CoInternalOrder internalOrder;

    /**
     * 获取接收方类型名称
     */
    public String getReceiverTypeName() {
        return switch (receiverType) {
            case "01" -> "成本中心";
            case "02" -> "内部订单";
            case "03" -> "资产";
            case "04" -> "物料";
            case "05" -> "G/L科目";
            default -> "未知";
        };
    }
}
