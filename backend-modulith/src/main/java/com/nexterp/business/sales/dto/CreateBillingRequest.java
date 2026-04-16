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
public class CreateBillingRequest {

    private Long tenantId;
    private String billingType;
    private LocalDate billingDate;

    private Long deliveryId;
    private Long orderId;
    private String remark;

    private List<CreateBillingItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBillingItemRequest {

        private Integer itemNumber;
        private Long materialId;
        private String materialCode;
        private String description;
        private BigDecimal billedQty;
        private String salesUnit;
        private BigDecimal netPrice;
        private Long deliveryId;
        private Long deliveryItemId;
    }
}
