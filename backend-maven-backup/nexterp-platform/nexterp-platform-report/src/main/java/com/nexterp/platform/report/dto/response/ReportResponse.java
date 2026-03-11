package com.nexterp.platform.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报表响应
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    /**
     * 报表ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 报表编码
     */
    private String reportCode;

    /**
     * 报表名称
     */
    private String reportName;

    /**
     * 报表类型
     */
    private String reportType;

    /**
     * 数据源类型
     */
    private String datasourceType;

    /**
     * 数据源配置
     */
    private Map<String, Object> datasourceConfig;

    /**
     * 报表配置
     */
    private Map<String, Object> reportConfig;

    /**
     * 分组
     */
    private String reportGroup;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
