package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购订单DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDTO {

    private Long id;
    private String poNumber;
    private String poType;
    private Long vendorId;
    private String vendorCode;
    private String purchasingOrg;
    private String purchasingGroup;
    private Long companyId;
    private String companyCode;
    private String currency;
    private BigDecimal exchangeRate;
    private LocalDate documentDate;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String termsOfPayment;
    private String incoterms1;
    private String incoterms2;
    private String status;
    private String releaseStatus;
    private String headerText;
    private BigDecimal totalNetValue;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalGrossValue;
    private List<PurchaseOrderItemDTO> items;
}
