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
public class BillingDTO {

    private Long id;
    private String billingNumber;
    private String billingType;
    private Long salesOrgId;
    private String distributionChannel;
    private String division;

    private Long soldToParty;
    private Long billToParty;
    private Long payerParty;

    private LocalDate documentDate;
    private LocalDate billingDate;

    private BigDecimal netValue;
    private BigDecimal taxAmount;
    private BigDecimal grossValue;

    private String paymentTerm;
    private LocalDate paymentDueDate;
    private String billingStatus;

    private Long deliveryId;
    private Long orderId;
    private String remark;

    private List<BillingItemDTO> items;
}
