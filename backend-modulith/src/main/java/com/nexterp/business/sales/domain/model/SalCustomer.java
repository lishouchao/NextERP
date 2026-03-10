package com.nexterp.business.sales.domain.model;

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
 * 客户
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sal_customer")
public class SalCustomer extends TenantAwareEntity {

    /**
     * 客户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 客户编码
     */
    @Column(name = "customer_code", nullable = false, length = 50)
    private String customerCode;

    /**
     * 客户名称
     */
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    /**
     * 客户简称
     */
    @Column(name = "short_name", length = 50)
    private String shortName;

    /**
     * 客户类型 (1-一般客户 2-重点客户 3-战略客户)
     */
    @Column(name = "customer_type", nullable = false)
    private Integer customerType;

    /**
     * 客户分类ID
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 客户分类名称
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
     * 交货条件
     */
    @Column(name = "delivery_terms", length = 50)
    private String deliveryTerms;

    /**
     * 销售员ID
     */
    @Column(name = "sales_person_id")
    private Long salesPersonId;

    /**
     * 销售员姓名
     */
    @Column(name = "sales_person_name", length = 50)
    private String salesPersonName;

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
     * 最后销售日期
     */
    @Column(name = "last_sale_date")
    private LocalDate lastSaleDate;

    /**
     * 累计销售金额
     */
    @Column(name = "total_sale_amount", precision = 19, scale = 2)
    private BigDecimal totalSaleAmount;

    /**
     * 销售次数
     */
    @Column(name = "sale_count")
    private Integer saleCount;

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
     * 子客户列表 (不持久化)
     */
    @Transient
    private List<SalCustomer> children = new ArrayList<>();

    /**
     * 获取客户类型名称
     */
    public String getCustomerTypeName() {
        return switch (customerType) {
            case 1 -> "一般客户";
            case 2 -> "重点客户";
            case 3 -> "战略客户";
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
