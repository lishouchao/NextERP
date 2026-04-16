package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建物料销售视图请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaterialSalesRequest {

    private Long salesOrgId;
    private String salesOrgCode;
    private String distrChannel;
    private String deliveringPlant;
    private String salesUnit;
    private BigDecimal minOrderQty;
    private BigDecimal minDelivQty;
    private String pricingGroup;
    private String itemCategoryGroup;
    private String accountAssignmentGroup;
    private String productHierarchy;
    private String materialPricingGroup;
}
