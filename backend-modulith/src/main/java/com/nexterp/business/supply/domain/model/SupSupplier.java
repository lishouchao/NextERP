package com.nexterp.business.supply.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 供应商
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sup_supplier")
public class SupSupplier extends TenantAwareEntity {

    /**
     * 供应商ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 供应商编码
     */
    @Column(name = "supplier_code", nullable = false, length = 50)
    private String supplierCode;

    /**
     * 供应商名称
     */
    @Column(name = "supplier_name", nullable = false, length = 100)
    private String supplierName;

    /**
     * 供应商简称
     */
    @Column(name = "short_name", length = 50)
    private String shortName;

    /**
     * 供应商类型 (1-一般供应商 2-重点供应商 3-战略供应商)
     */
    @Column(name = "supplier_type", nullable = false)
    private Integer supplierType;

    /**
     * 供应商分类ID
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 供应商分类名称
     */
    @Column(name = "category_name", length = 50)
    private String categoryName;

    /**
     * 联系人
     */
    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    /**
     * 联系电话
     */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * 联系手机
     */
    @Column(name = "contact_mobile", length = 20)
    private String contactMobile;

    /**
     * 联系邮箱
     */
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    /**
     * 省份
     */
    @Column(name = "province", length = 50)
    private String province;

    /**
     * 城市
     */
    @Column(name = "city", length = 50)
    private String city;

    /**
     * 区县
     */
    @Column(name = "district", length = 50)
    private String district;

    /**
     * 详细地址
     */
    @Column(name = "address", length = 200)
    private String address;

    /**
     * 纳税人识别号
     */
    @Column(name = "tax_no", length = 50)
    private String taxNo;

    /**
     * 开户银行
     */
    @Column(name = "bank_name", length = 100)
    private String bankName;

    /**
     * 银行账号
     */
    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    /**
     * 信用额度
     */
    @Column(name = "credit_limit", precision = 19, scale = 2)
    private BigDecimal creditLimit;

    /**
     * 信用期限 (天)
     */
    @Column(name = "credit_days")
    private Integer creditDays;

    /**
     * 付款条件
     */
    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    /**
     * 币种
     */
    @Column(name = "currency", length = 10)
    private String currency;

    /**
     * 交货周期 (天)
     */
    @Column(name = "delivery_days")
    private Integer deliveryDays;

    /**
     * 最小起订量
     */
    @Column(name = "minimum_order_qty")
    private Integer minimumOrderQty;

    /**
     * 质量等级
     */
    @Column(name = "quality_level", length = 20)
    private String qualityLevel;

    /**
     * 合格率
     */
    @Column(name = "qualified_rate", precision = 5, scale = 2)
    private BigDecimal qualifiedRate;

    /**
     * 准时交货率
     */
    @Column(name = "on_time_delivery_rate", precision = 5, scale = 2)
    private BigDecimal onTimeDeliveryRate;

    /**
     * 状态 (0-禁用 1-启用)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * 合作开始日期
     */
    @Column(name = "cooperation_start_date")
    private LocalDate cooperationStartDate;

    /**
     * 最后采购日期
     */
    @Column(name = "last_purchase_date")
    private LocalDate lastPurchaseDate;

    /**
     * 累计采购金额
     */
    @Column(name = "total_purchase_amount", precision = 19, scale = 2)
    private BigDecimal totalPurchaseAmount;

    /**
     * 采购次数
     */
    @Column(name = "purchase_count")
    private Integer purchaseCount;

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
     * 自定义字段1
     */
    @Column(name = "custom_field1", length = 100)
    private String customField1;

    /**
     * 自定义字段2
     */
    @Column(name = "custom_field2", length = 100)
    private String customField2;

    /**
     * 自定义字段3
     */
    @Column(name = "custom_field3", length = 100)
    private String customField3;

    /**
     * 子供应商列表 (不持久化)
     */
    @Transient
    private List<SupSupplier> children = new ArrayList<>();

    /**
     * 获取供应商类型名称
     */
    public String getSupplierTypeName() {
        return switch (supplierType) {
            case 1 -> "一般供应商";
            case 2 -> "重点供应商";
            case 3 -> "战略供应商";
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
     * 判断是否可用 (启用且未删除)
     */
    public boolean isAvailable() {
        return isEnabled() && !getIsDeleted();
    }
}
