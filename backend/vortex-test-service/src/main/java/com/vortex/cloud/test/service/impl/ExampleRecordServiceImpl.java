package com.vortex.cloud.test.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.vortex.cloud.sdk.api.dto.ums.CloudUserDTO;
import com.vortex.cloud.sdk.api.dto.ums.ParamSettingDTO;
import com.vortex.cloud.sdk.api.dto.ums.SimpleStaffDTO;
import com.vortex.cloud.sdk.api.service.IUmsService;
import com.vortex.cloud.test.domain.ExampleRecord;
import com.vortex.cloud.test.dto.*;
import com.vortex.cloud.test.enums.ExampleEnum;
import com.vortex.cloud.test.mapper.ExampleRecordMapper;
import com.vortex.cloud.test.service.ExampleRecordService;
import com.vortex.cloud.test.service.base.SubServiceImpl;
import com.vortex.cloud.test.support.Constants;
import com.vortex.cloud.vfs.common.exception.VortexException;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.GeometryDTO;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import com.vortex.cloud.vfs.lite.base.excel.ExcelImportCell;
import com.vortex.cloud.vfs.lite.base.excel.ExcelImportField;
import com.vortex.cloud.vfs.lite.base.excel.ExcelImportRow;
import com.vortex.cloud.vfs.lite.base.excel.ExcelReader;
import com.vortex.cloud.vfs.lite.base.util.GeometryUtils;
import com.vortex.cloud.vfs.lite.data.util.PageUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author donghao
 * @date 2025/4/22 11:06
 */
@Transactional(readOnly = true)
@Service
public class ExampleRecordServiceImpl  extends SubServiceImpl<ExampleRecordMapper, ExampleRecord> implements ExampleRecordService {

    private static final BeanCopier ENTITY_TO_VO = BeanCopier.create(ExampleRecord.class, ExampleRecordVO.class, false);
    private static final BeanCopier DTO_TO_ENTITY = BeanCopier.create(ExampleRecordDTO.class, ExampleRecord.class, false);

    @Resource
    private IUmsService umsService;

    @Override
    public String getTableName(Object subTableValue) {
        if (subTableValue instanceof Date) {
            return ExampleRecord.TABLE_NAME + "_" + DateUtil.format((Date) subTableValue, DatePattern.SIMPLE_MONTH_PATTERN);
        } else if (subTableValue instanceof String) {
            return ExampleRecord.TABLE_NAME + "_" + subTableValue;
        }
        throw new VortexException("不支持的分表字段类型：" + subTableValue.getClass());
    }

    @Override
    public DataStoreDTO<ExampleRecordVO> page(Pageable pageable, ExampleRecordQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        Assert.notNull(queryDTO.getMonth(),"分表key月份不能为空");

        QueryWrapper<ExampleRecord> queryWrapper = this.buildQuery(queryDTO);
        Page<ExampleRecord> page = PageUtils.transferPage(pageable);
        Page<ExampleRecord> result = this.page(queryDTO.getMonth(),page, queryWrapper);
        return new DataStoreDTO<>(result.getTotal(), this.transferFromEntity(result.getRecords()));
    }

    @Override
    public List<ExampleRecordVO> list(Sort sort, ExampleRecordQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        Assert.notNull(queryDTO.getMonth(),"分表key月份不能为空");

        QueryWrapper<ExampleRecord> queryWrapper = this.buildQuery(queryDTO);
        PageUtils.transferSort(queryWrapper, sort);
        List<ExampleRecord> records = this.list(queryDTO.getMonth(),queryWrapper);
        return this.transferFromEntity(records);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(ExampleRecordDTO dto) {
        checkData(dto);
        // 唯一性字段校验
        Assert.isTrue(!this.exist(dto.getMonth(),dto.getTenantId(), dto.getId(), "code", dto.getCode()), "编码为" + dto.getCode() + "的记录已存在");
        this.save(dto.getMonth(),this.transferToEntity(null, dto));
    }

    @Override
    public ExampleRecordVO get(ExampleRecordQueryDTO dto) {
        Assert.notNull(dto.getMonth(),"分表key月份不能为空");
        Assert.hasText(dto.getId(), "ID不能为空");
        ExampleRecord entity = this.getById(dto.getMonth(),dto.getId());
        Assert.notNull(entity, "找不到ID为" + dto.getId() + " 的记录");
        return this.transferFromEntity(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(ExampleRecordDTO dto) {
        Assert.hasText(dto.getId(), "ID不能为空");
        checkData(dto);
        // 唯一性字段校验
        Assert.isTrue(!this.exist(dto.getMonth(),dto.getTenantId(), dto.getId(), "code", dto.getCode()), "编码为" + dto.getCode() + "的记录已存在");
        ExampleRecord entity = this.getById(dto.getMonth(),dto.getId());
        Assert.notNull(entity, "找不到id为" + dto.getId() + " 的记录");
        this.updateById(dto.getMonth(),this.transferToEntity(entity, dto));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(ExampleRecordQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        Assert.notNull(queryDTO.getMonth(), "评估月份不能为空");
        if (CollectionUtils.isEmpty(queryDTO.getIds())) {
            return;
        }

        this.removeByIds(queryDTO.getMonth(), queryDTO.getIds());
    }

    @Override
    public Boolean exist(Object shardingKey, String tenantId, String id, String key, String value) {
        Assert.notNull(shardingKey,"分表key月份不能为空");
        Assert.hasText(tenantId, "租户ID不能为空");
        Assert.hasText(key, "字段名不能为空");

        QueryWrapper<ExampleRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ExampleRecord::getTenantId, tenantId);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.lambda().ne(ExampleRecord::getId, id);
        }
        queryWrapper.eq(StrUtil.toUnderlineCase(key), value);
        return this.count(shardingKey,queryWrapper) >= 1;
    }

    private void checkData(ExampleRecordDTO dto) {
        Assert.notNull(dto.getMonth(),"时间不能为空");
        Assert.hasText(dto.getTenantId(), "租户ID不能为空");
        Assert.hasText(dto.getCode(), "编码不能为空");
        Assert.hasText(dto.getName(), "名称不能为空");

        if (StrUtil.isNotBlank(dto.getDateType())) {
            ExampleEnum anEnum = ExampleEnum.getByKey(dto.getDateType());
            Assert.notNull(anEnum, "日期类型：" + dto.getDateType() + "不存在");
        }

        if (StrUtil.isNotBlank(dto.getManagerStaffId())) {
            CloudUserDTO userDTO = this.umsService.getUserByStaffId(dto.getTenantId(), dto.getManagerStaffId());
            Assert.notNull(userDTO, "管理人员不存在");
        }
        if (StrUtil.isNotBlank(dto.getType())) {
            List<ParamSettingDTO> byParamTypeCode = this.umsService.getByParamTypeCode(dto.getTenantId(), Constants.PARAM_TYPE_EXAMPLE_TYPE);
            Map<String, ParamSettingDTO> paramMap = byParamTypeCode.stream().collect(Collectors.toMap(ParamSettingDTO::getParmCode, Function.identity()));
            Assert.notNull(paramMap.get(dto.getType()), "类型编码：" + dto.getType() + "不存在");
        }
    }

    private QueryWrapper<ExampleRecord> buildQuery(ExampleRecordQueryDTO queryDTO) {
        QueryWrapper<ExampleRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getTenantId()), ExampleRecord::getTenantId, queryDTO.getTenantId());
        queryWrapper.lambda().like(StringUtils.isNotBlank(queryDTO.getName()), ExampleRecord::getName, queryDTO.getName());
        queryWrapper.lambda().in(CollUtil.isNotEmpty(queryDTO.getIds()), ExampleRecord::getId, queryDTO.getIds());
        return queryWrapper;
    }

    private List<ExampleRecordVO> transferFromEntity(List<ExampleRecord> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return Lists.newArrayList();
        }

        String tenantId = entities.get(0).getTenantId();

        List<SimpleStaffDTO> simpleStaffDTOS = umsService.loadSimpleStaffs(tenantId);
        Map<String, SimpleStaffDTO> simpleStaffMap = simpleStaffDTOS.stream().collect(Collectors.toMap(SimpleStaffDTO::getId, Function.identity()));

        List<ParamSettingDTO> byParamTypeCode = this.umsService.getByParamTypeCode(tenantId, Constants.PARAM_TYPE_EXAMPLE_TYPE);
        Map<String, ParamSettingDTO> paramMap = byParamTypeCode.stream().collect(Collectors.toMap(ParamSettingDTO::getParmCode, Function.identity()));


        return entities.stream()
                .map(entity -> {
                    ExampleRecordVO vo = new ExampleRecordVO();
                    ENTITY_TO_VO.copy(entity, vo, null);

                    if (Objects.nonNull(entity.getLocation())) {
                        vo.setLocation(GeometryUtils.transferFromGeometry(entity.getLocation()));
                    }
                    if (StrUtil.isNotBlank(entity.getManagerStaffId())) {
                        SimpleStaffDTO simpleStaffDTO = simpleStaffMap.get(entity.getManagerStaffId());
                        if (Objects.nonNull(simpleStaffDTO)) {
                            vo.setManagerStaffName(simpleStaffDTO.getName());
                        }
                    }
                    if (StrUtil.isNotBlank(entity.getDateType())) {
                        vo.setDateTypeName(Objects.requireNonNull(ExampleEnum.getByKey(entity.getDateType())).getValue());
                    }
                    if (StrUtil.isNotBlank(entity.getType())) {
                        ParamSettingDTO paramSettingDTO = paramMap.get(entity.getType());
                        if (Objects.nonNull(paramSettingDTO)) {
                            vo.setTypeName(paramSettingDTO.getParmName());
                        }
                    }

                    return vo;
                })
                .collect(Collectors.toList());
    }

    private ExampleRecordVO transferFromEntity(ExampleRecord entity) {
        if (Objects.isNull(entity)) {
            return null;
        }

        return this.transferFromEntity(Lists.newArrayList(entity)).get(0);
    }

    private ExampleRecord transferToEntity(ExampleRecord entity, ExampleRecordDTO dto) {
        if (Objects.isNull(entity)) {
            entity = new ExampleRecord();
        }
        DTO_TO_ENTITY.copy(dto, entity, null);


        if (Objects.nonNull(dto.getLocation())) {
            entity.setLocation(GeometryUtils.transferToGeometry(dto.getLocation()));
        }

        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResultDTO<?> importExcel(String tenantId, MultipartFile file, Integer startRowNum, Integer startCellNum) throws Exception {

        // 字段校验：必填项、格式、唯一性、是否存在
        List<ExcelImportField> fields = Lists.newArrayList();
        this.buildExcelFields(fields);
        ExcelReader excelReader = this.buildExcelReader(file, startRowNum, startCellNum, fields);
        // 读取excel
        List<ExcelImportRow> excelImportRows = excelReader.readRows();

        if (!excelReader.hasError()) {
            // 保存数据
            this.saveOrUpdateList(tenantId, excelImportRows);
            return RestResultDTO.newSuccess(excelImportRows.size(), "导入成功");
        } else {
            // 写入错误信息
            String errorFileId = excelReader.writeError();
            RestResultDTO<Object> restResultDTO = RestResultDTO.newFail("导入失败");
            restResultDTO.setData(errorFileId);
            return restResultDTO;
        }
    }

    private void saveOrUpdateList(String tenantId, List<ExcelImportRow> excelImportRows) {

        List<ExampleRecord> exampleRecords = Lists.newArrayList();
        for (ExcelImportRow row : excelImportRows) {
            ExampleRecord exampleRecord = new ExampleRecord();
            exampleRecord.setTenantId(tenantId);

            for (ExcelImportCell cell : row.getCells()) {
                switch (cell.getField().getKey()) {
                    case "code":
                        exampleRecord.setCode(Objects.isNull(cell.getTargetValue()) ? null : cell.getTargetValue().toString());
                        break;
                    case "name":
                        exampleRecord.setName(Objects.isNull(cell.getTargetValue()) ? null : cell.getTargetValue().toString());
                        break;
                    case "month":
                        if (cell.getTargetValue() instanceof Date) {
                            exampleRecord.setMonth((Date) cell.getTargetValue());
                        } else if (Objects.nonNull(cell.getTargetValue()) && StrUtil.isNotBlank(cell.getTargetValue().toString())) {
                            exampleRecord.setMonth(DateUtil.parse(cell.getTargetValue().toString(), DatePattern.NORM_MONTH_PATTERN));
                        }
                        break;
                    case "type":
                        exampleRecord.setType(Objects.isNull(cell.getTargetValue()) ? null : cell.getTargetValue().toString());
                        break;
                    case "buildDate":
                        if (Objects.nonNull(cell.getTargetValue()) && StrUtil.isNotBlank(cell.getTargetValue().toString())) {
                            exampleRecord.setBuildDate(LocalDate.parse(cell.getTargetValue().toString()));
                        }
                        break;
                    case "dateType":
                        exampleRecord.setDateType(Objects.isNull(cell.getTargetValue()) ? null : cell.getTargetValue().toString());
                        break;
                    case "status":
                        if (Objects.nonNull(cell.getTargetValue()) && StrUtil.isNotBlank(cell.getTargetValue().toString())) {
                            exampleRecord.setStatus(Integer.valueOf(cell.getTargetValue().toString()));
                        }
                        break;
                    case "hasOffline":
                        if (Objects.nonNull(cell.getTargetValue()) && StrUtil.isNotBlank(cell.getTargetValue().toString())) {
                            exampleRecord.setHasOffline(Boolean.valueOf(cell.getTargetValue().toString()));
                        }
                        break;
                    case "managerStaffId":
                        exampleRecord.setManagerStaffId(Objects.isNull(cell.getTargetValue()) ? null : cell.getTargetValue().toString());
                        break;
                    case "amount":
                        if (Objects.nonNull(cell.getTargetValue()) && StrUtil.isNotBlank(cell.getTargetValue().toString())) {
                            exampleRecord.setAmount(new BigDecimal(cell.getTargetValue().toString()));
                        }
                        break;
                    case "version":
                        if (Objects.nonNull(cell.getTargetValue()) && StrUtil.isNotBlank(cell.getTargetValue().toString())) {
                            exampleRecord.setVersion(Double.valueOf(cell.getTargetValue().toString()));
                        }
                        break;
                    case "files":
                        exampleRecord.setFiles(Objects.isNull(cell.getTargetValue()) ? null : cell.getTargetValue().toString());
                        break;
                    case "location":
                        if (Objects.nonNull(cell.getTargetValue()) && StrUtil.isNotBlank(cell.getTargetValue().toString())) {
                            GeometryDTO geometryDTO = JSONObject.parseObject(cell.getTargetValue().toString(), GeometryDTO.class);
                            exampleRecord.setLocation(GeometryUtils.transferToGeometry(geometryDTO));
                        }
                        break;
                    default:
                        break;
                }
            }
            exampleRecords.add(exampleRecord);
        }

        if(CollectionUtils.isNotEmpty(exampleRecords)) {
            Map<String, List<ExampleRecord>> monthRecordMap = exampleRecords.stream().collect(Collectors.groupingBy(record -> DateUtil.format(record.getMonth(), DatePattern.SIMPLE_MONTH_PATTERN)));
            for (Map.Entry<String, List<ExampleRecord>> entry : monthRecordMap.entrySet()) {
                String month = entry.getKey();
                List<ExampleRecord> monthRecords = entry.getValue();

                List<ExampleRecord> allExampleRecords = this.list(month, Wrappers.lambdaQuery(ExampleRecord.class).eq(ExampleRecord::getTenantId, tenantId).select(ExampleRecord::getId, ExampleRecord::getCode));
                Map<String, ExampleRecord> existExampleRecords = allExampleRecords.stream().collect(Collectors.toMap(ExampleRecord::getCode, Function.identity(), (existing, replacement) -> existing));

                monthRecords.forEach(record -> {
                    if (record.getCode() != null && existExampleRecords.containsKey(record.getCode())) {
                        record.setId(existExampleRecords.get(record.getCode()).getId());
                    }
                });

                this.saveOrUpdateBatch(month, monthRecords);
            }
        }
    }

    private void buildExcelFields(List<ExcelImportField> fields) {
        fields.add(ExcelImportField.builder()
                .key("code")
                .title("编码")
                .required(true)
                .unique(true)
                .build());
        fields.add(ExcelImportField.builder()
                .key("name")
                .title("名称")
                .required(true)
                .build());
        fields.add(ExcelImportField.builder()
                .key("month")
                .title("月份")
                .required(true)
                .convertFunction((messages, source) -> {

                    String monthString;
                    if (source instanceof Date) {
                        monthString = DateUtil.format((Date) source, DatePattern.NORM_MONTH_PATTERN);
                    } else {
                        monthString = source.toString().trim();
                    }

                    try {
                        String normalized = DateUtil.format(
                                DateUtil.parse(monthString, DatePattern.NORM_MONTH_PATTERN),
                                DatePattern.NORM_MONTH_PATTERN
                        );
                        if (!monthString.equals(normalized)) {
                            messages.add("月份格式错误，必须为yyyy-MM");
                            return null;
                        }
                        return DateUtil.parse(normalized, DatePattern.NORM_MONTH_PATTERN);
                    } catch (Exception e) {
                        messages.add("月份格式错误，必须为yyyy-MM");
                        return null;
                    }

                })
                .build());
        fields.add(ExcelImportField.builder()
                .key("type")
                .title("类型")
                .build());
        fields.add(ExcelImportField.builder()
                .key("buildDate")
                .title("建设日期")
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String dateString = source.toString().trim();
                    try {
                        String normalized = DateUtil.format(
                                DateUtil.parse(dateString, DatePattern.NORM_DATE_PATTERN),
                                DatePattern.NORM_DATE_PATTERN
                        );
                        if (!dateString.equals(normalized)) {
                            messages.add("建设日期格式错误，必须为yyyy-MM-dd");
                            return null;
                        }
                        return normalized;
                    } catch (Exception e) {
                        messages.add("建设日期格式错误，必须为yyyy-MM-dd");
                        return null;
                    }
                })
                .build());
        fields.add(ExcelImportField.builder()
                .key("dateType")
                .title("日期类型")
                .build());
        fields.add(ExcelImportField.builder()
                .key("status")
                .title("状态")
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String value = source.toString().trim();
                    try {
                        Integer.valueOf(value);
                        return value;
                    } catch (Exception e) {
                        messages.add("状态格式错误，必须为整数");
                        return null;
                    }
                })
                .build());
        fields.add(ExcelImportField.builder()
                .key("hasOffline")
                .title("是否离线")
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String value = source.toString().trim().toLowerCase(Locale.ROOT);
                    if ("true".equals(value) || "false".equals(value)) {
                        return value;
                    }
                    messages.add("是否离线格式错误，必须为true或false");
                    return null;
                })
                .build());
        fields.add(ExcelImportField.builder()
                .key("managerStaffId")
                .title("管理人员ID")
                .build());
        fields.add(ExcelImportField.builder()
                .key("amount")
                .title("金额")
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String value = source.toString().trim();
                    try {
                        new BigDecimal(value);
                        return value;
                    } catch (Exception e) {
                        messages.add("金额格式错误，必须为数字");
                        return null;
                    }
                })
                .build());
        fields.add(ExcelImportField.builder()
                .key("version")
                .title("版本")
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String value = source.toString().trim();
                    try {
                        Double.valueOf(value);
                        return value;
                    } catch (Exception e) {
                        messages.add("版本格式错误，必须为数字");
                        return null;
                    }
                })
                .build());
        fields.add(ExcelImportField.builder()
                .key("files")
                .title("附件，json数组")
                .build());
        fields.add(ExcelImportField.builder()
                .key("location")
                .title("地理信息，GeometryDTO的JSON")
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String value = source.toString().trim();
                    try {
                        JSONObject.parseObject(value, GeometryDTO.class);
                        return value;
                    } catch (Exception e) {
                        messages.add("地理信息格式错误，必须为GeometryDTO的JSON");
                        return null;
                    }
                })
                .build());
    }

    private ExcelReader buildExcelReader(MultipartFile file, Integer startRowNum, Integer startCellNum, List<ExcelImportField> fields) throws Exception {
        return ExcelReader.builder()
                .inputStream(file.getInputStream())
                .fields(fields)
                .startRowNum(startRowNum)
                .startColNum(startCellNum)
                .rowValidateFunction(((readRows, currentRow) -> {

                })).build();
    }
}
