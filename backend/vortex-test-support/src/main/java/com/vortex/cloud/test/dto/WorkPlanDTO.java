package com.vortex.cloud.test.dto;

import com.vortex.cloud.vfs.lite.base.dto.AbstractBaseTenantDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 工作计划请求DTO
 */
@Data
@Schema(description = "工作计划请求DTO")
public class WorkPlanDTO extends AbstractBaseTenantDTO {

    @NotBlank(message = "项目ID不能为空")
    @Size(max = 64, message = "项目ID长度不能超过64")
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Size(max = 64, message = "项目编号长度不能超过64")
    @Schema(description = "项目编号，后端回填")
    private String projectNo;

    @Size(max = 200, message = "项目名称长度不能超过200")
    @Schema(description = "项目名称，后端回填")
    private String projectName;

    @NotNull(message = "年份不能为空")
    @Min(value = 1900, message = "年份不能小于1900")
    @Max(value = 2100, message = "年份不能大于2100")
    @Schema(description = "年份", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer year;

    @DecimalMin(value = "0", message = "一季度（人/次）必须大于等于0")
    @Digits(integer = 7, fraction = 3, message = "一季度（人/次）最多保留3位小数")
    @Schema(description = "一季度（人/次）")
    private BigDecimal firstQuarterPersonTimes;

    @DecimalMin(value = "0", message = "二季度（人/次）必须大于等于0")
    @Digits(integer = 7, fraction = 3, message = "二季度（人/次）最多保留3位小数")
    @Schema(description = "二季度（人/次）")
    private BigDecimal secondQuarterPersonTimes;

    @DecimalMin(value = "0", message = "三季度（人/次）必须大于等于0")
    @Digits(integer = 7, fraction = 3, message = "三季度（人/次）最多保留3位小数")
    @Schema(description = "三季度（人/次）")
    private BigDecimal thirdQuarterPersonTimes;

    @DecimalMin(value = "0", message = "四季度（人/次）必须大于等于0")
    @Digits(integer = 7, fraction = 3, message = "四季度（人/次）最多保留3位小数")
    @Schema(description = "四季度（人/次）")
    private BigDecimal fourthQuarterPersonTimes;
}
