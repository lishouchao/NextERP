package com.nexterp.business.controlling.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 盈利段
 * 对标: SAP CE1XXXX
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_profitability_segment")
public class CoProfitabilitySegment extends TenantAwareEntity {

    /**
     * 盈利段ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 盈利段号
     */
    @Column(name = "segment_number", nullable = false, length = 10)
    private String segmentNumber;

    /**
     * 经营范围
     */
    @Column(name = "operating_concern", length = 4)
    private String operatingConcern;

    /**
     * 公司代码
     */
    @Column(name = "company_code", length = 4)
    private String companyCode;

    /**
     * 业务范围
     */
    @Column(name = "business_area", length = 4)
    private String businessArea;

    /**
     * 利润中心
     */
    @Column(name = "profit_center", length = 10)
    private String profitCenter;

    /**
     * 销售组织
     */
    @Column(name = "sales_org", length = 4)
    private String salesOrg;

    /**
     * 分销渠道
     */
    @Column(name = "distribution_channel", length = 2)
    private String distributionChannel;

    /**
     * 产品组
     */
    @Column(name = "product_group", length = 4)
    private String productGroup;

    /**
     * 客户ID
     */
    @Column(name = "customer_id")
    private Long customerId;

    /**
     * 客户代码
     */
    @Column(name = "customer_code", length = 10)
    private String customerCode;

    /**
     * 客户组
     */
    @Column(name = "customer_group", length = 2)
    private String customerGroup;

    /**
     * 客户地区
     */
    @Column(name = "customer_region", length = 3)
    private String customerRegion;

    /**
     * 物料ID
     */
    @Column(name = "material_id")
    private Long materialId;

    /**
     * 物料代码
     */
    @Column(name = "material_code", length = 18)
    private String materialCode;

    /**
     * 物料组
     */
    @Column(name = "material_group", length = 9)
    private String materialGroup;

    /**
     * 品牌代码
     */
    @Column(name = "brand_code", length = 4)
    private String brandCode;

    /**
     * 销售凭证类型
     */
    @Column(name = "sales_document_type", length = 4)
    private String salesDocumentType;

    /**
     * 期间日期
     */
    @Column(name = "period_date", nullable = false)
    private LocalDate periodDate;

    /**
     * 会计年度
     */
    @Column(name = "fiscal_year", length = 4)
    private String fiscalYear;

    /**
     * 期间
     */
    @Column(name = "fiscal_period", length = 3)
    private String fiscalPeriod;

    /**
     * 销售数量
     */
    @Column(name = "sales_quantity", precision = 13, scale = 3)
    private BigDecimal salesQuantity;

    /**
     * 销售单位
     */
    @Column(name = "sales_unit", length = 3)
    private String salesUnit;

    /**
     * 收入
     */
    @Column(name = "revenue", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;

    /**
     * 销售折扣
     */
    @Column(name = "sales_discount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal salesDiscount = BigDecimal.ZERO;

    /**
     * 净收入
     */
    @Column(name = "net_revenue", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal netRevenue = BigDecimal.ZERO;

    /**
     * 销货成本
     */
    @Column(name = "cost_of_goods_sold", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal costOfGoodsSold = BigDecimal.ZERO;

    /**
     * 毛利
     */
    @Column(name = "gross_margin", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal grossMargin = BigDecimal.ZERO;

    /**
     * 销售费用
     */
    @Column(name = "sales_expense", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal salesExpense = BigDecimal.ZERO;

    /**
     * 管理费用
     */
    @Column(name = "admin_expense", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal adminExpense = BigDecimal.ZERO;

    /**
     * 贡献边际 I
     */
    @Column(name = "contribution_margin_1", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal contributionMargin1 = BigDecimal.ZERO;

    /**
     * 贡献边际 II
     */
    @Column(name = "contribution_margin_2", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal contributionMargin2 = BigDecimal.ZERO;

    /**
     * 货币代码
     */
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    /**
     * 描述
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 计算毛利
     */
    public void calculateGrossMargin() {
        this.netRevenue = (revenue != null ? revenue : BigDecimal.ZERO)
                .subtract(salesDiscount != null ? salesDiscount : BigDecimal.ZERO);
        this.grossMargin = netRevenue.subtract(costOfGoodsSold != null ? costOfGoodsSold : BigDecimal.ZERO);
    }

    /**
     * 计算贡献边际
     */
    public void calculateContributionMargin() {
        calculateGrossMargin();
        this.contributionMargin1 = grossMargin.subtract(salesExpense != null ? salesExpense : BigDecimal.ZERO);
        this.contributionMargin2 = contributionMargin1.subtract(adminExpense != null ? adminExpense : BigDecimal.ZERO);
    }

    /**
     * 获取毛利率
     */
    public BigDecimal getGrossMarginRate() {
        if (netRevenue == null || netRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return grossMargin.multiply(BigDecimal.valueOf(100))
                .divide(netRevenue, 2, BigDecimal.ROUND_HALF_UP);
    }
}
