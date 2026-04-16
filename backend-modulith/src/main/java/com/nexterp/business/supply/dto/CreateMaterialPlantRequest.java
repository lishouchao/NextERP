package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建物料工厂视图请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaterialPlantRequest {

    private Long plantId;
    private String plantCode;
    private String abcIndicator;
    private String mrpType;
    private String mrpController;
    private String lotSizeProcedure;
    private BigDecimal minLotSize;
    private BigDecimal maxLotSize;
    private BigDecimal safetyStock;
    private BigDecimal reorderPoint;
    private Integer plannedDelivTime;
    private String procurementType;
    private String storageLocation;
    private String availabilityCheck;
    private String batchManagement;
    private String profitCenter;
}
