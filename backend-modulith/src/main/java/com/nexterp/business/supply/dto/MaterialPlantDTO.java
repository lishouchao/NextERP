package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 物料工厂数据DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialPlantDTO {

    private Long id;
    private Long plantId;
    private String plantCode;
    private String statusPlant;
    private String abcIndicator;
    private String mrpType;
    private String mrpController;
    private String procurementType;
    private BigDecimal safetyStock;
    private BigDecimal reorderPoint;
    private String batchManagement;
}
