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
 * 转运订单头 (对标 SAP LTAK)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_transfer_order_hdr")
public class MmTransferOrderHdr extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 转运订单号 (TANUM) */
    @Column(name = "transfer_order", nullable = false, length = 10, unique = true)
    private String transferOrder;

    /** 仓库号 (LGNUM) */
    @Column(name = "warehouse_number", nullable = false, length = 3)
    private String warehouseNumber;

    /** TO类型: P-拣配, S-上架 */
    @Column(name = "to_type", nullable = false, length = 1)
    private String toType;

    /** 转运需求号 (TBNUM) */
    @Column(name = "transfer_requirement", length = 10)
    private String transferRequirement;

    /** 物料凭证号 (MBLNR) */
    @Column(name = "material_document", length = 10)
    private String materialDocument;

    /** 状态: 0-创建, 1-确认中, 2-已确认 */
    @Column(name = "status", length = 1)
    @Builder.Default
    private String status = "0";

    /** 确认日期 (QUEDT) */
    @Column(name = "confirmed_date")
    private LocalDate confirmedDate;

    /** 确认人 (QNAME) */
    @Column(name = "confirmed_by", length = 50)
    private String confirmedBy;

    /** 转运订单项 */
    @OneToMany(mappedBy = "toHdr", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MmTransferOrderItm> items = new ArrayList<>();
}
