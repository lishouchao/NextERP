package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * InfoType 0006 - 地址 (Address)
 * 对标 SAP IT0006
 *
 * 存储员工的各类地址信息，包括户籍地址、现住址、通讯地址等
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it0006_address", indexes = {
    @Index(name = "idx_it0006_employee", columnList = "tenant_id, employee_id, valid_from"),
    @Index(name = "idx_it0006_type", columnList = "tenant_id, employee_id, address_type")
})
public class HrIt0006Address extends TimeValidEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工内码
     */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /**
     * 员工编号
     */
    @Column(name = "employee_no", nullable = false, length = 8)
    private String employeeNo;

    /**
     * 地址类型 (1-户籍地址 2-现住址 3-通讯地址 4-紧急联系地址)
     */
    @Column(name = "address_type", nullable = false, length = 1)
    private String addressType;

    /**
     * 国家代码 (ISO 3166-1)
     */
    @Column(name = "country", length = 3)
    @Builder.Default
    private String country = "CHN";

    /**
     * 省份/州代码
     */
    @Column(name = "province_code", length = 6)
    private String provinceCode;

    /**
     * 省份名称
     */
    @Column(name = "province_name", length = 50)
    private String provinceName;

    /**
     * 城市代码
     */
    @Column(name = "city_code", length = 6)
    private String cityCode;

    /**
     * 城市名称
     */
    @Column(name = "city_name", length = 50)
    private String cityName;

    /**
     * 区县代码
     */
    @Column(name = "district_code", length = 6)
    private String districtCode;

    /**
     * 区县名称
     */
    @Column(name = "district_name", length = 50)
    private String districtName;

    /**
     * 街道/乡镇
     */
    @Column(name = "street", length = 100)
    private String street;

    /**
     * 详细地址
     */
    @Column(name = "address_line", length = 200)
    private String addressLine;

    /**
     * 邮政编码
     */
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    /**
     * 联系电话
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取地址类型名称
     */
    public String getAddressTypeName() {
        return switch (addressType) {
            case "1" -> "户籍地址";
            case "2" -> "现住址";
            case "3" -> "通讯地址";
            case "4" -> "紧急联系地址";
            default -> "未知";
        };
    }

    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (provinceName != null) sb.append(provinceName);
        if (cityName != null) sb.append(cityName);
        if (districtName != null) sb.append(districtName);
        if (street != null) sb.append(street);
        if (addressLine != null) sb.append(addressLine);
        return sb.toString();
    }
}
