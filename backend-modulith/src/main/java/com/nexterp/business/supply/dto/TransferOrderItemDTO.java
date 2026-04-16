package com.nexterp.business.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 转运订单项DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferOrderItemDTO {

    private Long id;
    private Integer toItem;
    private Long materialId;
    private String materialCode;
    private String batch;
    private BigDecimal quantity;
    private String unit;
    private String sourceStorageType;
    private String sourceStorageBin;
    private String destStorageType;
    private String destStorageBin;
    private String status;
}
