package com.nexterp.business.finance.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 财务凭证
 *
 * @author NextERP
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fin_voucher")
public class FinVoucher extends TenantAwareEntity {

    /**
     * 凭证号
     */
    @Column(name = "voucher_no", nullable = false, length = 50)
    private String voucherNo;

    /**
     * 凭证字 (记、收、付、转)
     */
    @Column(name = "voucher_word", length = 10)
    private String voucherWord;

    /**
     * 凭证日期
     */
    @Column(name = "voucher_date", nullable = false)
    private LocalDate voucherDate;

    /**
     * 会计期间 (格式: YYYY-MM)
     */
    @Column(name = "accounting_period", nullable = false, length = 10)
    private String accountingPeriod;

    /**
     * 凭证类型 (1-收款 2-付款 3-转账)
     */
    @Column(name = "voucher_type", nullable = false)
    private Integer voucherType;

    /**
     * 附件数量
     */
    @Column(name = "attachment_count")
    private Integer attachmentCount;

    /**
     * 借方金额合计
     */
    @Column(name = "debit_amount", precision = 19, scale = 2)
    private BigDecimal debitAmount;

    /**
     * 贷方金额合计
     */
    @Column(name = "credit_amount", precision = 19, scale = 2)
    private BigDecimal creditAmount;

    /**
     * 制单人ID
     */
    @Column(name = "created_by_id")
    private Long createdById;

    /**
     * 制单人姓名
     */
    @Column(name = "created_by_name", length = 50)
    private String createdByName;

    /**
     * 审核人ID
     */
    @Column(name = "approved_by_id")
    private Long approvedById;

    /**
     * 审核人姓名
     */
    @Column(name = "approved_by_name", length = 50)
    private String approvedByName;

    /**
     * 审核时间
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 记账人ID
     */
    @Column(name = "posted_by_id")
    private Long postedById;

    /**
     * 记账人姓名
     */
    @Column(name = "posted_by_name", length = 50)
    private String postedByName;

    /**
     * 记账时间
     */
    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    /**
     * 凭证状态 (0-草稿 1-待审核 2-已审核 3-已记账 4-已驳回)
     */
    @Column(name = "voucher_status", nullable = false)
    private Integer voucherStatus;

    /**
     * 驳回原因
     */
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    /**
     * 摘要
     */
    @Column(name = "summary", length = 500)
    private String summary;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 来源单据类型
     */
    @Column(name = "source_type", length = 50)
    private String sourceType;

    /**
     * 来源单据ID
     */
    @Column(name = "source_id")
    private Long sourceId;

    /**
     * 凭证分录列表
     */
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNo ASC")
    private List<FinVoucherEntry> entries = new ArrayList<>();

    /**
     * 获取凭证类型名称
     */
    public String getVoucherTypeName() {
        return switch (voucherType) {
            case 1 -> "收款";
            case 2 -> "付款";
            case 3 -> "转账";
            default -> "未知";
        };
    }

    /**
     * 获取凭证状态名称
     */
    public String getVoucherStatusName() {
        return switch (voucherStatus) {
            case 0 -> "草稿";
            case 1 -> "待审核";
            case 2 -> "已审核";
            case 3 -> "已记账";
            case 4 -> "已驳回";
            default -> "未知";
        };
    }

    /**
     * 判断是否已审核
     */
    public boolean isApproved() {
        return voucherStatus >= 2;
    }

    /**
     * 判断是否已记账
     */
    public boolean isPosted() {
        return voucherStatus == 3;
    }

    /**
     * 判断是否可以编辑
     */
    public boolean canEdit() {
        return voucherStatus == 0 || voucherStatus == 4;
    }

    /**
     * 判断是否可以审核
     */
    public boolean canApprove() {
        return voucherStatus == 1;
    }

    /**
     * 判断是否可以记账
     */
    public boolean canPost() {
        return voucherStatus == 2;
    }

    /**
     * 判断是否可以反记账
     */
    public boolean canUnpost() {
        return voucherStatus == 3;
    }

    /**
     * 判断是否平衡
     */
    public boolean isBalanced() {
        if (debitAmount == null || creditAmount == null) {
            return false;
        }
        return debitAmount.compareTo(creditAmount) == 0;
    }
}
