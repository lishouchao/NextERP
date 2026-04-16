package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 仓位DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageBinDTO {

    private Long id;
    private String warehouseNumber;
    private String storageType;
    private String storageBin;
    private String storageSection;
    private String binType;
    private Long materialId;
    private String materialCode;
    private String batch;
    private String stockCategory;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal quantityAvailable;
    private BigDecimal quantityPicking;
    private String blocked;
}
