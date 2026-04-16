package com.nexterp.business.supply.event;

/**
 * 物料创建事件
 *
 * @author NextERP
 */
public record MaterialCreatedEvent(
        /**
         * 物料ID
         */
        Long materialId,

        /**
         * 物料编码
         */
        String materialNumber,

        /**
         * 物料类型
         */
        String materialType,

        /**
         * 租户ID
         */
        Long tenantId,

        /**
         * 物料描述
         */
        String description
) {
}
