package com.nexterp.platform.report.event;

import java.time.LocalDateTime;

/**
 * 报表创建事件
 *
 * @author NextERP
 */
public record ReportCreatedEvent(
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
         * 报表分组
         */
        String reportGroup,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 创建人
         */
        String createdBy,

        /**
         * 创建时间
         */
        LocalDateTime createdAt
) {
    public ReportCreatedEvent {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
