package com.vortex.cloud.test.controller;

import cn.hutool.core.util.StrUtil;
import com.vortex.cloud.test.dto.*;
import com.vortex.cloud.test.service.ExampleRecordService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author zhanglei
 */
@Tag(name = "样例记录接口（按月分表）")
@RestController
@RequestMapping("exampleRecord")
public class ExampleRecordController {

    @Autowired
    private ExampleRecordService exampleRecordService;

    @Operation(summary = "分页")
    @RequestMapping(value = "page", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<DataStoreDTO<ExampleRecordVO>> page(@ParameterObject @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                             @Parameter(description = "租户ID") @RequestHeader String tenantId,
                                                             @Parameter(description = "用户ID") @RequestHeader String userId,
                                                             @ParameterObject ExampleRecordQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(exampleRecordService.page(pageable, queryDTO));
    }

    @Operation(summary = "列表")
    @RequestMapping(value = "list", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<List<ExampleRecordVO>> list(@ParameterObject @SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort,
                                               @Parameter(description = "租户ID") @RequestHeader String tenantId,
                                               @Parameter(description = "用户ID") @RequestHeader String userId,
                                               @ParameterObject ExampleRecordQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(exampleRecordService.list(sort, queryDTO));
    }

    @Operation(summary = "保存")
    @PostMapping(value = "save")
    public RestResultDTO<Void> save(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                    @Parameter(description = "用户ID") @RequestHeader String userId,
                                    @Validated @RequestBody ExampleRecordDTO dto) {
        if (StringUtils.isNotEmpty(tenantId)) {
            dto.setTenantId(tenantId);
        }
        exampleRecordService.save(dto);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "修改")
    @PostMapping(value = "update")
    public RestResultDTO<Void> update(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                      @Parameter(description = "用户ID") @RequestHeader String userId,
                                      @Validated @RequestBody ExampleRecordDTO dto) {
        if (StringUtils.isNotEmpty(tenantId)) {
            dto.setTenantId(tenantId);
        }
        exampleRecordService.update(dto);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "删除")
    @RequestMapping(value = "delete", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<Void> delete(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                      @Parameter(description = "用户ID") @RequestHeader String userId,
                                      @ParameterObject ExampleRecordQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        exampleRecordService.delete(queryDTO);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "查看")
    @RequestMapping(value = "get", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<ExampleRecordVO> get(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                              @Parameter(description = "用户ID") @RequestHeader String userId,
                                              @ParameterObject ExampleRecordQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(exampleRecordService.get(queryDTO));
    }

    @Operation(summary = "校验")
    @RequestMapping(value = "exist", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<Boolean> exist(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                        @Parameter(description = "用户ID") @RequestHeader String userId,
                                        @ParameterObject ExampleRecordQueryDTO queryDTO,
                                        @Parameter(description = "字段名") @RequestParam String key,
                                        @Parameter(description = "字段值") @RequestParam String value) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        if (exampleRecordService.exist(queryDTO.getMonth(),queryDTO.getTenantId(), queryDTO.getId(), key, value)) {
            return RestResultDTO.newSuccess(Boolean.TRUE, "已存在");
        } else {
            return RestResultDTO.newSuccess(Boolean.FALSE, "不存在");
        }
    }

    @Operation(summary = "导入Excel")
    @PostMapping(value = "importExcel")
    public RestResultDTO<?> importExcel(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                        @Parameter(description = "文件") @RequestPart MultipartFile file,
                                        @Parameter(description = "开始读取数据的行索引") @RequestParam(defaultValue = "1") Integer startRowNum,
                                        @Parameter(description = "开始读取数据的列索引") @RequestParam(defaultValue = "1") Integer startCellNum) {
        try {
            return exampleRecordService.importExcel(tenantId, file, startRowNum, startCellNum);
        } catch (Exception e) {
            return RestResultDTO.newFail(e.getMessage());
        }
    }

    @Operation(summary = "导出Excel")
    @RequestMapping(value = "exportExcel", method = {RequestMethod.POST, RequestMethod.GET})
    public void exportExcel(@ParameterObject @SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort,
                            @Parameter(description = "租户ID") @RequestHeader String tenantId,
                            @Parameter(description = "用户ID") @RequestHeader String userId,
                            @ParameterObject ExampleRecordQueryDTO queryDTO,
                            @Parameter(description = "导出文件名") @RequestParam(defaultValue = "导出样例数据") String fileName,
                            @Parameter(description = "文件扩展名") @RequestParam(defaultValue = Constants.EXTENSION_XLSX) String extension,
                            @Parameter(description = "导出列JSON") @RequestParam(required = false) String columnJson,
                            HttpServletResponse response) throws Exception {
        if (Objects.isNull(queryDTO)) {
            queryDTO = new ExampleRecordQueryDTO();
        }
        queryDTO.setTenantId(tenantId);
        columnJson = StrUtil.isNotBlank(columnJson) ? columnJson : "[{\"title\":\"编码\",\"field\":\"code\"},{\"title\":\"名称\",\"field\":\"name\"}]";

        List<ExampleRecordVO> exampleVOS = this.exampleRecordService.list(sort, queryDTO);
        ExcelUtils.exportExcel(fileName, extension, null, columnJson, exampleVOS, response);
    }
}
