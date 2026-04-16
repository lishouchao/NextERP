package com.nexterp.business.sales.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDTO {

    private Long id;
    private String deliveryNumber;
    private String deliveryType;
    private Long salesOrgId;
    private String distributionChannel;
    private String division;

    private Long soldToParty;
    private Long shipToParty;
    private LocalDate documentDate;
    private LocalDate plannedGiDate;
    private LocalDate actualGiDate;

    private String shippingPoint;
    private String deliveryStatus;
    private String pickingStatus;
    private String giStatus;

    private Long orderId;
    private String remark;

    private List<DeliveryItemDTO> items;
}
