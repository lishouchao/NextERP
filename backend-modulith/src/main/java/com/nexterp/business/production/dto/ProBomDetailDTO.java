package com.nexterp.business.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * BOM明细响应DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProBomDetailDTO {

    private Long id;
    private Long bomId;
    private Integer lineNo;
    private Integer componentType;
    private Long componentId;
    private String componentCode;
    private String componentName;
    private String specification;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal scrapRate;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private Boolean isKeyComponent;
    private Boolean isReverseSubstitute;
    private String substituteGroup;
    private Integer supplyType;
    private String remark;
}
