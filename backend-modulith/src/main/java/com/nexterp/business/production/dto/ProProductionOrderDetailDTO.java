package com.nexterp.business.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 生产订单明细DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProProductionOrderDetailDTO {

    private Long id;

    private Long productionOrderId;

    private Integer lineNo;

    private Integer detailType;

    private String detailTypeName;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String specification;

    private String unit;

    private BigDecimal requiredQty;

    private BigDecimal issuedQty;

    private BigDecimal receivedQty;

    private BigDecimal unissuedQty;

    private BigDecimal unreceivedQty;

    private Long warehouseId;

    private String warehouseName;

    private String location;

    private String batchNo;

    private String remark;
}
