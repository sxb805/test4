package com.vortex.cloud.test.dto;

import com.vortex.cloud.vfs.lite.base.dto.AbstractBaseTenantDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 项目请求DTO
 */
@Data
@Schema(description = "项目请求DTO")
public class ProjectDTO extends AbstractBaseTenantDTO {

    @NotBlank(message = "编号不能为空")
    @Size(max = 32, message = "编号长度不能超过32")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "编号格式不正确，仅支持字母、数字、下划线、中划线")
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过100")
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "类型不能为空")
    @Size(max = 20, message = "类型长度不能超过20")
    @Schema(description = "类型：PROJECT-项目，PRODUCT-产品", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @NotBlank(message = "TL人员ID不能为空")
    @Size(max = 64, message = "TL人员ID长度不能超过64")
    @Schema(description = "TL人员ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tlId;

    @Size(max = 50, message = "TL名称长度不能超过50")
    @Schema(description = "TL名称，后端回填")
    private String tlName;
}
