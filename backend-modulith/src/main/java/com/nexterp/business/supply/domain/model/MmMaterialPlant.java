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
 * 物料工厂数据 (对标 SAP MARC)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mm_material_plant")
public class MmMaterialPlant extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物料ID (FK to MmMaterial) */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /** 物料编码 */
    @Column(name = "material_number", nullable = false, length = 18)
    private String materialNumber;

    /** 工厂ID */
    @Column(name = "plant_id", nullable = false)
    private Long plantId;

    /** 工厂代码 */
    @Column(name = "plant_code", nullable = false, length = 4)
    private String plantCode;

    /** 工厂状态 */
    @Column(name = "status_plant", length = 1)
    @Builder.Default
    private String statusPlant = "A";

    /** ABC标识 */
    @Column(name = "abc_indicator", length = 1)
    private String abcIndicator;

    /** MRP类型 */
    @Column(name = "mrp_type", length = 4)
    private String mrpType;

    /** MRP控制者 */
    @Column(name = "mrp_controller", length = 3)
    private String mrpController;

    /** 批量过程 */
    @Column(name = "lot_size_procedure", length = 4)
    private String lotSizeProcedure;

    /** 最小批量 */
    @Column(name = "min_lot_size", precision = 13, scale = 3)
    private BigDecimal minLotSize;

    /** 最大批量 */
    @Column(name = "max_lot_size", precision = 13, scale = 3)
    private BigDecimal maxLotSize;

    /** 安全库存 */
    @Column(name = "safety_stock", precision = 13, scale = 3)
    private BigDecimal safetyStock;

    /** 再订货点 */
    @Column(name = "reorder_point", precision = 13, scale = 3)
    private BigDecimal reorderPoint;

    /** 计划交货时间 (天) */
    @Column(name = "planned_deliv_time")
    private Integer plannedDelivTime;

    /** 采购类型 (E:外部/F:内部) */
    @Column(name = "procurement_type", length = 1)
    private String procurementType;

    /** 库存地点 */
    @Column(name = "storage_location", length = 4)
    private String storageLocation;

    /** 可用性检查 */
    @Column(name = "availability_check", length = 2)
    private String availabilityCheck;

    /** 批次管理 */
    @Column(name = "batch_management", length = 1)
    @Builder.Default
    private String batchManagement = "0";

    /** 利润中心 */
    @Column(name = "profit_center", length = 10)
    private String profitCenter;

    /** 物料主记录 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", insertable = false, updatable = false)
    private MmMaterial material;
}
