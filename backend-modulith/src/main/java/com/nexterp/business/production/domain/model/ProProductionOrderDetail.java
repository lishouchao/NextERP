package com.nexterp.business.production.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产订单明细
 *
 * @author NextERP
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pro_production_order_detail")
public class ProProductionOrderDetail extends BaseEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 生产订单ID
     */
    @Column(name = "production_order_id", nullable = false)
    private Long productionOrderId;

    /**
     * 行号
     */
    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    /**
     * 明细类型 (1-子件 2-副产品 3-联产品)
     */
    @Column(name = "detail_type", nullable = false)
    private Integer detailType;

    /**
     * 物料ID
     */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /**
     * 物料编码
     */
    @Column(name = "material_code", length = 50)
    private String materialCode;

    /**
     * 物料名称
     */
    @Column(name = "material_name", length = 100)
    private String materialName;

    /**
     * 规格型号
     */
    @Column(name = "specification", length = 200)
    private String specification;

    /**
     * 单位
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * 需求数量
     */
    @Column(name = "required_qty", precision = 19, scale = 4)
    private BigDecimal requiredQty;

    /**
     * 领料数量
     */
    @Column(name = "issued_qty", precision = 19, scale = 4)
    private BigDecimal issuedQty;

    /**
     * 入库数量
     */
    @Column(name = "received_qty", precision = 19, scale = 4)
    private BigDecimal receivedQty;

    /**
     * 仓库ID
     */
    @Column(name = "warehouse_id")
    private Long warehouseId;

    /**
     * 仓库名称
     */
    @Column(name = "warehouse_name", length = 100)
    private String warehouseName;

    /**
     * 库位
     */
    @Column(name = "location", length = 50)
    private String location;

    /**
     * 批号
     */
    @Column(name = "batch_no", length = 50)
    private String batchNo;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 关联生产订单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", insertable = false, updatable = false)
    private ProProductionOrder productionOrder;

    /**
     * 获取明细类型名称
     */
    public String getDetailTypeName() {
        return switch (detailType) {
            case 1 -> "子件";
            case 2 -> "副产品";
            case 3 -> "联产品";
            default -> "未知";
        };
    }

    /**
     * 计算未领数量
     *
     * @return 未领数量
     */
    public BigDecimal getUnissuedQty() {
        BigDecimal required = requiredQty != null ? requiredQty : BigDecimal.ZERO;
        BigDecimal issued = issuedQty != null ? issuedQty : BigDecimal.ZERO;
        return required.subtract(issued);
    }

    /**
     * 计算未入库数量
     *
     * @return 未入库数量
     */
    public BigDecimal getUnreceivedQty() {
        BigDecimal required = requiredQty != null ? requiredQty : BigDecimal.ZERO;
        BigDecimal received = receivedQty != null ? receivedQty : BigDecimal.ZERO;
        return required.subtract(received);
    }
}
