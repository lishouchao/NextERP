package com.nexterp.business.production.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 工艺路线
 *
 * @author NextERP
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pro_routing")
public class ProRouting extends TenantAwareEntity {

    /**
     * 工艺路线编码
     */
    @Column(name = "routing_code", nullable = false, length = 50)
    private String routingCode;

    /**
     * 工艺路线名称
     */
    @Column(name = "routing_name", nullable = false, length = 100)
    private String routingName;

    /**
     * 产品ID
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 产品编码
     */
    @Column(name = "product_code", length = 50)
    private String productCode;

    /**
     * 产品名称
     */
    @Column(name = "product_name", length = 100)
    private String productName;

    /**
     * 规格型号
     */
    @Column(name = "specification", length = 200)
    private String specification;

    /**
     * 工艺路线类型 (1-标准工艺 2-替代工艺)
     */
    @Column(name = "routing_type", nullable = false)
    private Integer routingType;

    /**
     * 版本号
     */
    @Column(name = "version", length = 20)
    private String version;

    /**
     * 默认标识
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

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
     * 工艺路线明细列表
     */
    @OneToMany(mappedBy = "routing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNo ASC")
    private List<ProRoutingDetail> details = new ArrayList<>();

    /**
     * 获取工艺路线类型名称
     */
    public String getRoutingTypeName() {
        return switch (routingType) {
            case 1 -> "标准工艺";
            case 2 -> "替代工艺";
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
