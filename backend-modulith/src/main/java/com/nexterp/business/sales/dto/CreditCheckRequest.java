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
public class CreditCheckRequest {

    private Long tenantId;
    private Long customerId;
    private Long companyId;
    private String checkType;
    private String documentType;
    private Long documentId;
    private String documentNumber;
    private BigDecimal checkAmount;
}
