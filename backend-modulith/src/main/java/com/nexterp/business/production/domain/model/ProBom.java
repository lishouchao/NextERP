package com.nexterp.business.production.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 物料清单 (BOM)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pro_bom")
public class ProBom extends TenantAwareEntity {

    /**
     * BOM编码
     */
    @Column(name = "bom_code", nullable = false, length = 50)
    private String bomCode;

    /**
     * BOM名称
     */
    @Column(name = "bom_name", nullable = false, length = 100)
    private String bomName;

    /**
     * BOM类型 (1-标准BOM 2-替代BOM 3-工艺BOM)
     */
    @Column(name = "bom_type", nullable = false)
    private Integer bomType;

    /**
     * 版本号
     */
    @Column(name = "version", length = 20)
    private String version;

    /**
     * 成品物料ID
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 成品物料编码
     */
    @Column(name = "product_code", length = 50)
    private String productCode;

    /**
     * 成品物料名称
     */
    @Column(name = "product_name", length = 100)
    private String productName;

    /**
     * 规格型号
     */
    @Column(name = "specification", length = 200)
    private String specification;

    /**
     * 单位
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * BOM数量
     */
    @Column(name = "bom_qty", precision = 19, scale = 4)
    private BigDecimal bomQty;

    /**
     * 基准类型 (1-离散 2-流程)
     */
    @Column(name = "base_type", nullable = false)
    private Integer baseType;

    /**
     * 状态 (0-草稿 1-启用 2-停用)
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 生效日期
     */
    @Column(name = "effective_date")
    private java.time.LocalDate effectiveDate;

    /**
     * 失效日期
     */
    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 附件 (JSON格式)
     */
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    /**
     * BOM明细列表
     */
    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNo ASC")
    private List<ProBomDetail> details = new ArrayList<>();

    /**
     * 获取BOM类型名称
     */
    public String getBomTypeName() {
        return switch (bomType) {
            case 1 -> "标准BOM";
            case 2 -> "替代BOM";
            case 3 -> "工艺BOM";
            default -> "未知";
        };
    }

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 判断是否有效
     */
    public boolean isValid() {
        java.time.LocalDate now = java.time.LocalDate.now();
        if (effectiveDate != null && now.isBefore(effectiveDate)) {
            return false;
        }
        if (expiryDate != null && now.isAfter(expiryDate)) {
            return false;
        }
        return isEnabled();
    }
}
