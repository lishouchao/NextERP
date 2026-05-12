package com.nexterp.business.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产订单DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProProductionOrderDTO {

    private Long id;

    private Long tenantId;

    private String orderNo;

    private Integer orderType;

    private String orderTypeName;

    private Long productId;

    private String productCode;

    private String productName;

    private String specification;

    private String unit;

    private BigDecimal plannedQty;

    private BigDecimal completedQty;

    private BigDecimal scrappedQty;

    private BigDecimal completionRate;

    private Long bomId;

    private String bomVersion;

    private Long routingId;

    private LocalDate planStartDate;

    private LocalDate planEndDate;

    private LocalDate actualStartDate;

    private LocalDate actualEndDate;

    private Long workshopId;

    private String workshopName;

    private Long productionLineId;

    private String productionLineName;

    private Integer status;

    private String statusName;

    private Integer priority;

    private String priorityName;

    private String sourceType;

    private Long sourceId;

    private String sourceNo;

    private Long demandUserId;

    private String demandUserName;

    private Long createdById;

    private String createdByName;

    private Long approvedById;

    private String approvedByName;

    private LocalDateTime approvedAt;

    private String remark;

    private String attachments;

    private List<ProProductionOrderDetailDTO> details;
}
