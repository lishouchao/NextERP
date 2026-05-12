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
 * 物料清单(BOM)响应DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProBomDTO {

    private Long id;
    private Long tenantId;
    private String bomCode;
    private String bomName;
    private Integer bomType;
    private String bomTypeName;
    private String version;
    private Long productId;
    private String productCode;
    private String productName;
    private String specification;
    private String unit;
    private BigDecimal bomQty;
    private Integer baseType;
    private Integer status;
    private String statusName;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private String remark;
    private String attachments;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    /**
     * BOM明细列表
     */
    private List<ProBomDetailDTO> details;
}
