package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 库存DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {

    private Long id;
    private Long materialId;
    private String materialCode;
    private Long plantId;
    private String plantCode;
    private Long slocId;
    private String slocCode;
    private BigDecimal unrestrictedStock;
    private BigDecimal qualityStock;
    private BigDecimal blockedStock;
    private BigDecimal unrestrictedValue;
    private BigDecimal qualityValue;
    private BigDecimal blockedValue;
    private String currency;
}
