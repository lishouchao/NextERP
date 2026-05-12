package com.nexterp.business.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 工艺路线DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProRoutingDTO {

    private Long id;

    private Long tenantId;

    private String routingCode;

    private String routingName;

    private Long productId;

    private String productCode;

    private String productName;

    private String specification;

    private Integer routingType;

    private String routingTypeName;

    private String version;

    private Boolean isDefault;

    private Integer status;

    private String statusName;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private String remark;

    private List<ProRoutingDetailDTO> details;
}
