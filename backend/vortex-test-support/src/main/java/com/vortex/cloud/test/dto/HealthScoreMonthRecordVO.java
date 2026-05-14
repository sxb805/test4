package com.vortex.cloud.test.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vortex.cloud.vfs.lite.base.dto.AbstractBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 管网健康度评分月度数据
 * @author donghao
 * @date 2025/4/22 10:45
 */
@Data
@EqualsAndHashCode(of = "id")
public class HealthScoreMonthRecordVO extends AbstractBaseDTO {

    @Schema(description = "yyyy-MM")
    @JsonFormat(pattern = "yyyy-MM")
    private Date month;

    private String monthStr;

    @Schema(description = "模型id")
    private String modelId;

    @Schema(description = "模型name")
    private String modelName;

    @Schema(description = "水务管线ID")
    private String lineId;

    private String lineCode;

    private String lineName;

    @Schema(description = "水务管线长度")
    private Double lineLength;

    @Schema(description = "jcss-reborn管线ID")
    private String lineFacilityId;

    @Schema(description = "管线健康度得分")
    private Double score;

    @Schema(description = "管线排名")
    private Integer scoreRank;

    @Schema(description = "健康度等级ID")
    private String healthLevelId;

    @Schema(description = "健康度等级name")
    private String healthLevelName;

    @Schema(description =  "健康度等级颜色")
    private String healthLevelColor;

    @Schema(description = "同类管线平均分")
    private Double sameTypeAvgScore;

    @Schema(description = "超过同类管线百分比")
    private Double sameTypeMoreThanPercent;

    @ApiModelProperty(value= "管道类别 LineTypeEnum")
    private String networkType;

    @ApiModelProperty(value= "管道类别value LineTypeEnum")
    private String networkTypeValue;

    @Schema(description = "关联区域类型value")
    private String regionTypeValue;

    @Schema(description = "关联区域类型key")
    private String regionTypeKey;

    @Schema(description = "道路id")
    private String regionObjectId;

    @Schema(description = "所在道路")
    private String regionObjectName;

    @ApiModelProperty("片区id")
    private String districtId;

    @ApiModelProperty("行政区域id")
    private String divisionId;

    @ApiModelProperty("行政区划名称")
    private String divisionName;


}
