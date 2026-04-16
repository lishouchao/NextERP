package com.nexterp.business.sales.event;

import java.time.LocalDate;

/**
 * 发货过账事件
 *
 * @author NextERP
 */
public record GoodsIssuePostedEvent(
        /**
         * 交货单ID
         */
        Long deliveryId,

        /**
         * 交货单号
         */
        String deliveryNumber,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 实际发货日期
         */
        LocalDate actualGiDate
) {
}
