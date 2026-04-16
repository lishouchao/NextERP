package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购订单项DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemDTO {

    private Long id;
    private Integer poItem;
    private Long materialId;
    private String materialCode;
    private String shortText;
    private String materialGroup;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal price;
    private Integer priceUnit;
    private BigDecimal netValue;
    private String taxCode;
    private BigDecimal taxAmount;
    private Long plantId;
    private String plantCode;
    private Long slocId;
    private String slocCode;
    private LocalDate deliveryDate;
    private BigDecimal quantityDelivered;
    private BigDecimal quantityInvoiced;
    private String itemCategory;
    private String acctAssignmentCat;
    private String costCenter;
    private String deletionFlag;
}
