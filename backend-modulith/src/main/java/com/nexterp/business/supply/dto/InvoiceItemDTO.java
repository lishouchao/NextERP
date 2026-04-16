package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 发票项DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDTO {

    private Long id;
    private Integer lineItem;
    private String purchaseOrder;
    private Integer poItem;
    private Long materialId;
    private String materialCode;
    private String shortText;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal price;
    private BigDecimal netAmount;
    private String taxCode;
    private BigDecimal taxAmount;
    private Long plantId;
    private String plantCode;
    private String costCenter;
    private String itemText;
}
