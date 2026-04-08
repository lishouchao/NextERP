package com.nexterp.platform.report.event;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报表生成事件
 *
 * @author NextERP
 */
public record ReportGeneratedEvent(
        /**
         * 报表ID
         */
        Long reportId,

        /**
         * 报表编码
         */
        String reportCode,

        /**
         * 报表名称
         */
        String reportName,

        /**
         * 报表类型
         */
        String reportType,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 操作用户
         */
        String operator,

        /**
         * 生成时间
         */
        LocalDateTime generatedTime,

        /**
         * 查询参数
         */
        Map<String, Object> params,

        /**
         * 数据行数
         */
        Integer rowCount
) {
    public ReportGeneratedEvent {
        if (generatedTime == null) {
            generatedTime = LocalDateTime.now();
        }
    }
}
