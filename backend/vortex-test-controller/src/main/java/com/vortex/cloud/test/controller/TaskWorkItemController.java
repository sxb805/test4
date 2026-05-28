package com.vortex.cloud.test.controller;

import cn.hutool.core.util.StrUtil;
import com.vortex.cloud.test.dto.TaskWorkItemDTO;
import com.vortex.cloud.test.dto.TaskWorkItemQueryDTO;
import com.vortex.cloud.test.dto.TaskWorkItemVO;
import com.vortex.cloud.test.dto.TaskWorkItemWeeklyOccupancyVO;
import com.vortex.cloud.test.service.TaskWorkItemService;
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
import org.springframework.web.bind.annotation.RequestBody;
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
 * 任务工单接口
 */
@Tag(name = "任务工单接口")
@RestController
@RequestMapping("taskWorkItem")
public class TaskWorkItemController {

    @Autowired
    private TaskWorkItemService taskWorkItemService;

    @Operation(summary = "分页")
    @RequestMapping(value = "page", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<DataStoreDTO<TaskWorkItemVO>> page(@ParameterObject @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                            @Parameter(description = "租户ID") @RequestHeader String tenantId,
                                                            @Parameter(description = "用户ID") @RequestHeader String userId,
                                                            @ParameterObject TaskWorkItemQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(taskWorkItemService.page(pageable, queryDTO));
    }

    @Operation(summary = "列表")
    @RequestMapping(value = "list", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<List<TaskWorkItemVO>> list(@ParameterObject @SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort,
                                                    @Parameter(description = "租户ID") @RequestHeader String tenantId,
                                                    @Parameter(description = "用户ID") @RequestHeader String userId,
                                                    @ParameterObject TaskWorkItemQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(taskWorkItemService.list(sort, queryDTO));
    }

    @Operation(summary = "保存")
    @PostMapping(value = "save")
    public RestResultDTO<Void> save(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                    @Validated @RequestBody TaskWorkItemDTO dto) {
        if (StringUtils.isNotEmpty(tenantId)) {
            dto.setTenantId(tenantId);
        }
        taskWorkItemService.save(dto);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "修改")
    @PostMapping(value = "update")
    public RestResultDTO<Void> update(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                      @Validated @RequestBody TaskWorkItemDTO dto) {
        if (StringUtils.isNotEmpty(tenantId)) {
            dto.setTenantId(tenantId);
        }
        taskWorkItemService.update(dto);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "删除")
    @RequestMapping(value = "delete", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<Void> delete(@Parameter(description = "记录ID集合") @RequestParam Set<String> ids) {
        taskWorkItemService.delete(ids);
        return RestResultDTO.newSuccess();
    }

    @Operation(summary = "查看")
    @GetMapping(value = "get")
    public RestResultDTO<TaskWorkItemVO> get(@Parameter(description = "记录ID") @RequestParam String id) {
        return RestResultDTO.newSuccess(taskWorkItemService.get(id));
    }

    @Operation(summary = "责任人周占用统计")
    @RequestMapping(value = "weeklyOccupancy", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<TaskWorkItemWeeklyOccupancyVO> weeklyOccupancy(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                                                        @Parameter(description = "用户ID") @RequestHeader String userId,
                                                                        @ParameterObject TaskWorkItemQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(taskWorkItemService.weeklyOccupancy(queryDTO));
    }

    @Operation(summary = "导入Excel")
    @PostMapping(value = "importExcel")
    public RestResultDTO<?> importExcel(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                        @Parameter(description = "文件") @RequestPart MultipartFile file,
                                        @Parameter(description = "开始读取数据的行索引") @RequestParam(defaultValue = "1") Integer startRowNum,
                                        @Parameter(description = "开始读取数据的列索引") @RequestParam(defaultValue = "0") Integer startCellNum) {
        try {
            return taskWorkItemService.importExcel(tenantId, file, startRowNum, startCellNum);
        } catch (Exception e) {
            return RestResultDTO.newFail(e.getMessage());
        }
    }

    @Operation(summary = "导出Excel")
    @RequestMapping(value = "exportExcel", method = {RequestMethod.POST, RequestMethod.GET})
    public void exportExcel(@ParameterObject @SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort,
                            @Parameter(description = "租户ID") @RequestHeader String tenantId,
                            @Parameter(description = "用户ID") @RequestHeader String userId,
                            @ParameterObject TaskWorkItemQueryDTO queryDTO,
                            @Parameter(description = "导出文件名") @RequestParam(defaultValue = "导出任务工单数据") String fileName,
                            @Parameter(description = "文件扩展名") @RequestParam(defaultValue = Constants.EXTENSION_XLSX) String extension,
                            @Parameter(description = "导出列JSON") @RequestParam(required = false) String columnJson,
                            HttpServletResponse response) throws Exception {
        if (Objects.isNull(queryDTO)) {
            queryDTO = new TaskWorkItemQueryDTO();
        }
        queryDTO.setTenantId(tenantId);
        columnJson = StrUtil.isNotBlank(columnJson)
                ? columnJson
                : "[{\"title\":\"项目编号\",\"field\":\"projectNo\"},{\"title\":\"项目名称\",\"field\":\"projectName\"},{\"title\":\"所属TL\",\"field\":\"ownerTlName\"},{\"title\":\"模块\",\"field\":\"moduleName\"},{\"title\":\"任务描述\",\"field\":\"taskDesc\"},{\"title\":\"开始日期\",\"field\":\"startDate\"},{\"title\":\"结束日期\",\"field\":\"endDate\"},{\"title\":\"预计工时\",\"field\":\"estimatedHours\"},{\"title\":\"责任人\",\"field\":\"ownerName\"},{\"title\":\"完成状态\",\"field\":\"status\"},{\"title\":\"实际完成日期\",\"field\":\"actualFinishDate\"},{\"title\":\"实际工时\",\"field\":\"actualHours\"},{\"title\":\"实际完成人\",\"field\":\"actualOwnerName\"},{\"title\":\"任务进度跟进描述\",\"field\":\"progressNote\"}]";

        List<TaskWorkItemVO> rows = this.taskWorkItemService.list(sort, queryDTO);
        ExcelUtils.exportExcel(fileName, extension, null, columnJson, rows, response);
    }
}
