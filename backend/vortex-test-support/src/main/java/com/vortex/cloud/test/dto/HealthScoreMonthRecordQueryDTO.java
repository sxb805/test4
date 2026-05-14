package com.vortex.cloud.test.dto;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author zhanglei
 */
@Data
public class HealthScoreMonthRecordQueryDTO {

    @JsonFormat(timezone = "GMT+8", pattern = DatePattern.NORM_MONTH_PATTERN)
    @Parameter(description = "评估月份")
    private Date month;

    @Parameter(description = "租户ID")
    private String tenantId;

    @Parameter(description = "ID集合")
    private Set<String> ids;

    @Parameter(description = "模型id")
    private String modelId;

    @Schema(description = "健康度等级ID")
    private String healthLevelId;

    @Schema(description = "健康度等级ID")
    private List<String> healthLevelIdList;

    @Parameter(description = "管线code")
    private String lineCode;

    @Parameter(description = "管线code模糊查询")
    private String lineCodeLike;

    @Schema(description = "道路id")
    private String regionObjectId;

    @Schema(description = "所在道路")
    private String regionObjectName;

    @Parameter(description = "健康度得分区间左值")
    private Double leftScore;

    @Parameter(description = "健康度得分区间右值")
    private Double rightScore;

    @Parameter(description = "管线id")
    private String lineId;

    @Parameter(description = "管线 jcss-reborn id")
    private String lineFacilityId;

    @Parameter(description = "指定查询字段")
    private String select;

    @Parameter(description = "排序规则 moduleCode,desc/asc")
    private String sort;

    @Schema(description = "导出字段 适配总部导出组件")
    private String columnJson;

    @Schema(description = "导出名称")
    private String fileName = "管网健康度得分";

    @Schema(description = "导出类型 1-模板 2-数据")
    private Integer exportType = 2;

    @Parameter(description = "行政区划ID，本级及以下")
    private String divisionId;

    @Parameter(description = "行政区划本级及以下查询")
    private String divisionPrefix;

    @Schema(description = "片区id，本级及以下")
    private String districtId;

    @Schema(description = "片区id，模糊搜索")
    private String districtIdLike;

    @Schema(description = "分项规则bitIndex集合")
    private List<Integer> ruleBitIndexList;

}
