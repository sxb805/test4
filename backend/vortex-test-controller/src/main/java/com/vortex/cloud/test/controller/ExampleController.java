package com.vortex.cloud.test.controller;

import com.vortex.cloud.test.dto.ExampleDTO;
import com.vortex.cloud.test.dto.ExampleQueryDTO;
import com.vortex.cloud.test.dto.ExampleVO;
import com.vortex.cloud.test.service.ExampleService;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import com.vortex.cloud.vfs.lite.base.util.ExcelUtils;
import com.vortex.cloud.vfs.lite.base.support.Constants;

import cn.hutool.core.util.StrUtil;
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
@Tag(name = "样例接口")
@RestController
@RequestMapping("example")
public class ExampleController {
    @Autowired
    private ExampleService exampleService;

    @Operation(summary = "分页")
    @RequestMapping(value = "page", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<DataStoreDTO<ExampleVO>> page(@ParameterObject @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                       @Parameter(description = "租户ID") @RequestHeader String tenantId,
                                                       @Parameter(description = "用户ID") @RequestHeader String userId,
                                                       @ParameterObject ExampleQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(exampleService.page(pageable, queryDTO));
    }

    @Operation(summary = "列表")
    @RequestMapping(value = "list", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<List<ExampleVO>> list(@ParameterObject @SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort,
                                               @Parameter(description = "租户ID") @RequestHeader String tenantId,
                                               @Parameter(description = "用户ID") @RequestHeader String userId,
                                               @ParameterObject ExampleQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(exampleService.list(sort, queryDTO));
    }

    @Operation(summary = "保存")
    @PostMapping(value = "save")
    public RestResultDTO<Void> save(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                    @Validated @RequestBody ExampleDTO dto) {
        if (StringUtils.isNotEmpty(tenantId)) {
            dto.setTenantId(tenantId);
        }
        exampleService.save(dto);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "修改")
    @PostMapping(value = "update")
    public RestResultDTO<Void> update(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                      @Validated @RequestBody ExampleDTO dto) {
        if (StringUtils.isNotEmpty(tenantId)) {
            dto.setTenantId(tenantId);
        }
        exampleService.update(dto);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "删除")
    @RequestMapping(value = "delete", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<Void> delete(@Parameter(description = "记录ID集合") @RequestParam Set<String> ids) {
        exampleService.delete(ids);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "查看")
    @RequestMapping(value = "get", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<ExampleVO> get(@Parameter(description = "记录ID") @RequestParam String id) {
        return RestResultDTO.newSuccess(exampleService.get(id));
    }

    @Operation(summary = "校验")
    @RequestMapping(value = "exist", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<Boolean> exist(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                        @Parameter(description = "记录ID") @RequestParam(required = false) String id,
                                        @Parameter(description = "字段名") @RequestParam String key,
                                        @Parameter(description = "字段值") @RequestParam String value) {
        if (exampleService.exist(tenantId, id, key, value)) {
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
            return exampleService.importExcel(tenantId, file, startRowNum, startCellNum);
        } catch (Exception e) {
            return RestResultDTO.newFail(e.getMessage());
        }
    }



    @Operation(summary = "导出Excel")
    @RequestMapping(value = "exportExcel", method = {RequestMethod.POST, RequestMethod.GET})
    public void exportExcel(@ParameterObject @SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort,
                                             @Parameter(description = "租户ID") @RequestHeader String tenantId,
                                             @Parameter(description = "用户ID") @RequestHeader String userId,
                                             @ParameterObject ExampleQueryDTO queryDTO,
                                             @Parameter(description = "导出文件名") @RequestParam(defaultValue = "导出样例数据") String fileName,
                                             @Parameter(description = "文件扩展名") @RequestParam(defaultValue = Constants.EXTENSION_XLSX) String extension,
                                             @Parameter(description = "导出列JSON") @RequestParam(required = false) String columnJson,
                                            HttpServletResponse response) throws Exception {
        if (Objects.isNull(queryDTO)) {
            queryDTO = new ExampleQueryDTO();
        }
        queryDTO.setTenantId(tenantId);
        columnJson = StrUtil.isNotBlank(columnJson) ? columnJson : "[{\"title\":\"编码\",\"field\":\"code\"},{\"title\":\"名称\",\"field\":\"name\"}]";

        List<ExampleVO> exampleVOS = this.exampleService.list(sort, queryDTO);
        ExcelUtils.exportExcel(fileName, extension, null, columnJson, exampleVOS, response);
    }
}
