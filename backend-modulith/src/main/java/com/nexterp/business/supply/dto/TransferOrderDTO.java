package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 转运订单DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferOrderDTO {

    private Long id;
    private String transferOrder;
    private String warehouseNumber;
    private String toType;
    private String transferRequirement;
    private String materialDocument;
    private String status;
    private LocalDate confirmedDate;
    private String confirmedBy;
    private List<TransferOrderItemDTO> items;
}
