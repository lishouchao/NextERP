package com.nexterp.platform.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程统计信息 DTO
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流程统计信息")
public class ProcessStatisticsDTO {

    @Schema(description = "运行中流程数")
    private Long runningCount;

    @Schema(description = "已完成流程数")
    private Long finishedCount;

    @Schema(description = "活动任务数")
    private Long activeTaskCount;

    @Schema(description = "总流程数")
    private Long totalCount;

    @Schema(description = "平均完成时间(毫秒)")
    private Long avgDuration;

    @Schema(description = "今日启动流程数")
    private Long todayStartedCount;

    @Schema(description = "今日完成流程数")
    private Long todayFinishedCount;
}
