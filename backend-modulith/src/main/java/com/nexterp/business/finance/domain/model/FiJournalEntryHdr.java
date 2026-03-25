package com.nexterp.business.finance.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 凭证头 (Journal Entry Header)
 * 对标 SAP BKPF，按 fiscal_year 分区
 *
 * @author NextERP
 */
@Data
@Entity
@IdClass(JournalEntryId.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "fi_journal_entry_hdr")
public class FiJournalEntryHdr {

    /**
     * 凭证ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 会计年度 (分区键)
     */
    @Id
    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * 公司代码
     */
    @Column(name = "company_code", nullable = false, length = 4)
    private String companyCode;

    /**
     * 凭证号
     * 对标 SAP BKPF-BELNR
     */
    @Column(name = "document_number", nullable = false, length = 10)
    private String documentNumber;

    /**
     * 凭证类型ID
     */
    @Column(name = "document_type_id")
    private Long documentTypeId;

    /**
     * 凭证类型代码
     */
    @Column(name = "document_type_code", length = 2)
    private String documentTypeCode;

    /**
     * 凭证日期
     * 对标 SAP BKPF-BLDAT
     */
    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    /**
     * 过账日期
     * 对标 SAP BKPF-BUDAT
     */
    @Column(name = "posting_date", nullable = false)
    private LocalDate postingDate;

    /**
     * 会计期间 (1-16)
     * 对标 SAP BKPF-MONAT
     */
    @Column(name = "fiscal_period", nullable = false)
    private Integer fiscalPeriod;

    /**
     * 货币代码
     */
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    /**
     * 汇率
     */
    @Column(name = "exchange_rate", precision = 12, scale = 6)
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    /**
     * 参考号
     * 对标 SAP BKPF-XBLNR
     */
    @Column(name = "reference_number", length = 20)
    private String referenceNumber;

    /**
     * 凭证抬头文本
     * 对标 SAP BKPF-BKTXT
     */
    @Column(name = "header_text", length = 100)
    private String headerText;

    /**
     * 来源类型 (MANUAL/MM/SD/HR/FA)
     */
    @Column(name = "source_type", length = 10)
    private String sourceType;

    /**
     * 来源单据号
     */
    @Column(name = "source_document", length = 50)
    private String sourceDocument;

    /**
     * 凭证状态 (DRAFT/POSTED/REVERSED)
     */
    @Column(name = "doc_status", nullable = false, length = 10)
    @Builder.Default
    private String docStatus = "DRAFT";

    /**
     * 是否已过账
     */
    @Column(name = "is_posted", nullable = false)
    @Builder.Default
    private Boolean isPosted = false;

    /**
     * 是否已冲销
     */
    @Column(name = "is_reversed", nullable = false)
    @Builder.Default
    private Boolean isReversed = false;

    /**
     * 冲销凭证ID
     */
    @Column(name = "reversed_doc_id")
    private Long reversedDocId;

    /**
     * 冲销凭证年度
     */
    @Column(name = "reversed_fiscal_year")
    private Integer reversedFiscalYear;

    /**
     * 审批状态 (DRAFT/PENDING/APPROVED/REJECTED)
     */
    @Column(name = "approval_status", length = 10)
    @Builder.Default
    private String approvalStatus = "DRAFT";

    /**
     * 审批人
     */
    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    /**
     * 审批时间
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 借方金额合计
     */
    @Column(name = "total_debit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal totalDebit = BigDecimal.ZERO;

    /**
     * 贷方金额合计
     */
    @Column(name = "total_credit", precision = 23, scale = 2)
    @Builder.Default
    private BigDecimal totalCredit = BigDecimal.ZERO;

    /**
     * 过账人
     */
    @Column(name = "posted_by", length = 50)
    private String postedBy;

    /**
     * 过账时间
     */
    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 乐观锁版本号
     */
    @Version
    @Column(name = "lock_version")
    @Builder.Default
    private Integer lockVersion = 0;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取凭证状态名称
     */
    public String getDocStatusName() {
        return switch (docStatus) {
            case "DRAFT" -> "草稿";
            case "POSTED" -> "已过账";
            case "REVERSED" -> "已冲销";
            default -> "未知";
        };
    }

    /**
     * 判断是否平衡
     */
    public boolean isBalanced() {
        if (totalDebit == null || totalCredit == null) {
            return false;
        }
        return totalDebit.compareTo(totalCredit) == 0;
    }

    /**
     * 判断是否可以过账
     */
    public boolean canPost() {
        return "DRAFT".equals(docStatus) && !"PENDING".equals(approvalStatus) && isBalanced();
    }

    /**
     * 判断是否可以冲销
     */
    public boolean canReverse() {
        return "POSTED".equals(docStatus) && !isReversed;
    }
}
