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
public class ConditionDTO {

    private Long id;
    private String conditionType;
    private String conditionRecord;
    private Integer conditionItem;

    private BigDecimal amount;
    private BigDecimal rate;
    private Integer priceUnit;
    private String calculationType;

    private LocalDate validFrom;
    private LocalDate validTo;
    private Long salesOrgId;
    private String distributionChannel;

    private Long customerId;
    private Long materialId;
}
