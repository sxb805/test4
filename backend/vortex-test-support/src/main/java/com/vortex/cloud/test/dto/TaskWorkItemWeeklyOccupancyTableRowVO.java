package com.vortex.cloud.test.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 任务工单-责任人周占用表格行
 */
@Data
@Schema(description = "任务工单-责任人周占用表格行")
public class TaskWorkItemWeeklyOccupancyTableRowVO {

    @Schema(description = "行key")
    private String key;

    @Schema(description = "责任人")
    private String ownerName;

    @Schema(description = "总计")
    private Integer totalHours;

    @JsonIgnore
    @Schema(description = "动态列数据，key=week_xxx")
    private Map<String, Integer> cells;

    @JsonAnyGetter
    public Map<String, Integer> getWeekHours() {
        return cells;
    }
}
