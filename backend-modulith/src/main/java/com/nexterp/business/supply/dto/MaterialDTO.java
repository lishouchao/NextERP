package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 物料主数据DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDTO {

    private Long id;
    private String materialNumber;
    private String materialType;
    private String industrySector;
    private String materialGroup;
    private String description;
    private String descriptionEn;
    private String baseUom;
    private String orderUom;
    private BigDecimal grossWeight;
    private BigDecimal netWeight;
    private String weightUnit;
    private BigDecimal volume;
    private String volumeUnit;
    private String eanUpc;
    private String oldMatNo;
    private String division;
    private String productHierarchy;
    private String crossPlantStatus;
    private LocalDate validFrom;
    private LocalDate validTo;

    private List<MaterialPlantDTO> plantViews;
    private List<MaterialSalesDTO> salesViews;
    private List<MaterialValuationDTO> valuationViews;
}
