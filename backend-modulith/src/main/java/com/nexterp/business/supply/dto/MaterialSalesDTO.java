package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物料销售数据DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialSalesDTO {

    private Long id;
    private Long salesOrgId;
    private String salesOrgCode;
    private String distrChannel;
    private String statusSales;
    private String deliveringPlant;
    private String salesUnit;
    private String pricingGroup;
    private String itemCategoryGroup;
}
