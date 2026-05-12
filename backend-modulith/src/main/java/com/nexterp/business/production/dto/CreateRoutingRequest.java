package com.nexterp.business.production.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建工艺路线请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoutingRequest {

    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @NotBlank(message = "工艺路线编码不能为空")
    private String routingCode;

    @NotBlank(message = "工艺路线名称不能为空")
    private String routingName;

    @NotNull(message = "产品ID不能为空")
    private Long productId;

    private String productCode;

    private String productName;

    private String specification;

    @NotNull(message = "工艺路线类型不能为空")
    private Integer routingType;

    private String version;

    @NotNull(message = "默认标识不能为空")
    private Boolean isDefault;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private String remark;

    @Valid
    private List<RoutingDetailRequest> details;
}
