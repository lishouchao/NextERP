package com.nexterp.shared.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.Period;

/**
 * 时间有效性实体基类
 * 所有需要时间有效性管理的实体都应继承此基类 (如 SAP InfoType)
 *
 * <p>对标 SAP HCM InfoType 时间分割概念:
 * <ul>
 *   <li>valid_from - 记录生效开始日期</li>
 *   <li>valid_to - 记录生效结束日期 (默认 9999-12-31)</li>
 * </ul>
 *
 * @author NextERP
 */
@MappedSuperclass
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public abstract class TimeValidEntity extends TenantAwareEntity {

    /**
     * 生效开始日期
     * 对标 SAP BEGDA
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    /**
     * 生效结束日期
     * 对标 SAP ENDDA
     * 默认值: 9999-12-31 (表示"无限期有效")
     */
    @Column(name = "valid_to", nullable = false)
    @Builder.Default
    private LocalDate validTo = LocalDate.of(9999, 12, 31);

    /**
     * 持久化前的回调方法
     * 确保有效期字段在保存前被设置
     */
    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (validFrom == null) {
            validFrom = LocalDate.now();
        }
        if (validTo == null) {
            validTo = LocalDate.of(9999, 12, 31);
        }
    }

    /**
     * 判断当前是否有效
     *
     * @return 如果当前日期在有效期内返回 true
     */
    public boolean isCurrentlyValid() {
        LocalDate now = LocalDate.now();
        return isValidOn(now);
    }

    /**
     * 判断指定日期是否在有效期内
     *
     * @param date 要检查的日期
     * @return 如果指定日期在有效期内返回 true
     */
    public boolean isValidOn(LocalDate date) {
        if (validFrom == null || validTo == null || date == null) {
            return false;
        }
        return !date.isBefore(validFrom) && !date.isAfter(validTo);
    }

    /**
     * 判断有效期是否与指定范围重叠
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 如果存在重叠返回 true
     */
    public boolean overlaps(LocalDate startDate, LocalDate endDate) {
        if (validFrom == null || validTo == null || startDate == null || endDate == null) {
            return false;
        }
        return !validTo.isBefore(startDate) && !validFrom.isAfter(endDate);
    }

    /**
     * 获取有效期天数
     *
     * @return 有效期天数
     */
    public long getValidityDays() {
        if (validFrom == null || validTo == null) {
            return 0;
        }
        return Period.between(validFrom, validTo).getDays() + 1L;
    }

    /**
     * 判断是否为"无限期有效"
     *
     * @return 如果结束日期为 9999-12-31 返回 true
     */
    public boolean isIndefinite() {
        return LocalDate.of(9999, 12, 31).equals(validTo);
    }

    /**
     * 设置为无限期有效
     */
    public void setIndefinite() {
        this.validTo = LocalDate.of(9999, 12, 31);
    }

    /**
     * 使记录在指定日期失效
     *
     * @param date 失效日期 (成为结束日期)
     */
    public void expireOn(LocalDate date) {
        if (date != null && (validTo == null || date.isBefore(validTo))) {
            this.validTo = date;
        }
    }

    /**
     * 使记录立即失效
     */
    public void expireNow() {
        expireOn(LocalDate.now().minusDays(1));
    }
}
