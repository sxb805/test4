package com.vortex.cloud.test.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.vortex.cloud.sdk.api.dto.ums.ParamSettingDTO;
import com.vortex.cloud.sdk.api.dto.ums.SimpleStaffDTO;
import com.vortex.cloud.sdk.api.service.IUmsService;
import com.vortex.cloud.test.domain.Example;
import com.vortex.cloud.test.dto.ExampleDTO;
import com.vortex.cloud.test.dto.ExampleQueryDTO;
import com.vortex.cloud.test.dto.ExampleVO;
import com.vortex.cloud.test.enums.ExampleEnum;
import com.vortex.cloud.test.mapper.ExampleMapper;
import com.vortex.cloud.test.service.ExampleService;
import com.vortex.cloud.test.support.Constants;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhanglei
 */
@Transactional(readOnly = true)
@Service
public class ExampleServiceImpl extends ServiceImpl<ExampleMapper, Example> implements ExampleService {

    private static final BeanCopier ENTITY_TO_VO = BeanCopier.create(Example.class, ExampleVO.class, false);
    private static final BeanCopier DTO_TO_ENTITY = BeanCopier.create(ExampleDTO.class, Example.class, false);

    @Resource
    private IUmsService umsService;

    @Override
    public DataStoreDTO<ExampleVO> page(Pageable pageable, ExampleQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");

        QueryWrapper<Example> queryWrapper = this.buildQuery(queryDTO);
        Page<Example> page = PageUtils.transferPage(pageable);
        Page<Example> result = this.page(page, queryWrapper);
        return new DataStoreDTO<>(result.getTotal(), this.transferFromEntity(result.getRecords()));
    }

    @Override
    public List<ExampleVO> list(Sort sort, ExampleQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");

        QueryWrapper<Example> queryWrapper = this.buildQuery(queryDTO);
        PageUtils.transferSort(queryWrapper, sort);
        List<Example> records = this.list(queryWrapper);
        return this.transferFromEntity(records);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(ExampleDTO dto) {
        checkData(dto);
        // 唯一性字段校验
        Assert.isTrue(!this.exist(dto.getTenantId(), dto.getId(), "code", dto.getCode()), "编码为" + dto.getCode() + "的记录已存在");
        this.save(this.transferToEntity(null, dto));
    }

    private void checkData(ExampleDTO dto) {
        Assert.hasText(dto.getTenantId(), "租户ID不能为空");
        Assert.hasText(dto.getCode(), "编码不能为空");
        Assert.hasText(dto.getName(), "名称不能为空");

        if (StrUtil.isNotBlank(dto.getDateType())) {
            ExampleEnum anEnum = ExampleEnum.getByKey(dto.getDateType());
            Assert.notNull(anEnum, "日期类型：" + dto.getDateType() + "不存在");
        }

        if (StrUtil.isNotBlank(dto.getManagerStaffId())) {
            List<SimpleStaffDTO> simpleStaffList = Optional.ofNullable(this.umsService.loadSimpleStaffs(dto.getTenantId()))
                    .orElse(Lists.newArrayList());
            Set<String> validStaffIds = simpleStaffList.stream()
                    .map(SimpleStaffDTO::getId)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toSet());
            Assert.isTrue(validStaffIds.contains(dto.getManagerStaffId()), "管理人员不存在");
        }
        if (StrUtil.isNotBlank(dto.getType())) {
            List<ParamSettingDTO> byParamTypeCode = Optional.ofNullable(
                    this.umsService.getByParamTypeCode(dto.getTenantId(), Constants.PARAM_TYPE_EXAMPLE_TYPE))
                    .orElse(Lists.newArrayList());
            Map<String, ParamSettingDTO> paramMap = byParamTypeCode.stream().collect(Collectors.toMap(ParamSettingDTO::getParmCode, Function.identity()));
            Assert.notNull(paramMap.get(dto.getType()), "类型编码：" + dto.getType() + "不存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(ExampleDTO dto) {
        Assert.hasText(dto.getId(), "ID不能为空");
        checkData(dto);
        // 唯一性字段校验
        Assert.isTrue(!this.exist(dto.getTenantId(), dto.getId(), "code", dto.getCode()), "编码为" + dto.getCode() + "的记录已存在");
        Example entity = this.getById(dto.getId());
        Assert.notNull(entity, "找不到id为" + dto.getId() + " 的记录");
        this.updateById(this.transferToEntity(entity, dto));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        this.removeByIds(ids);
    }

    @Override
    public ExampleVO get(String id) {
        Assert.hasText(id, "ID不能为空");
        Example entity = this.getById(id);
        Assert.notNull(entity, "找不到ID为" + id + " 的记录");
        return this.transferFromEntity(entity);
    }

    @Override
    public Boolean exist(String tenantId, String id, String key, String value) {
        Assert.hasText(tenantId, "租户ID不能为空");
        Assert.hasText(key, "字段名不能为空");

        QueryWrapper<Example> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Example::getTenantId, tenantId);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.lambda().ne(Example::getId, id);
        }
        queryWrapper.eq(StrUtil.toUnderlineCase(key), value);

        return this.count(queryWrapper) >= 1;
    }

    private QueryWrapper<Example> buildQuery(ExampleQueryDTO queryDTO) {
        QueryWrapper<Example> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getTenantId()), Example::getTenantId, queryDTO.getTenantId());
        queryWrapper.lambda().like(StringUtils.isNotBlank(queryDTO.getName()), Example::getName, queryDTO.getName());
        queryWrapper.lambda().eq(Objects.nonNull(queryDTO.getBuildDate()), Example::getBuildDate, queryDTO.getBuildDate());
        queryWrapper.lambda().in(CollUtil.isNotEmpty(queryDTO.getIds()), Example::getId, queryDTO.getIds());
        return queryWrapper;
    }

    private List<ExampleVO> transferFromEntity(List<Example> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return Lists.newArrayList();
        }

        String tenantId = entities.get(0).getTenantId();

        List<SimpleStaffDTO> simpleStaffDTOS = Optional.ofNullable(umsService.loadSimpleStaffs(tenantId))
                .orElse(Lists.newArrayList());
        Map<String, SimpleStaffDTO> simpleStaffMap = simpleStaffDTOS.stream().collect(Collectors.toMap(SimpleStaffDTO::getId, Function.identity()));

        List<ParamSettingDTO> byParamTypeCode = Optional.ofNullable(
                this.umsService.getByParamTypeCode(tenantId, Constants.PARAM_TYPE_EXAMPLE_TYPE))
                .orElse(Lists.newArrayList());
        Map<String, ParamSettingDTO> paramMap = byParamTypeCode.stream().collect(Collectors.toMap(ParamSettingDTO::getParmCode, Function.identity()));


        return entities.stream()
                .map(entity -> {
                    ExampleVO vo = new ExampleVO();
                    ENTITY_TO_VO.copy(entity, vo, null);

                    if (Objects.nonNull(entity.getLocation())) {
                        vo.setLocation(GeometryUtils.transferFromGeometry(entity.getLocation()));
                    }
                    if (StrUtil.isNotBlank(entity.getManagerStaffId())) {
                        SimpleStaffDTO staffDTO = simpleStaffMap.get(entity.getManagerStaffId());
                        if (Objects.nonNull(staffDTO)) {
                            vo.setManagerStaffName(staffDTO.getName());
                        }
                    }
                    if (StrUtil.isNotBlank(entity.getDateType())) {
                        ExampleEnum dateType = ExampleEnum.getByKey(entity.getDateType());
                        if (Objects.nonNull(dateType)) {
                            vo.setDateTypeName(dateType.getValue());
                        }
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

    private ExampleVO transferFromEntity(Example entity) {
        if (Objects.isNull(entity)) {
            return null;
        }

        return this.transferFromEntity(Lists.newArrayList(entity)).get(0);
    }

    private Example transferToEntity(Example entity, ExampleDTO dto) {
        if (Objects.isNull(entity)) {
            entity = new Example();
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
        List<Example> allExamples = super.list(Wrappers.lambdaQuery(Example.class)
                .eq(Example::getTenantId, tenantId)
                .select(Example::getId, Example::getCode));
        Map<String, Example> existExamples = allExamples.stream()
                .collect(Collectors.toMap(Example::getCode, Function.identity(), (a, b) -> a));
        List<ParamSettingDTO> byParamTypeCode = Optional.ofNullable(
                this.umsService.getByParamTypeCode(tenantId, Constants.PARAM_TYPE_EXAMPLE_TYPE))
                .orElse(Lists.newArrayList());
        Set<String> validTypeCodes = byParamTypeCode.stream()
                .map(ParamSettingDTO::getParmCode)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        Set<String> validDateTypes = Arrays.stream(ExampleEnum.values())
                .map(ExampleEnum::getKey)
                .collect(Collectors.toSet());
        List<SimpleStaffDTO> simpleStaffDTOS = Optional.ofNullable(umsService.loadSimpleStaffs(tenantId))
                .orElse(Lists.newArrayList());
        Map<String, String> staffNameToIdMap = simpleStaffDTOS.stream()
                .filter(staff -> StrUtil.isNotBlank(staff.getName()) && StrUtil.isNotBlank(staff.getId()))
                .collect(Collectors.toMap(SimpleStaffDTO::getName, SimpleStaffDTO::getId, (a, b) -> a));
        Set<String> validStaffNames = staffNameToIdMap.keySet();

        // 字段校验：必填项、格式、唯一性、是否存在
        List<ExcelImportField> fields = Lists.newArrayList();
        this.buildExcelFields(fields, existExamples.keySet(), validTypeCodes, validDateTypes, validStaffNames);
        ExcelReader excelReader = this.buildExcelReader(file, startRowNum, startCellNum, fields);
        // 读取excel
        List<ExcelImportRow> excelImportRows = excelReader.readRows();

        if (!excelReader.hasError()) {
            // 保存数据
            this.saveOrUpdateList(tenantId, excelImportRows, existExamples, staffNameToIdMap);
            return RestResultDTO.newSuccess(excelImportRows.size(), "导入成功");
        } else {
            // 写入错误信息
            String errorFileId = excelReader.writeError();
            RestResultDTO<Object> restResultDTO = RestResultDTO.newFail("导入失败");
            restResultDTO.setData(errorFileId);
            return restResultDTO;
        }
    }

    private void saveOrUpdateList(String tenantId, List<ExcelImportRow> excelImportRows, Map<String, Example> existExamples, Map<String, String> staffNameToIdMap) {
        List<Example> examples = Lists.newArrayList();
        for (ExcelImportRow row : excelImportRows) {
            Example example = new Example();
            example.setTenantId(tenantId);

            for (ExcelImportCell cell : row.getCells()) {
                switch (cell.getField().getKey()) {
                    case "code":
                        example.setCode((String) cell.getTargetValue());
                        break;
                    case "name":
                        example.setName((String) cell.getTargetValue());
                        break;
                    case "type":
                        example.setType((String) cell.getTargetValue());
                        break;
                    case "buildDate":
                        if (Objects.nonNull(cell.getTargetValue())) {
                            example.setBuildDate(LocalDate.parse(cell.getTargetValue().toString(), DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
                        }
                        break;
                    case "buildTime":
                        if (Objects.nonNull(cell.getTargetValue())) {
                            example.setBuildTime(LocalDateTime.parse(cell.getTargetValue().toString(), DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
                        }
                        break;
                    case "dateType":
                        example.setDateType((String) cell.getTargetValue());
                        break;
                    case "hasOffline":
                        if (Objects.nonNull(cell.getTargetValue())) {
                            example.setHasOffline(Boolean.valueOf(cell.getTargetValue().toString()));
                        }
                        break;
                    case "managerStaffName":
                        if (Objects.nonNull(cell.getTargetValue())) {
                            example.setManagerStaffId(staffNameToIdMap.get(cell.getTargetValue().toString()));
                        }
                        break;
                    case "amount":
                        if (Objects.nonNull(cell.getTargetValue())) {
                            example.setAmount(new BigDecimal(cell.getTargetValue().toString()));
                        }
                        break;
                    case "version":
                        if (Objects.nonNull(cell.getTargetValue())) {
                            example.setVersion(Double.valueOf(cell.getTargetValue().toString()));
                        }
                        break;
                    case "files":
                        example.setFiles((String) cell.getTargetValue());
                        break;
                    case "location":
                        if (Objects.nonNull(cell.getTargetValue())) {
                            GeometryDTO geometryDTO = JSONObject.parseObject(cell.getTargetValue().toString(), GeometryDTO.class);
                            example.setLocation(GeometryUtils.transferToGeometry(geometryDTO));
                        }
                        break;
                    default:
                        break;
                }
            }

            Example existExample = existExamples.get(example.getCode());
            if (Objects.nonNull(existExample)) {
                // 更新
                example.setId(existExample.getId());
            }
            examples.add(example);
        }
        if (CollectionUtils.isNotEmpty(examples)) {
            this.saveOrUpdateBatch(examples);
        }
    }

    private void buildExcelFields(List<ExcelImportField> fields,
                                  Set<String> existCodes,
                                  Set<String> validTypeCodes,
                                  Set<String> validDateTypes,
                                  Set<String> validStaffNames) {
        fields.add(ExcelImportField.builder()
                .key("code")
                .title("编码")
                .required(true)
                .unique(true)
                .uniqueSet(existCodes)
                .build());
        fields.add(ExcelImportField.builder()
                .key("name")
                .title("名称")
                .required(true)
                .build());
        fields.add(ExcelImportField.builder()
                .key("type")
                .title("类型")
                .dictSet(validTypeCodes)
                .build());
        fields.add(ExcelImportField.builder()
                .key("buildDate")
                .title("建设日期")
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String dateString = source instanceof Date
                            ? DateUtil.format((Date) source, DatePattern.NORM_DATE_PATTERN)
                            : source.toString().trim();
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
                .key("buildTime")
                .title("建设时间")
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String timeString = source instanceof Date
                            ? DateUtil.format((Date) source, DatePattern.NORM_DATETIME_PATTERN)
                            : source.toString().trim();
                    try {
                        String normalized = DateUtil.format(
                                DateUtil.parse(timeString, DatePattern.NORM_DATETIME_PATTERN),
                                DatePattern.NORM_DATETIME_PATTERN
                        );
                        if (!timeString.equals(normalized)) {
                            messages.add("建设时间格式错误，必须为yyyy-MM-dd HH:mm:ss");
                            return null;
                        }
                        return normalized;
                    } catch (Exception e) {
                        messages.add("建设时间格式错误，必须为yyyy-MM-dd HH:mm:ss");
                        return null;
                    }
                })
                .build());
        fields.add(ExcelImportField.builder()
                .key("dateType")
                .title("日期类型")
                .dictSet(validDateTypes)
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
                .key("managerStaffName")
                .title("管理人员名称")
                .dictSet(validStaffNames)
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
