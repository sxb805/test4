package com.vortex.cloud.test.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vortex.cloud.vfs.lite.base.dto.AbstractBaseDTO;
import com.vortex.cloud.vfs.lite.base.dto.GeometryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
public class ExampleRecordVO  extends AbstractBaseDTO {

    @Schema(description = "时间")
    @JsonFormat(pattern = "yyyy-MM")
    private Date month;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "建设日期")
    private LocalDate buildDate;

    @Schema(description = "日期类型")
    private String dateType;

    @Schema(description = "日期类型名称")
    private String dateTypeName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "是否离线")
    private Boolean hasOffline;

    @Schema(description = "管理人员ID")
    private String managerStaffId;

    @Schema(description = "管理人员姓名")
    private String managerStaffName;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "版本")
    private Double version;

    @Schema(description = "附件，json数组")
    private String files;

    @Schema(description = "地理信息")
    private GeometryDTO location;

}
