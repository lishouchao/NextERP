package com.nexterp.business.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditMasterDTO {

    private Long id;
    private Long customerId;
    private Long companyId;
    private BigDecimal creditLimit;
    private BigDecimal usedLimit;
    private BigDecimal availableLimit;
    private String riskClass;
    private String creditGroup;
    private String creditStatus;
    private String checkRule;
    private LocalDate lastCheckDate;
    private LocalDate nextCheckDate;
}
