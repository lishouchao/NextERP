package com.nexterp.business.production.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建/更新BOM请求DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBomRequest {

    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @NotBlank(message = "BOM编码不能为空")
    private String bomCode;

    @NotBlank(message = "BOM名称不能为空")
    private String bomName;

    @NotNull(message = "BOM类型不能为空")
    private Integer bomType;

    private String version;

    @NotNull(message = "成品物料ID不能为空")
    private Long productId;

    private String productCode;
    private String productName;
    private String specification;
    private String unit;

    private BigDecimal bomQty;

    @NotNull(message = "基准类型不能为空")
    private Integer baseType;

    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private String remark;
    private String attachments;

    /**
     * BOM明细列表
     */
    @Valid
    private List<BomDetailRequest> details;

    /**
     * BOM明细请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BomDetailRequest {

        @NotNull(message = "行号不能为空")
        private Integer lineNo;

        @NotNull(message = "子件类型不能为空")
        private Integer componentType;

        @NotNull(message = "子件物料ID不能为空")
        private Long componentId;

        private String componentCode;
        private String componentName;
        private String specification;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal scrapRate;
        private LocalDate effectiveStartDate;
        private LocalDate effectiveEndDate;

        @NotNull(message = "是否关键件不能为空")
        private Boolean isKeyComponent;

        @NotNull(message = "是否逆向替代不能为空")
        private Boolean isReverseSubstitute;

        private String substituteGroup;
        private Integer supplyType;
        private String remark;
    }
}
