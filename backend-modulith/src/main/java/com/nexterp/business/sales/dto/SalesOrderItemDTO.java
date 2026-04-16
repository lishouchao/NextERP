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
public class SalesOrderItemDTO {

    private Long id;
    private Integer itemNumber;
    private Long materialId;
    private String materialCode;
    private String description;

    private BigDecimal orderedQty;
    private BigDecimal deliveredQty;
    private BigDecimal invoicedQty;

    private String salesUnit;
    private BigDecimal netPrice;
    private BigDecimal netValue;

    private Long plantId;
    private Long slocId;
    private String itemCategory;
}
