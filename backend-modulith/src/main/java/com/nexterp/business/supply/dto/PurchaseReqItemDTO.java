package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购申请项DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReqItemDTO {

    private Long id;
    private Integer prItem;
    private Long materialId;
    private String materialCode;
    private String shortText;
    private String materialGroup;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal price;
    private Integer priceUnit;
    private LocalDate deliveryDate;
    private Long plantId;
    private String plantCode;
    private Long slocId;
    private String slocCode;
    private String costCenter;
    private String itemCategory;
    private String status;
}
