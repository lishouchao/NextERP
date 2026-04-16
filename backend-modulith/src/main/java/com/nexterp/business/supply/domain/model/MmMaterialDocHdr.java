package com.nexterp.business.supply.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 物料凭证头 (对标 SAP MKPF)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_material_doc_hdr")
public class MmMaterialDocHdr extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物料凭证号 (MBLNR) */
    @Column(name = "material_document", nullable = false, length = 10, unique = true)
    private String materialDocument;

    /** 会计年度 (MJAHR) */
    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    /** 过账日期 (BUDAT) */
    @Column(name = "posting_date", nullable = false)
    private LocalDate postingDate;

    /** 凭证日期 (BLDAT) */
    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    /** 移动类型 (BWART) */
    @Column(name = "movement_type", nullable = false, length = 3)
    private String movementType;

    /** 事务代码 (TCODE) */
    @Column(name = "transaction_code", length = 20)
    private String transactionCode;

    /** 头文本 (BKTXT) */
    @Column(name = "header_text", length = 50)
    private String headerText;

    /** 参考凭证号 (XBLNR) */
    @Column(name = "ref_document_no", length = 16)
    private String refDocumentNo;

    /** 物料凭证项 */
    @OneToMany(mappedBy = "docHdr", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineItem ASC")
    @Builder.Default
    private List<MmMaterialDocItm> items = new ArrayList<>();
}
