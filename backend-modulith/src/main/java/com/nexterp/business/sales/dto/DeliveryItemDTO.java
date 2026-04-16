package com.nexterp.business.sales.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryItemDTO {

    private Long id;
    private Integer itemNumber;
    private Long materialId;
    private String materialCode;
    private String description;

    private BigDecimal deliveryQty;
    private BigDecimal pickedQty;
    private String salesUnit;
    private String baseUnit;

    private String batchNumber;
    private Long plantId;
    private Long slocId;
    private Long orderId;
    private Long orderItemId;
}
