package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建采购订单请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderRequest {

    private Long tenantId;
    private String poType;
    private Long vendorId;
    private String vendorCode;
    private String purchasingOrg;
    private String purchasingGroup;
    private Long companyId;
    private String companyCode;
    private String currency;
    private BigDecimal exchangeRate;
    private LocalDate documentDate;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String termsOfPayment;
    private String incoterms1;
    private String incoterms2;
    private String headerText;
    private List<CreatePurchaseOrderItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePurchaseOrderItemRequest {
        private Integer poItem;
        private Long materialId;
        private String materialCode;
        private String shortText;
        private String materialGroup;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal price;
        private Integer priceUnit;
        private String taxCode;
        private Long plantId;
        private String plantCode;
        private Long slocId;
        private String slocCode;
        private LocalDate deliveryDate;
        private String itemCategory;
        private String acctAssignmentCat;
        private String costCenter;
    }
}
