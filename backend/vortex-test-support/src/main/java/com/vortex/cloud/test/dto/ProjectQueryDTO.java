package com.vortex.cloud.test.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import java.util.Set;

/**
 * 项目查询DTO
 */
@Data
public class ProjectQueryDTO {

    @Parameter(description = "租户ID")
    private String tenantId;

    @Parameter(description = "ID集合")
    private Set<String> ids;

    @Parameter(description = "编号，模糊查询")
    private String code;

    @Parameter(description = "名称，模糊查询")
    private String name;

    @Parameter(description = "TL人员ID")
    private String tlId;
}
