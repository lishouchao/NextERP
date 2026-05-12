package com.nexterp.business.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建生产订单请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductionOrderRequest {

    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @NotBlank(message = "生产订单号不能为空")
    private String orderNo;

    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @NotNull(message = "产品ID不能为空")
    private Long productId;

    private String productCode;

    private String productName;

    private String specification;

    private String unit;

    @NotNull(message = "计划数量不能为空")
    private BigDecimal plannedQty;

    private Long bomId;

    private String bomVersion;

    private Long routingId;

    private LocalDate planStartDate;

    private LocalDate planEndDate;

    private Long workshopId;

    private String workshopName;

    private Long productionLineId;

    private String productionLineName;

    @NotNull(message = "优先级不能为空")
    private Integer priority;

    private String sourceType;

    private Long sourceId;

    private String sourceNo;

    private Long demandUserId;

    private String demandUserName;

    private String remark;
}
