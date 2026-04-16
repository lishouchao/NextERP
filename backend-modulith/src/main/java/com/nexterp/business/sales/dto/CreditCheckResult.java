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
public class CreditCheckResult {

    private String checkResult;
    private BigDecimal creditLimit;
    private BigDecimal usedBefore;
    private BigDecimal usedAfter;
    private BigDecimal availableLimit;
    private BigDecimal usageRate;
    private String message;
}
