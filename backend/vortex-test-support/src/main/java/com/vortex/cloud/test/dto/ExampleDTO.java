package com.vortex.cloud.test.dto;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vortex.cloud.vfs.lite.base.dto.AbstractBaseTenantDTO;
import com.vortex.cloud.vfs.lite.base.dto.GeometryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author zhanglei
 */
@Data
public class ExampleDTO extends AbstractBaseTenantDTO {

    @Schema(description = "编码")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "类型名称")
    private String typeName;

    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    @Schema(description = "建设日期")
    private LocalDate buildDate;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @Schema(description = "建设时间")
    private LocalDateTime buildTime;

    @Schema(description = "日期类型")
    private String dateType;

    @Schema(description = "日期类型名称")
    private String dateTypeName;

    @Schema(description = "是否离线")
    private Boolean hasOffline;

    @Schema(description = "管理人员ID")
    private String managerStaffId;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "版本")
    private Double version;

    @Schema(description = "附件，json数组")
    private String files;

    @Schema(description = "地理信息")
    private GeometryDTO location;


}
