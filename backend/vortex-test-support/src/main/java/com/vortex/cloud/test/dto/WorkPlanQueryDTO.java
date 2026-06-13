package com.vortex.cloud.test.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import java.util.Set;

/**
 * 工作计划查询DTO
 */
@Data
public class WorkPlanQueryDTO {

    @Parameter(description = "租户ID")
    private String tenantId;

    @Parameter(description = "ID集合")
    private Set<String> ids;

    @Parameter(description = "项目ID")
    private String projectId;

    @Parameter(description = "项目编号，模糊查询")
    private String projectNo;

    @Parameter(description = "项目名称，模糊查询")
    private String projectName;

    @Parameter(description = "年份")
    private Integer year;
}
