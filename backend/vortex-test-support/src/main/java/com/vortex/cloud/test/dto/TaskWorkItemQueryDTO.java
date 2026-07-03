package com.vortex.cloud.test.dto;

import cn.hutool.core.date.DatePattern;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

/**
 * 任务工单查询DTO
 */
@Data
public class TaskWorkItemQueryDTO {

    @Parameter(description = "租户ID")
    private String tenantId;

    @Parameter(description = "ID集合")
    private Set<String> ids;

    @Parameter(description = "项目ID")
    private String projectId;

    @Parameter(description = "项目类型")
    private String projectType;

    @Parameter(description = "所属TL ID")
    private String ownerTlId;

    @DateTimeFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    @Parameter(description = "开始日期-起")
    private LocalDate startDateBegin;

    @DateTimeFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    @Parameter(description = "开始日期-止")
    private LocalDate startDateEnd;

    @Parameter(description = "完成状态")
    private String status;
}
