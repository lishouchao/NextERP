package com.nexterp.business.supply.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 转运订单项 (对标 SAP LTAP)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_transfer_order_itm")
public class MmTransferOrderItm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** TO头ID */
    @Column(name = "to_hdr_id", nullable = false)
    private Long toHdrId;

    /** TO项号 (TAPOS) */
    @Column(name = "to_item", nullable = false)
    private Integer toItem;

    /** 物料ID (MATNR) */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /** 物料编码 */
    @Column(name = "material_code", nullable = false, length = 18)
    private String materialCode;

    /** 批次 (CHARG) */
    @Column(name = "batch", length = 10)
    private String batch;

    /** 数量 (NSOLM) */
    @Column(name = "quantity", precision = 13, scale = 3)
    private BigDecimal quantity;

    /** 单位 (MEINS) */
    @Column(name = "unit", length = 3)
    private String unit;

    /** 源存储类型 (VLTYP) */
    @Column(name = "source_storage_type", length = 3)
    private String sourceStorageType;

    /** 源仓位 (VLPLA) */
    @Column(name = "source_storage_bin", length = 10)
    private String sourceStorageBin;

    /** 目标存储类型 (NLTYT) */
    @Column(name = "dest_storage_type", length = 3)
    private String destStorageType;

    /** 目标仓位 (NLPLA) */
    @Column(name = "dest_storage_bin", length = 10)
    private String destStorageBin;

    /** 状态: 0-创建, 1-确认中, 2-已确认 */
    @Column(name = "status", length = 1)
    @Builder.Default
    private String status = "0";

    /** TO头关联 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_hdr_id", insertable = false, updatable = false)
    private MmTransferOrderHdr toHdr;
}
