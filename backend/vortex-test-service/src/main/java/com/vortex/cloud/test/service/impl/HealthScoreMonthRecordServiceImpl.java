package com.vortex.cloud.test.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.vortex.cloud.sdk.api.dto.ums.DivisionDTO;
import com.vortex.cloud.sdk.api.dto.ums.TenantDivisionQueryDTO;
import com.vortex.cloud.sdk.api.dto.ums.TenantDivisionSimpleVO;
import com.vortex.cloud.sdk.api.dto.zhswjcssv2.DistrictQueryDTO;
import com.vortex.cloud.sdk.api.dto.zhswjcssv2.NameValueDTO;
import com.vortex.cloud.sdk.api.service.IUmsService;
import com.vortex.cloud.sdk.api.service.IZhswJcssV2Service;
import com.vortex.cloud.test.domain.HealthScoreMonthRecord;
import com.vortex.cloud.test.dto.HealthScoreMonthRecordDTO;
import com.vortex.cloud.test.dto.HealthScoreMonthRecordQueryDTO;
import com.vortex.cloud.test.dto.HealthScoreMonthRecordVO;
import com.vortex.cloud.test.mapper.HealthScoreMonthRecordMapper;
import com.vortex.cloud.test.service.HealthScoreMonthRecordService;
import com.vortex.cloud.test.service.base.SubServiceImpl;
import com.vortex.cloud.vfs.common.exception.VortexException;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.excel.ExcelColumnDTO;
import com.vortex.cloud.vfs.lite.data.util.PageUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author donghao
 * @date 2025/4/22 11:06
 */
@Slf4j
@Service
public class HealthScoreMonthRecordServiceImpl extends SubServiceImpl<HealthScoreMonthRecordMapper, HealthScoreMonthRecord> implements HealthScoreMonthRecordService {

    private static final BeanCopier ENTITY_TO_VO = BeanCopier.create(HealthScoreMonthRecord.class, HealthScoreMonthRecordVO.class, false);
    private static final BeanCopier DTO_TO_ENTITY = BeanCopier.create(HealthScoreMonthRecordDTO.class, HealthScoreMonthRecord.class, false);

    @Resource
    private HealthScoreMonthRecordMapper healthScoreMonthRecordMapper;

    @Resource
    private IUmsService umsService;

    @Override
    public String getTableName(Object subTableValue) {
        if (subTableValue instanceof Date) {
            return HealthScoreMonthRecord.TABLE_NAME + "_" + DateUtil.format((Date) subTableValue, DatePattern.SIMPLE_MONTH_PATTERN);
        } else if (subTableValue instanceof String) {
            return HealthScoreMonthRecord.TABLE_NAME + "_" + subTableValue;
        }
        throw new VortexException("不支持的分表字段类型：" + subTableValue.getClass());
    }

    @Override
    public DataStoreDTO<HealthScoreMonthRecordVO> page(Pageable pageable, HealthScoreMonthRecordQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        QueryWrapper<HealthScoreMonthRecord> queryWrapper = this.buildQuery(queryDTO);
        if (StringUtils.hasText(queryDTO.getSort())) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
            String[] sort = queryDTO.getSort().split(",");
            queryWrapper.orderBy(sort.length == 2, Sort.Direction.ASC.name().toLowerCase().equals(sort[1]), "module_" + sort[0]);
        }
        Page<HealthScoreMonthRecord> page = PageUtils.transferPage(pageable);
        Page<HealthScoreMonthRecord> result = this.page(queryDTO.getMonth(), page, queryWrapper);
        return new DataStoreDTO<>(result.getTotal(), this.transferFromEntity(result.getRecords()));
    }

    @Override
    public List<HealthScoreMonthRecordVO> list(Pageable pageable, HealthScoreMonthRecordQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        Assert.notNull(queryDTO.getMonth(), "评估月份不能为空");

        QueryWrapper<HealthScoreMonthRecord> queryWrapper = this.buildQuery(queryDTO);
        Page<HealthScoreMonthRecord> page = PageUtils.transferPage(pageable);
        List<HealthScoreMonthRecord> result = super.list(queryDTO.getMonth(), page, queryWrapper);
        return this.transferFromEntity(result);
    }

    @Override
    public Map<String, Double> lengthSummary( HealthScoreMonthRecordQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        Assert.notNull(queryDTO.getMonth(), "评估月份不能为空");
        String tableName = this.getTableName(queryDTO.getMonth());
        List<HealthScoreMonthRecord> healthScoreMonthRecords = healthScoreMonthRecordMapper.groupByHealthLevelName(tableName, this.buildQuery(queryDTO));
        return healthScoreMonthRecords.stream().collect(Collectors.toMap(HealthScoreMonthRecord::getHealthLevelName, HealthScoreMonthRecord::getLineLength));
    }

    public List<HealthScoreMonthRecordVO> list(Sort sort, HealthScoreMonthRecordQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        Assert.notNull(queryDTO.getMonth(), "评估月份不能为空");

        QueryWrapper<HealthScoreMonthRecord> queryWrapper = this.buildQuery(queryDTO);
        Page<HealthScoreMonthRecord> page = PageUtils.transferSort(sort);
        List<HealthScoreMonthRecord> result = super.list(queryDTO.getMonth(), page, queryWrapper);
        return this.transferFromEntity(result);
    }

    @Override
    public HealthScoreMonthRecordVO selectOne(HealthScoreMonthRecordQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        Assert.notNull(queryDTO.getMonth(), "评估月份不能为空");
        Assert.isTrue(StrUtil.isNotBlank(queryDTO.getLineId()) || StrUtil.isNotBlank(queryDTO.getLineFacilityId()), "lineId或lineFacilityId不能同时为空");
        Assert.hasText(queryDTO.getModelId(), "模型id不能为空");
        QueryWrapper<HealthScoreMonthRecord> queryWrapper = this.buildQuery(queryDTO);
        HealthScoreMonthRecord one = getOne(queryDTO.getMonth(), queryWrapper);
        return this.transferFromEntity(one);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(HealthScoreMonthRecordQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        Assert.notNull(queryDTO.getMonth(), "评估月份不能为空");
        Assert.hasText(queryDTO.getModelId(), "模型id不能为空");
        Assert.hasText(queryDTO.getLineId(), "管线id不能为空");
        if (CollectionUtils.isEmpty(queryDTO.getIds())) {
            return;
        }

        this.removeByIds(queryDTO.getMonth(), queryDTO.getIds());
    }

    private List<HealthScoreMonthRecordVO> transferFromEntity(List<HealthScoreMonthRecord> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return Lists.newArrayList();
        }
        return entities.stream()
                .map(entity -> {
                    HealthScoreMonthRecordVO monthRecordVO = new HealthScoreMonthRecordVO();
                    ENTITY_TO_VO.copy(entity, monthRecordVO, null);

                    monthRecordVO.setMonthStr(DateUtil.format(entity.getMonth(), DatePattern.NORM_DATE_PATTERN));
                    return monthRecordVO;
                })
                .collect(Collectors.toList());
    }

    private HealthScoreMonthRecordVO transferFromEntity(HealthScoreMonthRecord entity) {
        if (Objects.isNull(entity)) {
            return null;
        }


        return this.transferFromEntity(Lists.newArrayList(entity)).getFirst();
    }

    private QueryWrapper<HealthScoreMonthRecord> buildQuery(HealthScoreMonthRecordQueryDTO queryDTO) {
        QueryWrapper<HealthScoreMonthRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getTenantId()), HealthScoreMonthRecord::getTenantId, queryDTO.getTenantId());
        queryWrapper.select(StrUtil.isNotBlank(queryDTO.getSelect()), queryDTO.getSelect());
        queryWrapper.lambda().eq(HealthScoreMonthRecord::getMonth, DateUtil.format(queryDTO.getMonth(), DatePattern.NORM_DATE_PATTERN));
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getLineId()), HealthScoreMonthRecord::getLineId, queryDTO.getLineId());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getLineFacilityId()), HealthScoreMonthRecord::getLineFacilityId, queryDTO.getLineFacilityId());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getModelId()), HealthScoreMonthRecord::getModelId, queryDTO.getModelId());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getHealthLevelId()), HealthScoreMonthRecord::getHealthLevelId, queryDTO.getHealthLevelId());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getLineCode()), HealthScoreMonthRecord::getLineCode, queryDTO.getLineCode());
        queryWrapper.lambda().like(StrUtil.isNotBlank(queryDTO.getLineCodeLike()), HealthScoreMonthRecord::getLineCode, queryDTO.getLineCodeLike());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getRegionObjectId()), HealthScoreMonthRecord::getRegionObjectId, queryDTO.getRegionObjectId());
        queryWrapper.lambda().like(StrUtil.isNotBlank(queryDTO.getRegionObjectName()), HealthScoreMonthRecord::getRegionObjectName, queryDTO.getRegionObjectName());
        queryWrapper.lambda().ge(queryDTO.getLeftScore() != null, HealthScoreMonthRecord::getScore, queryDTO.getLeftScore());
        queryWrapper.lambda().le(queryDTO.getRightScore() != null, HealthScoreMonthRecord::getScore, queryDTO.getRightScore());
        queryWrapper.lambda().in(CollectionUtils.isNotEmpty(queryDTO.getIds()), HealthScoreMonthRecord::getId, queryDTO.getIds());
        queryWrapper.lambda().in(CollectionUtils.isNotEmpty(queryDTO.getHealthLevelIdList()), HealthScoreMonthRecord::getHealthLevelId, queryDTO.getHealthLevelIdList());


        // 片区本级及以下，参考zhsw-jcss，通过Like查询实现（慎用）
        queryWrapper.lambda().like(StrUtil.isNotBlank(queryDTO.getDistrictIdLike()), HealthScoreMonthRecord::getDistrictId, queryDTO.getDistrictIdLike());

        // 行政区划本级及以下（优选）
        if (StrUtil.isNotBlank(queryDTO.getDivisionPrefix())) {
            DivisionDTO division = this.umsService.getTenantDivisionById(queryDTO.getDivisionPrefix());
            if (division != null) {
                queryWrapper.lambda().likeRight(HealthScoreMonthRecord::getDivisionNodeCode, division.getNodeCode());
            }
        }

        // 行政区划本级及以下
        if (StrUtil.isNotBlank(queryDTO.getDivisionId())) {
            TenantDivisionQueryDTO tenantDivisionQueryDTO = new TenantDivisionQueryDTO();
            tenantDivisionQueryDTO.setTenantId(queryDTO.getTenantId());
            tenantDivisionQueryDTO.setParentId(queryDTO.getDivisionId());
            List<TenantDivisionSimpleVO> divisionList = this.umsService.divisionList(queryDTO.getTenantId(), tenantDivisionQueryDTO);
            if (CollUtil.isNotEmpty(divisionList)) {
                List<String> divisionIds = divisionList.stream().map(TenantDivisionSimpleVO::getId).collect(Collectors.toList());
                queryWrapper.lambda().in(HealthScoreMonthRecord::getDivisionId, divisionIds);
            }
        }

        return queryWrapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveBatch(List<HealthScoreMonthRecordDTO> dtoList) {
        Map<String, List<HealthScoreMonthRecord>> subMap = new HashMap<>();
        for (HealthScoreMonthRecordDTO dto : dtoList) {
            Assert.notNull(dto.getMonth(), "评估月份不能为空");

            String subTableValue = DateUtil.format(dto.getMonth(), DatePattern.SIMPLE_MONTH_PATTERN);
            List<HealthScoreMonthRecord> entityList = subMap.getOrDefault(subTableValue, new ArrayList<>());
            entityList.add(this.transferToEntity(null, dto));
            subMap.put(subTableValue, entityList);
        }

        for (Map.Entry<String, List<HealthScoreMonthRecord>> entry : subMap.entrySet()) {
            super.saveBatch(entry.getKey(), entry.getValue());
        }
        return true;
    }

    @Override
    public boolean physicalDelete(Object subTableValue, QueryWrapper<HealthScoreMonthRecord> queryWrapper) {
        String tableName = this.getTableName(subTableValue);
        return this.healthScoreMonthRecordMapper.physicalDelete(tableName, queryWrapper) > 0;
    }

    private HealthScoreMonthRecord transferToEntity(HealthScoreMonthRecord entity, HealthScoreMonthRecordDTO dto) {
        if (Objects.isNull(entity)) {
            entity = new HealthScoreMonthRecord();
        }
        DTO_TO_ENTITY.copy(dto, entity, null);

        return entity;
    }

    public HashMap<String, String[]> getDownMap(String tenantId) {
        HashMap<String, String[]> downMap = new HashMap<>();
        return downMap;
    }

}
