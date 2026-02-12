package com.nexterp.platform.report.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 报表创建请求
 *
 * @author NextERP
 */
@Data
public class ReportCreateRequest {

    /**
     * 租户ID
     */
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    /**
     * 报表编码
     */
    @NotBlank(message = "报表编码不能为空")
    @Size(max = 50, message = "报表编码长度不能超过50")
    private String reportCode;

    /**
     * 报表名称
     */
    @NotBlank(message = "报表名称不能为空")
    @Size(max = 100, message = "报表名称长度不能超过100")
    private String reportName;

    /**
     * 报表类型
     */
    @NotBlank(message = "报表类型不能为空")
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
    @Size(max = 50, message = "分组长度不能超过50")
    private String reportGroup;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
