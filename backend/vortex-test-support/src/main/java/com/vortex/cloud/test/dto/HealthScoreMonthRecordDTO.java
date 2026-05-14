package com.vortex.cloud.test.dto;

import com.vortex.cloud.vfs.lite.base.dto.AbstractBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.locationtech.jts.geom.Geometry;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 管网健康度评分月度数据
 * @author donghao
 * @date 2025/4/22 10:45
 */
@Data
@EqualsAndHashCode(of = "id")
public class HealthScoreMonthRecordDTO extends AbstractBaseDTO {

    @Schema(description = "租户id")
    private String tenantId;

    @Schema(description = "yyyy-MM")
    private Date month;

    @Schema(description = "模型id")
    private String modelId;

    @Schema(description = "模型name")
    private String modelName;

    @Schema(description = "水务管线ID")
    private String lineId;

    @Schema(description = "水务管线code")
    private String lineCode;

    @Schema(description = "水务管线name")
    private String lineName;

    @Schema(description = "水务管线长度")
    private Double lineLength;

    @Schema(description = "jcss-reborn管线ID")
    private String lineFacilityId;

    @Schema(description = "管线健康度得分")
    private BigDecimal score;

    @Schema(description = "健康度等级ID")
    private String healthLevelId;

    @Schema(description = "健康度等级name")
    private String healthLevelName;

    @Schema(description =  "健康度等级颜色")
    private String healthLevelColor;

    @Schema(description = "管线排名")
    private Integer scoreRank;

    @ApiModelProperty(value= "管道类别 LineTypeEnum")
    private String networkType;

    @Schema(description = "关联区域类型value")
    private String regionTypeValue;

    @Schema(description = "关联区域类型key")
    private String regionTypeKey;

    @Schema(description = "道路id")
    private String regionObjectId;

    @Schema(description = "所在道路")
    private String regionObjectName;

    @ApiModelProperty(value = "地图信息")
    private Geometry location;

    @ApiModelProperty("片区id，污水片区是全路径id")
    private String districtId;

    @ApiModelProperty("片区id，最后一层id")
    private String districtLastId;

    @ApiModelProperty("行政区域id")
    private String divisionId;

    @ApiModelProperty("行政区划名称")
    private String divisionName;

    @ApiModelProperty("行政区域nodeCode")
    private String divisionNodeCode;

    @ApiModelProperty(value= "管网流向")
    private String flowDirectionName;

    @Schema(description = "命中评分项规则bitIndex")
    private Long itemRuleBitIndex;

    @Schema(description = "命中评分项规则bitIndex2")
    private Long itemRuleBitIndex2;



}
