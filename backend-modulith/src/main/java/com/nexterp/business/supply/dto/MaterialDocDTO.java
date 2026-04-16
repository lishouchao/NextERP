package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 物料凭证DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDocDTO {

    private Long id;
    private String materialDocument;
    private Integer fiscalYear;
    private LocalDate postingDate;
    private LocalDate documentDate;
    private String movementType;
    private String transactionCode;
    private String headerText;
    private String refDocumentNo;
    private List<MaterialDocItemDTO> items;
}
