package com.nexterp.platform.report.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 报表查询请求
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportQueryRequest {

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 报表编码
     */
    private String reportCode;

    /**
     * 报表名称（模糊查询）
     */
    private String reportName;

    /**
     * 报表类型
     */
    private String reportType;

    /**
     * 分组
     */
    private String reportGroup;

    /**
     * 查询参数
     */
    private Map<String, Object> params;

    /**
     * 导出格式 (excel, pdf, csv)
     */
    private String exportFormat;

    /**
     * 分页参数
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;
}
