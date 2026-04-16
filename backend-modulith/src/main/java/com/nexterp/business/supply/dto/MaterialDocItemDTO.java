package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 物料凭证项DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDocItemDTO {

    private Long id;
    private Integer lineItem;
    private Long materialId;
    private String materialCode;
    private Long plantId;
    private String plantCode;
    private Long slocId;
    private String slocCode;
    private String batch;
    private String movementType;
    private String stockType;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal amount;
    private String purchaseOrder;
    private Integer poItem;
    private Long vendorId;
    private Long customerId;
    private String salesOrder;
    private String costCenter;
    private String reasonForMovement;
    private String itemText;
}
