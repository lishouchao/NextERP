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
 * 定价条件记录 (对标 SAP KONP)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sd_condition")
public class SdCondition extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 条件类型 (KSCHL): PR00-价格, PR01-促销价, K004-客户折扣, K005-物料折扣, K007-附加费, KF00-运费, MWST-税, SKTO-现金折扣 */
    @Column(name = "condition_type", nullable = false, length = 4)
    private String conditionType;

    /** 条件记录号 (KNUMH) */
    @Column(name = "condition_record", nullable = false, length = 10, unique = true)
    private String conditionRecord;

    /** 条件项 (KOPOS) */
    @Column(name = "condition_item")
    @Builder.Default
    private Integer conditionItem = 1;

    /** 金额 (KBETR) */
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    /** 比率/百分比 */
    @Column(name = "rate", precision = 9, scale = 5)
    private BigDecimal rate;

    /** 价格单位 (KPEIN) */
    @Column(name = "price_unit")
    @Builder.Default
    private Integer priceUnit = 1;

    /** 计算类型 (KRECH): A-百分比, B-固定金额, C-数量 */
    @Column(name = "calculation_type", nullable = false, length = 1)
    private String calculationType;

    /** 有效开始 (DATAB) */
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    /** 有效结束 (DATBI) */
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    /** 销售组织 (VKORG) */
    @Column(name = "sales_org_id")
    private Long salesOrgId;

    /** 分销渠道 */
    @Column(name = "distribution_channel", length = 2)
    private String distributionChannel;

    /** 客户 (KUNNR) */
    @Column(name = "customer_id")
    private Long customerId;

    /** 物料 (MATNR) */
    @Column(name = "material_id")
    private Long materialId;
}
