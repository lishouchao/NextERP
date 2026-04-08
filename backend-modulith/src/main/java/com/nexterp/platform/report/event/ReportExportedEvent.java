package com.nexterp.platform.report.event;

import java.time.LocalDateTime;

/**
 * 报表导出事件
 *
 * @author NextERP
 */
public record ReportExportedEvent(
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
         * 导出格式 (excel, pdf, csv)
         */
        String exportFormat,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 操作用户
         */
        String operator,

        /**
         * 导出时间
         */
        LocalDateTime exportedTime,

        /**
         * 文件大小 (字节)
         */
        Long fileSize,

        /**
         * 是否成功
         */
        boolean success,

        /**
         * 错误信息
         */
        String errorMessage
) {
    public ReportExportedEvent {
        if (exportedTime == null) {
            exportedTime = LocalDateTime.now();
        }
    }
}
