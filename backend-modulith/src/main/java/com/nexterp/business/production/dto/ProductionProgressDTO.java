package com.nexterp.business.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 生产进度DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionProgressDTO {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 总工序数
     */
    private Integer totalOperations;

    /**
     * 已完成工序数
     */
    private Integer completedOperations;

    /**
     * 总体完工率
     */
    private BigDecimal overallCompletionRate;

    /**
     * 各工序进度明细
     */
    private List<OperationProgressItem> operations;

    /**
     * 工序进度项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationProgressItem {

        /**
         * 顺序号
         */
        private Integer sequenceNo;

        /**
         * 工序名称
         */
        private String processName;

        /**
         * 状态 (0-待开工 1-进行中 2-已完成 3-已暂停)
         */
        private Integer status;

        /**
         * 状态名称
         */
        private String statusName;

        /**
         * 完工率
         */
        private BigDecimal completionRate;

        /**
         * 合格率
         */
        private BigDecimal qualifiedRate;
    }
}
