package com.nexterp.platform.report.event;

import java.time.LocalDateTime;

/**
 * 报表删除事件
 *
 * @author NextERP
 */
public record ReportDeletedEvent(
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
         * 租户ID
         */
        Long tenantId,

        /**
         * 删除人
         */
        String deletedBy,

        /**
         * 删除时间
         */
        LocalDateTime deletedTime,

        /**
         * 删除原因
         */
        String deleteReason
) {
    public ReportDeletedEvent {
        if (deletedTime == null) {
            deletedTime = LocalDateTime.now();
        }
    }
}
