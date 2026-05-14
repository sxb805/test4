package com.vortex.cloud.test.dto;

import cn.hutool.core.date.DatePattern;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

/**
 * @author zhanglei
 */
@Data
public class ExampleQueryDTO {

    @Parameter(description = "租户ID")
    private String tenantId;

    @Parameter(description = "ID集合")
    private Set<String> ids;

    @Parameter(description = "名称，模糊查询")
    private String name;

    @DateTimeFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    @Schema(description = "建设日期")
    private LocalDate buildDate;

}
