package com.vortex.cloud.test.dto;

import com.vortex.cloud.vfs.lite.base.dto.AbstractBaseTenantDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 项目展示VO
 */
@Data
@Schema(description = "项目展示VO")
public class ProjectVO extends AbstractBaseTenantDTO {

    @Schema(description = "编号")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "TL人员ID")
    private String tlId;

    @Schema(description = "TL名称")
    private String tlName;
}
