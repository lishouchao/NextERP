package com.nexterp.business.supply.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购申请头
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_purchase_req_hdr")
public class MmPurchaseReqHdr extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 采购申请号 */
    @Column(name = "pr_number", nullable = false, length = 10, unique = true)
    private String prNumber;

    /** 采购申请类型 */
    @Column(name = "pr_type", length = 4)
    @Builder.Default
    private String prType = "NB";

    /** 采购组 */
    @Column(name = "purchasing_group", length = 3)
    private String purchasingGroup;

    /** 采购组织 */
    @Column(name = "purchasing_org", length = 4)
    private String purchasingOrg;

    /** 工厂ID */
    @Column(name = "plant_id")
    private Long plantId;

    /** 工厂代码 */
    @Column(name = "plant_code", length = 4)
    private String plantCode;

    /** 凭证日期 */
    @Column(name = "document_date")
    private LocalDate documentDate;

    /** 交货日期 */
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    /** 总价值 */
    @Column(name = "total_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalValue = BigDecimal.ZERO;

    /** 币种 */
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "CNY";

    /** 头文本 */
    @Column(name = "header_text", length = 50)
    private String headerText;

    /** 状态: 0-草稿, 1-已审批, 2-已转订单, 3-已关闭 */
    @Column(name = "status", length = 1)
    @Builder.Default
    private String status = "0";

    /** 审批状态: 0-未审批, 1-审批中, 2-已审批, 3-已拒绝 */
    @Column(name = "approval_status", length = 1)
    @Builder.Default
    private String approvalStatus = "0";

    /** 审批人 */
    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    /** 审批日期 */
    @Column(name = "approved_date")
    private LocalDate approvedDate;

    /** 采购申请项 */
    @OneToMany(mappedBy = "reqHdr", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MmPurchaseReqItm> items = new ArrayList<>();
}
