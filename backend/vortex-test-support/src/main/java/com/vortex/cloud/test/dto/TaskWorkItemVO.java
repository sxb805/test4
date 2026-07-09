package com.vortex.cloud.test.dto;

import com.vortex.cloud.vfs.lite.base.dto.AbstractBaseTenantDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 任务工单展示VO
 */
@Data
@Schema(description = "任务工单展示VO")
public class TaskWorkItemVO extends AbstractBaseTenantDTO {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "项目编号")
    private String projectNo;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "项目类型名称")
    private String projectTypeName;

    @Schema(description = "所属公司")
    private String company;

    @Schema(description = "所属公司名称")
    private String companyName;

    @Schema(description = "所属TL ID")
    private String ownerTlId;

    @Schema(description = "所属TL名称")
    private String ownerTlName;

    @Schema(description = "模块")
    private String moduleName;

    @Schema(description = "任务描述")
    private String taskDesc;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "预计工时")
    private Integer estimatedHours;

    @Schema(description = "责任人ID")
    private String ownerId;

    @Schema(description = "责任人")
    private String ownerName;

    @Schema(description = "完成状态")
    private String status;

    @Schema(description = "实际完成日期")
    private LocalDate actualFinishDate;

    @Schema(description = "实际工时")
    private Integer actualHours;

    @Schema(description = "实际完成人ID")
    private String actualOwnerId;

    @Schema(description = "实际完成人")
    private String actualOwnerName;

    @Schema(description = "任务进度跟进描述")
    private String progressNote;
}
