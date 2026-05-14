package com.vortex.cloud.test.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.collect.Lists;
import com.vortex.cloud.vfs.lite.data.domain.AbstractBaseDeleteModel;
import com.vortex.cloud.vfs.lite.data.dto.IndexDefinitionDTO;
import com.vortex.cloud.vfs.lite.data.handler.GeometryTypeHandler;
import com.vortex.cloud.test.support.Constants;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.locationtech.jts.geom.Geometry;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 管网健康度评分月度数据
 * @author donghao
 * @date 2025/4/22 10:45
 */
@Data
@EqualsAndHashCode(of = "id")
@Entity(name = HealthScoreMonthRecord.TABLE_NAME)
@Table(name = HealthScoreMonthRecord.TABLE_NAME)
@Comment(value = "管网健康度评分月度数据")
@TableName(value = HealthScoreMonthRecord.TABLE_NAME)
public class HealthScoreMonthRecord extends AbstractBaseDeleteModel {

    public static final String TABLE_NAME = Constants.TABLE_PREFIX + "health_score_month_record";

    @Column(columnDefinition = "date comment 'yyyy-MM'")
    private Date month;

    @Column(columnDefinition = "varchar(50) comment '模型id'")
    private String modelId;

    @Column(columnDefinition = "varchar(50) comment '模型名称'")
    private String modelName;

    @Column(columnDefinition = "varchar(50) comment '水务管线ID'")
    private String lineId;

    @Column(columnDefinition = "varchar(50) comment '水务管线code'")
    private String lineCode;

    @Column(columnDefinition = "varchar(50) comment '水务管线name'")
    private String lineName;

    @Column(columnDefinition = "varchar(50) comment '水务管线长度'")
    private Double lineLength;

    @Column(columnDefinition = "varchar(50) comment 'jcss-reborn管线ID'")
    private String lineFacilityId;

    @Column(columnDefinition = "decimal(20,2) comment '管线健康度得分'")
    private BigDecimal score;

    @Column(columnDefinition = "varchar(50) comment '管线健康度等级ID'")
    private String healthLevelId;

    @Column(columnDefinition = "varchar(50) comment '管线健康度等级Name'")
    private String healthLevelName;

    @Column(columnDefinition = "varchar(50) comment '管线健康度等级颜色'")
    private String healthLevelColor;

    @Column(columnDefinition = "int comment '管线排名'")
    private Integer scoreRank;

    @Column(columnDefinition = "varchar(50) comment '管道类别'")
    private String networkType;

    @Column(columnDefinition = "varchar(50) comment '关联区域类型value'")
    private String regionTypeValue;

    @Column(columnDefinition = "varchar(50) comment '关联区域类型key'")
    private String regionTypeKey;

    @Column(columnDefinition = "varchar(50) comment '道路id'")
    private String regionObjectId;

    @Column(columnDefinition = "varchar(50) comment '所在道路'")
    private String regionObjectName;

    @ApiModelProperty("片区id")
    @Column(columnDefinition = "varchar(100) comment '片区id'")
    private String districtId;

    @ApiModelProperty("片区id，最后一层id")
    @Column(columnDefinition = "varchar(50) comment '片区id'")
    private String districtLastId;

    @ApiModelProperty("行政区域id")
    @Column(columnDefinition = "varchar(50) comment '行政区域id'")
    private String divisionId;

    @ApiModelProperty("行政区划名称")
    @Column(columnDefinition = "varchar(50) comment '行政区划名称'")
    private String divisionName;

    @ApiModelProperty("行政区域nodeCode")
    @Column(columnDefinition = "varchar(50) comment '行政区域nodeCode'")
    private String divisionNodeCode;

    @ApiModelProperty(value= "管网流向")
    @Column(columnDefinition = "varchar(50) comment '管网流向'")
    private String flowDirectionName;

    @TableField(value = "location", typeHandler = GeometryTypeHandler.class)
    @Column(columnDefinition = "geometry comment '地图信息'")
    private Geometry location;

    @Schema(description = "各模块评分情况")
    @Column(columnDefinition = "longtext comment '模块'")
    private String modules;

    @Schema(description = "命中评分项规则bitIndex")
    @Column(columnDefinition = "bigint(11) default 0")
    private Long itemRuleBitIndex;

    @Schema(description = "命中评分项规则bitIndex2")
    @Column(columnDefinition = "bigint(11) default 0")
    private Long itemRuleBitIndex2;

    public static List<IndexDefinitionDTO> indexDefinitions() {
        return Lists.newArrayList(
                IndexDefinitionDTO.builder()
                        .indexName("idx_modelId_month_lineId")
                        .columnNames(Lists.newArrayList("model_id", "month","line_id"))
                        .unique(true)
                        .build(),
                IndexDefinitionDTO.builder()
                        .indexName("idx_modelId_score")
                        .columnNames(Lists.newArrayList("model_id", "score"))
                        .build(),
                IndexDefinitionDTO.builder()
                        .indexName("idx_modelId_healthLevelId_score")
                        .columnNames(Lists.newArrayList("model_id", "health_level_id", "score"))
                        .build(),
                IndexDefinitionDTO.builder()
                        .indexName("idx_lineId")
                        .columnNames(Lists.newArrayList("line_id"))
                        .build(),
                IndexDefinitionDTO.builder()
                        .indexName("idx_lineFacilityId")
                        .columnNames(Lists.newArrayList("line_facility_id"))
                        .build(),
                IndexDefinitionDTO.builder()
                        .indexName("idx_score")
                        .columnNames(Lists.newArrayList("score"))
                        .build(),
                IndexDefinitionDTO.builder()
                        .indexName("idx_score_rank")
                        .columnNames(Lists.newArrayList("score_rank"))
                        .build(),
                IndexDefinitionDTO.builder()
                        .indexName("idx_divisionNodeCode")
                        .columnNames(Lists.newArrayList("division_node_code"))
                        .build()
        );
    }

}
