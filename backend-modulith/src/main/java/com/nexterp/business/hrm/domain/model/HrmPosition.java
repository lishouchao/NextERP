package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 职位 (对标 SAP HRP1000 - Object Type S)
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hrm_position")
public class HrmPosition extends TenantAwareEntity {

    /**
     * 职位ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 职位编码
     */
    @Column(name = "position_code", nullable = false, length = 8)
    private String positionCode;

    /**
     * 职位名称
     */
    @Column(name = "position_name", nullable = false, length = 100)
    private String positionName;

    /**
     * 职位简称
     */
    @Column(name = "position_short_name", length = 50)
    private String positionShortName;

    /**
     * 所属部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 所属部门代码
     */
    @Column(name = "department_code", length = 50)
    private String departmentCode;

    /**
     * 所属职务ID (Job)
     */
    @Column(name = "job_id")
    private Long jobId;

    /**
     * 所属职务代码
     */
    @Column(name = "job_code", length = 8)
    private String jobCode;

    /**
     * 编制人数
     */
    @Column(name = "head_count")
    private Integer headCount;

    /**
     * 在岗人数
     */
    @Column(name = "actual_count")
    @Builder.Default
    private Integer actualCount = 0;

    /**
     * 职位类型 (01-全职 02-兼职 03-临时)
     */
    @Column(name = "position_type", length = 2)
    private String positionType;

    /**
     * 有效起始日期
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    /**
     * 有效结束日期
     */
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    /**
     * 状态 (1-启用 0-禁用)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 职责描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 获取职位类型名称
     */
    public String getPositionTypeName() {
        return switch (positionType) {
            case "01" -> "全职";
            case "02" -> "兼职";
            case "03" -> "临时";
            default -> "未知";
        };
    }

    /**
     * 判断是否有空缺
     */
    public boolean hasVacancy() {
        if (headCount == null) {
            return false;
        }
        return actualCount < headCount;
    }

    /**
     * 获取空缺数量
     */
    public int getVacancyCount() {
        if (headCount == null || actualCount == null) {
            return 0;
        }
        return Math.max(0, headCount - actualCount);
    }

    /**
     * 判断当前是否有效
     */
    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(validFrom) && !now.isAfter(validTo);
    }
}
