package com.vortex.cloud.test.dto;

import com.vortex.cloud.vfs.lite.base.dto.AbstractBaseTenantDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 工作计划展示VO
 */
@Data
@Schema(description = "工作计划展示VO")
public class WorkPlanVO extends AbstractBaseTenantDTO {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "项目编号")
    private String projectNo;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "一季度（人/次）")
    private BigDecimal firstQuarterPersonTimes;

    @Schema(description = "二季度（人/次）")
    private BigDecimal secondQuarterPersonTimes;

    @Schema(description = "三季度（人/次）")
    private BigDecimal thirdQuarterPersonTimes;

    @Schema(description = "四季度（人/次）")
    private BigDecimal fourthQuarterPersonTimes;
}
