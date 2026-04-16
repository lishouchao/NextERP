package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 发票DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {

    private Long id;
    private String invoiceNumber;
    private Integer fiscalYear;
    private String invoiceType;
    private LocalDate documentDate;
    private LocalDate postingDate;
    private Long vendorId;
    private String vendorCode;
    private Long companyId;
    private String companyCode;
    private String currency;
    private String supplierInvoice;
    private LocalDate supplierInvoiceDate;
    private BigDecimal grossAmount;
    private BigDecimal netAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private String paymentTerms;
    private LocalDate dueDate;
    private String status;
    private String blockingReason;
    private List<InvoiceItemDTO> items;
}
