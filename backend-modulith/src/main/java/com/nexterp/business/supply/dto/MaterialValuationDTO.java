package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 物料评估数据DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialValuationDTO {

    private Long id;
    private String valuationArea;
    private String valuationType;
    private String priceControl;
    private BigDecimal movingPrice;
    private BigDecimal standardPrice;
    private String valuationClass;
    private String currency;
}
