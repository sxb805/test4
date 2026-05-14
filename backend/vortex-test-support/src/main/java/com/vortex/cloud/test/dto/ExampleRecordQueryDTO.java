package com.vortex.cloud.test.dto;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import java.util.Date;
import java.util.Set;

/**
 * @author zhanglei
 */
@Data
public class ExampleRecordQueryDTO {

    @JsonFormat(timezone = "GMT+8", pattern = DatePattern.NORM_MONTH_PATTERN)
    @Parameter(description = "评估月份")
    private Date month;

    @Parameter(description = "租户ID")
    private String tenantId;

    @Parameter(description = "ID")
    private String id;

    @Parameter(description = "ID集合")
    private Set<String> ids;

    @Parameter(description = "名称，模糊查询")
    private String name;

}
