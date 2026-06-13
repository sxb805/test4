package com.vortex.cloud.test.dto;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vortex.cloud.vfs.lite.base.dto.AbstractBaseTenantDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 任务工单请求DTO
 */
@Data
@Schema(description = "任务工单请求DTO")
public class TaskWorkItemDTO extends AbstractBaseTenantDTO {

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

    @NotBlank(message = "所属TL ID不能为空")
    @Size(max = 64, message = "所属TL ID长度不能超过64")
    @Schema(description = "所属TL ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerTlId;

    @Size(max = 50, message = "所属TL名称长度不能超过50")
    @Schema(description = "所属TL名称，后端回填")
    private String ownerTlName;

    @Size(max = 100, message = "模块长度不能超过100")
    @Schema(description = "模块")
    private String moduleName;

    @NotBlank(message = "任务描述不能为空")
    @Size(max = 2000, message = "任务描述长度不能超过2000")
    @Schema(description = "任务描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskDesc;

    @NotNull(message = "开始日期不能为空")
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @Max(value = Integer.MAX_VALUE, message = "预计工时超出范围")
    @Schema(description = "预计工时")
    private Integer estimatedHours;

    @NotBlank(message = "责任人ID不能为空")
    @Size(max = 64, message = "责任人ID长度不能超过64")
    @Schema(description = "责任人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerId;

    @Size(max = 50, message = "责任人长度不能超过50")
    @Schema(description = "责任人，后端回填")
    private String ownerName;

    @NotBlank(message = "完成状态不能为空")
    @Pattern(regexp = "^(完成|延期)$", message = "完成状态仅支持：完成、延期")
    @Schema(description = "完成状态（完成/延期）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    @Schema(description = "实际完成日期")
    private LocalDate actualFinishDate;

    @Schema(description = "实际工时")
    private Integer actualHours;

    @Size(max = 64, message = "实际完成人ID长度不能超过64")
    @Schema(description = "实际完成人ID")
    private String actualOwnerId;

    @Size(max = 50, message = "实际完成人长度不能超过50")
    @Schema(description = "实际完成人，后端回填")
    private String actualOwnerName;

    @Size(max = 2000, message = "任务进度跟进描述长度不能超过2000")
    @Schema(description = "任务进度跟进描述")
    private String progressNote;
}
