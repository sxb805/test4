package com.vortex.cloud.test.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 任务工单-责任人周占用结果
 */
@Data
@Schema(description = "任务工单-责任人周占用结果")
public class TaskWorkItemWeeklyOccupancyVO {

    @Schema(description = "表格列定义")
    private List<TaskWorkItemWeeklyOccupancyColumnVO> columns;

    @Schema(description = "表格数据")
    private List<TaskWorkItemWeeklyOccupancyTableRowVO> tableData;
}
