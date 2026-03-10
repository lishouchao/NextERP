package com.nexterp.platform.report.domain.model;

import com.nexterp.shared.data.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 报表实体
 *
 * @author NextERP
 */
@Entity
@Table(name = "sys_report")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SysReport extends BaseEntity {

    /**
     * 报表ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * 报表编码
     */
    @Column(name = "report_code", nullable = false, length = 50)
    private String reportCode;

    /**
     * 报表名称
     */
    @Column(name = "report_name", nullable = false, length = 100)
    private String reportName;

    /**
     * 报表类型 (table-表格 chart-图表 pivot-透视)
     */
    @Column(name = "report_type", nullable = false, length = 20)
    private String reportType;

    /**
     * 数据源类型 (sql-查询 api-接口)
     */
    @Column(name = "datasource_type", length = 20)
    private String datasourceType;

    /**
     * 数据源配置 (JSON格式)
     */
    @Column(name = "datasource_config", columnDefinition = "TEXT")
    private String datasourceConfig;

    /**
     * 报表配置 (JSON格式)
     */
    @Column(name = "report_config", columnDefinition = "TEXT")
    private String reportConfig;

    /**
     * 导出配置 (JSON格式)
     */
    @Column(name = "export_config", columnDefinition = "TEXT")
    private String exportConfig;

    /**
     * 列配置 (JSON格式)
     */
    @Column(name = "column_config", columnDefinition = "TEXT")
    private String columnConfig;

    /**
     * 分组
     */
    @Column(name = "report_group", length = 50)
    private String reportGroup;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 状态 (0-禁用 1-正常)
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
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
}
