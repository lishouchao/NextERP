package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建物料请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaterialRequest {

    private Long tenantId;
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

    private List<CreateMaterialPlantRequest> plantViews;
    private List<CreateMaterialSalesRequest> salesViews;
    private List<CreateMaterialValuationRequest> valuationViews;
}
