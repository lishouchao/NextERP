package com.nexterp.business.sales.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingItemDTO {

    private Long id;
    private Integer itemNumber;
    private Long materialId;
    private String materialCode;
    private String description;

    private BigDecimal billedQty;
    private String salesUnit;
    private BigDecimal netPrice;
    private BigDecimal netValue;

    private String taxCode;
    private BigDecimal taxAmount;
    private BigDecimal grossValue;
    private BigDecimal costValue;

    private Long deliveryId;
    private Long deliveryItemId;
}
