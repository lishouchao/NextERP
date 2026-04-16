package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建采购申请请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseReqRequest {

    private Long tenantId;
    private String prType;
    private String purchasingGroup;
    private String purchasingOrg;
    private Long plantId;
    private String plantCode;
    private LocalDate documentDate;
    private LocalDate deliveryDate;
    private String headerText;
    private List<CreatePurchaseReqItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePurchaseReqItemRequest {
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
    }
}
