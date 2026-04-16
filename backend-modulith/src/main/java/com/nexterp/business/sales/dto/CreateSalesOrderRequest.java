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
public class CreateSalesOrderRequest {

    private Long tenantId;
    private String orderType;
    private Long salesOrgId;
    private String distributionChannel;
    private String division;

    private Long soldToParty;
    private Long shipToParty;
    private Long billToParty;
    private Long payerParty;

    private LocalDate documentDate;
    private LocalDate requestedDeliveryDate;

    private String purchaseOrder;
    private String remark;

    private List<CreateSalesOrderItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSalesOrderItemRequest {

        private Integer itemNumber;
        private Long materialId;
        private String materialCode;
        private String description;
        private BigDecimal orderedQty;
        private String salesUnit;
        private BigDecimal netPrice;
        private Long plantId;
        private Long slocId;
        private String itemCategory;
    }
}
