package com.nexterp.business.sales.dto;

import java.math.BigDecimal;
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
public class CreateDeliveryRequest {

    private Long tenantId;
    private String deliveryType;
    private Long salesOrgId;
    private String distributionChannel;
    private String division;

    private Long soldToParty;
    private Long shipToParty;
    private LocalDate documentDate;
    private LocalDate plannedGiDate;

    private String shippingPoint;
    private Long orderId;
    private String remark;

    private List<CreateDeliveryItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDeliveryItemRequest {

        private Integer itemNumber;
        private Long materialId;
        private String materialCode;
        private String description;
        private BigDecimal deliveryQty;
        private String salesUnit;
        private Long plantId;
        private Long slocId;
        private Long orderItemId;
    }
}
