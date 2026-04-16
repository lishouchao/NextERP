package com.nexterp.business.supply.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 仓位 (对标 SAP LQUA)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_storage_bin")
public class MmStorageBin extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 仓库号 (LGNUM) */
    @Column(name = "warehouse_number", nullable = false, length = 3)
    private String warehouseNumber;

    /** 存储类型 (LGTYP) */
    @Column(name = "storage_type", nullable = false, length = 3)
    private String storageType;

    /** 仓位 (LGPLA) */
    @Column(name = "storage_bin", nullable = false, length = 10)
    private String storageBin;

    /** 存储区域 (LGBER) */
    @Column(name = "storage_section", length = 4)
    private String storageSection;

    /** 仓位类型 (PLATY) */
    @Column(name = "bin_type", length = 1)
    private String binType;

    /** 物料ID (MATNR) */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /** 物料编码 */
    @Column(name = "material_code", nullable = false, length = 18)
    private String materialCode;

    /** 批次 (CHARG) */
    @Column(name = "batch", length = 10)
    private String batch;

    /** 库存类别 (BESTQ) */
    @Column(name = "stock_category", length = 1)
    private String stockCategory;

    /** 数量 (GESME) */
    @Column(name = "quantity", precision = 13, scale = 3)
    private BigDecimal quantity;

    /** 单位 (MEINS) */
    @Column(name = "unit", length = 3)
    private String unit;

    /** 可用数量 (VERME) */
    @Column(name = "quantity_available", precision = 13, scale = 3)
    private BigDecimal quantityAvailable;

    /** 拣配数量 (PICKD) */
    @Column(name = "quantity_picking", precision = 13, scale = 3)
    private BigDecimal quantityPicking;

    /** 阻塞标识 (SKZUA) */
    @Column(name = "blocked", length = 1)
    @Builder.Default
    private String blocked = "0";
}
