package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建物料评估视图请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaterialValuationRequest {

    private String valuationArea;
    private String valuationType;
    private String priceControl;
    private BigDecimal movingPrice;
    private BigDecimal standardPrice;
    private String valuationClass;
    private Integer priceUnit;
    private BigDecimal futurePrice;
    private LocalDate futurePriceValidFrom;
    private String currency;
}
