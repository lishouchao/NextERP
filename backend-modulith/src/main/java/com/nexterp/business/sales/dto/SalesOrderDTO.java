package com.nexterp.business.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderDTO {

    private Long id;
    private String orderNumber;
    private String orderType;
    private Long salesOrgId;
    private String distributionChannel;
    private String division;

    private Long soldToParty;
    private Long shipToParty;
    private Long billToParty;
    private Long payerParty;

    private LocalDate documentDate;
    private LocalDate requestedDeliveryDate;

    private BigDecimal netValue;
    private BigDecimal taxAmount;
    private BigDecimal grossValue;

    private String orderStatus;
    private String deliveryStatus;
    private String billingStatus;

    private String purchaseOrder;
    private String remark;

    private List<SalesOrderItemDTO> items;
}
