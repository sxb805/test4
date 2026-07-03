package com.vortex.cloud.test.controller;

import cn.hutool.core.util.StrUtil;
import com.vortex.cloud.test.dto.ProjectDTO;
import com.vortex.cloud.test.dto.ProjectQueryDTO;
import com.vortex.cloud.test.dto.ProjectVO;
import com.vortex.cloud.test.service.ProjectService;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import com.vortex.cloud.vfs.lite.base.support.Constants;
import com.vortex.cloud.vfs.lite.base.util.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 项目接口
 */
@Tag(name = "项目接口")
@RestController
@RequestMapping("project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Operation(summary = "分页")
    @RequestMapping(value = "page", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<DataStoreDTO<ProjectVO>> page(@ParameterObject @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                       @Parameter(description = "租户ID") @RequestHeader String tenantId,
                                                       @Parameter(description = "用户ID") @RequestHeader String userId,
                                                       @ParameterObject ProjectQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(projectService.page(pageable, queryDTO));
    }

    @Operation(summary = "列表")
    @RequestMapping(value = "list", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<List<ProjectVO>> list(@ParameterObject @SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort,
                                               @Parameter(description = "租户ID") @RequestHeader String tenantId,
                                               @Parameter(description = "用户ID") @RequestHeader String userId,
                                               @ParameterObject ProjectQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(projectService.list(sort, queryDTO));
    }

    @Operation(summary = "保存")
    @PostMapping(value = "save")
    public RestResultDTO<Void> save(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                    @Validated @org.springframework.web.bind.annotation.RequestBody ProjectDTO dto) {
        if (StringUtils.isNotEmpty(tenantId)) {
            dto.setTenantId(tenantId);
        }
        projectService.save(dto);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "修改")
    @PostMapping(value = "update")
    public RestResultDTO<Void> update(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                      @Validated @org.springframework.web.bind.annotation.RequestBody ProjectDTO dto) {
        if (StringUtils.isNotEmpty(tenantId)) {
            dto.setTenantId(tenantId);
        }
        projectService.update(dto);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "删除")
    @RequestMapping(value = "delete", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<Void> delete(@Parameter(description = "记录ID集合") @RequestParam Set<String> ids) {
        projectService.delete(ids);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "查看")
    @GetMapping(value = "get")
    public RestResultDTO<ProjectVO> get(@Parameter(description = "记录ID") @RequestParam String id) {
        return RestResultDTO.newSuccess(projectService.get(id));
    }

    @Operation(summary = "校验")
    @RequestMapping(value = "exist", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<Boolean> exist(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                        @Parameter(description = "记录ID") @RequestParam(required = false) String id,
                                        @Parameter(description = "字段名") @RequestParam String key,
                                        @Parameter(description = "字段值") @RequestParam String value) {
        if (projectService.exist(tenantId, id, key, value)) {
            return RestResultDTO.newSuccess(Boolean.TRUE, "已存在");
        }
        return RestResultDTO.newSuccess(Boolean.FALSE, "不存在");
    }

    @Operation(summary = "导入Excel")
    @PostMapping(value = "importExcel")
    public RestResultDTO<?> importExcel(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                        @Parameter(description = "文件") @RequestPart MultipartFile file,
                                        @Parameter(description = "开始读取数据的行索引") @RequestParam(defaultValue = "1") Integer startRowNum,
                                        @Parameter(description = "开始读取数据的列索引") @RequestParam(defaultValue = "0") Integer startCellNum) {
        try {
            return projectService.importExcel(tenantId, file, startRowNum, startCellNum);
        } catch (Exception e) {
            return RestResultDTO.newFail(e.getMessage());
        }
    }

    @Operation(summary = "导出Excel")
    @RequestMapping(value = "exportExcel", method = {RequestMethod.POST, RequestMethod.GET})
    public void exportExcel(@ParameterObject @SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort,
                            @Parameter(description = "租户ID") @RequestHeader String tenantId,
                            @Parameter(description = "用户ID") @RequestHeader String userId,
                            @ParameterObject ProjectQueryDTO queryDTO,
                            @Parameter(description = "导出文件名") @RequestParam(defaultValue = "导出项目数据") String fileName,
                            @Parameter(description = "文件扩展名") @RequestParam(defaultValue = Constants.EXTENSION_XLSX) String extension,
                            @Parameter(description = "导出列JSON") @RequestParam(required = false) String columnJson,
                            HttpServletResponse response) throws Exception {
        if (Objects.isNull(queryDTO)) {
            queryDTO = new ProjectQueryDTO();
        }
        queryDTO.setTenantId(tenantId);
        columnJson = StrUtil.isNotBlank(columnJson)
                ? columnJson
                : "[{\"title\":\"编号\",\"field\":\"code\"},{\"title\":\"名称\",\"field\":\"name\"},{\"title\":\"类型\",\"field\":\"typeName\"},{\"title\":\"TL\",\"field\":\"tlName\"}]";

        List<ProjectVO> projectVOS = this.projectService.list(sort, queryDTO);
        ExcelUtils.exportExcel(fileName, extension, null, columnJson, projectVOS, response);
    }
}
