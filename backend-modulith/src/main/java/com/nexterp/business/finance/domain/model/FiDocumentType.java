package com.nexterp.business.finance.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * 凭证类型 (Document Type)
 * 对标 SAP T003 (凭证类型)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fi_document_type", uniqueConstraints = {
    @UniqueConstraint(name = "uk_doctype_tenant_code", columnNames = {"tenant_id", "doc_type_code"})
})
public class FiDocumentType extends TenantAwareEntity {

    /**
     * 凭证类型ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 凭证类型代码 (2位)
     * 对标 SAP T003-BLART
     */
    @Column(name = "doc_type_code", nullable = false, length = 2)
    private String docTypeCode;

    /**
     * 凭证类型名称
     */
    @Column(name = "doc_type_name", nullable = false, length = 100)
    private String docTypeName;

    /**
     * 凭证类型名称 (英文)
     */
    @Column(name = "doc_type_name_en", length = 100)
    private String docTypeNameEn;

    /**
     * 凭证类型分类
     * 01-收款凭证 02-付款凭证 03-转账凭证 04-通用凭证 05-调整凭证 06-结账凭证
     */
    @Column(name = "doc_type_class", length = 2, nullable = false)
    private String docTypeClass;

    /**
     * 借贷标识 (D-借方 C-贷方 N-无限制)
     * 对标 SAP T003-XBLDR
     */
    @Column(name = "debit_credit_ind", length = 1, nullable = false)
    @Builder.Default
    private String debitCreditInd = "N";

    /**
     * 编号范围代码
     * 对标 SAP T003-NUMKR
     */
    @Column(name = "number_range_code", length = 2)
    private String numberRangeCode;

    /**
     * 凭证编号策略 (01-按公司 02-按年度 03-按期间)
     */
    @Column(name = "numbering_strategy", length = 2)
    @Builder.Default
    private String numberingStrategy = "02";

    /**
     * 是否允许手工凭证号
     */
    @Column(name = "allow_manual_no", nullable = false)
    @Builder.Default
    private Boolean allowManualNo = false;

    /**
     * 审批流程代码
     */
    @Column(name = "approval_wf_code", length = 20)
    private String approvalWfCode;

    /**
     * 是否需要审批
     */
    @Column(name = "is_approval_required", nullable = false)
    @Builder.Default
    private Boolean isApprovalRequired = false;

    /**
     * 是否允许冲销
     */
    @Column(name = "is_reversible", nullable = false)
    @Builder.Default
    private Boolean isReversible = true;

    /**
     * 冲销凭证类型代码
     */
    @Column(name = "reverse_doc_type", length = 2)
    private String reverseDocType;

    /**
     * 凭证字 (记/收/付/转)
     */
    @Column(name = "voucher_word", length = 4)
    private String voucherWord;

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
     * 获取凭证类型分类名称
     */
    public String getDocTypeClassName() {
        return switch (docTypeClass) {
            case "01" -> "收款凭证";
            case "02" -> "付款凭证";
            case "03" -> "转账凭证";
            case "04" -> "通用凭证";
            case "05" -> "调整凭证";
            case "06" -> "结账凭证";
            default -> "未知";
        };
    }
}
