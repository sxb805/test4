package com.vortex.cloud.test.controller;

import com.vortex.cloud.test.dto.HealthScoreMonthRecordQueryDTO;
import com.vortex.cloud.test.dto.HealthScoreMonthRecordVO;
import com.vortex.cloud.test.service.HealthScoreMonthRecordService;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Tag(name = "健康度得分-按月得分数据")
@RestController
@RequestMapping("healthScoreMonthRecord")
public class HealthScoreMonthRecordController {

    @Resource
    private HealthScoreMonthRecordService healthScoreMonthRecordService;

    @Operation(summary = "分页")
    @RequestMapping(value = "page", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<DataStoreDTO<HealthScoreMonthRecordVO>> page(@ParameterObject @PageableDefault(sort = "score", direction = Sort.Direction.ASC)  Pageable pageable,
                                                                      @Parameter(description = "租户ID") @RequestHeader String tenantId,
                                                                      @RequestBody HealthScoreMonthRecordQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(healthScoreMonthRecordService.page(pageable, queryDTO));
    }

    @Operation(summary = "管线长度统计")
    @RequestMapping(value = "lengthSummary", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<Object> lengthSummary(@Parameter(description = "租户ID") @RequestHeader String tenantId,@RequestBody HealthScoreMonthRecordQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(healthScoreMonthRecordService.lengthSummary(queryDTO));
    }

    @Operation(summary = "查询指定数据")
    @RequestMapping(value = "selectOne", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<HealthScoreMonthRecordVO> selectOne(@Parameter(description = "租户ID") @RequestHeader String tenantId,
                                                             @RequestBody HealthScoreMonthRecordQueryDTO queryDTO) {
        if (StringUtils.isNotEmpty(tenantId)) {
            queryDTO.setTenantId(tenantId);
        }
        return RestResultDTO.newSuccess(healthScoreMonthRecordService.selectOne(queryDTO));
    }

    @Operation(summary = "删除")
    @RequestMapping(value = "delete", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<Void> delete(@RequestBody HealthScoreMonthRecordQueryDTO queryDTO) {
        healthScoreMonthRecordService.delete(queryDTO);
        return RestResultDTO.newSuccess();
    }
}
