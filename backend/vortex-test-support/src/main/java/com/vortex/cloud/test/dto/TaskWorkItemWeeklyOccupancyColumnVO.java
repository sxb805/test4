package com.vortex.cloud.test.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 任务工单-责任人周占用列定义
 */
@Data
@Schema(description = "任务工单-责任人周占用列定义")
public class TaskWorkItemWeeklyOccupancyColumnVO {

    @Schema(description = "字段名")
    private String field;

    @Schema(description = "标题上行")
    private String titleTop;

    @Schema(description = "标题下行")
    private String titleBottom;
}
