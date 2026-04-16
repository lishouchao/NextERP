package com.nexterp.business.supply.event;

import java.math.BigDecimal;

/**
 * 收货过账事件
 *
 * @author NextERP
 */
public record GoodsReceiptPostedEvent(
        /**
         * 物料凭证ID
         */
        Long materialDocId,

        /**
         * 物料凭证号
         */
        String materialDocument,

        /**
         * 移动类型
         */
        String movementType,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 采购订单号
         */
        String purchaseOrder,

        /**
         * 总金额
         */
        BigDecimal totalAmount
) {
}
