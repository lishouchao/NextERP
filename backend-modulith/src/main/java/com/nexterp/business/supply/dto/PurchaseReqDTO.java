package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购申请DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReqDTO {

    private Long id;
    private String prNumber;
    private String prType;
    private String purchasingGroup;
    private String purchasingOrg;
    private Long plantId;
    private String plantCode;
    private LocalDate documentDate;
    private LocalDate deliveryDate;
    private BigDecimal totalValue;
    private String currency;
    private String headerText;
    private String status;
    private String approvalStatus;
    private String approvedBy;
    private LocalDate approvedDate;
    private List<PurchaseReqItemDTO> items;
}
