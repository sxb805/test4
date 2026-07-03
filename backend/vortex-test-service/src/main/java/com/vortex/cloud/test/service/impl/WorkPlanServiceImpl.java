package com.vortex.cloud.test.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.vortex.cloud.test.domain.Project;
import com.vortex.cloud.test.domain.WorkPlan;
import com.vortex.cloud.test.dto.WorkPlanDTO;
import com.vortex.cloud.test.dto.WorkPlanQueryDTO;
import com.vortex.cloud.test.dto.WorkPlanVO;
import com.vortex.cloud.test.mapper.ProjectMapper;
import com.vortex.cloud.test.mapper.WorkPlanMapper;
import com.vortex.cloud.test.service.WorkPlanService;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import com.vortex.cloud.vfs.lite.base.excel.ExcelImportCell;
import com.vortex.cloud.vfs.lite.base.excel.ExcelImportField;
import com.vortex.cloud.vfs.lite.base.excel.ExcelImportRow;
import com.vortex.cloud.vfs.lite.base.excel.ExcelReader;
import com.vortex.cloud.vfs.lite.data.util.PageUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工作计划服务实现
 */
@Transactional(readOnly = true)
@Service
public class WorkPlanServiceImpl extends ServiceImpl<WorkPlanMapper, WorkPlan> implements WorkPlanService {

    private static final BeanCopier ENTITY_TO_VO = BeanCopier.create(WorkPlan.class, WorkPlanVO.class, false);
    private static final BeanCopier DTO_TO_ENTITY = BeanCopier.create(WorkPlanDTO.class, WorkPlan.class, false);
    private static final int MIN_YEAR = 1900;
    private static final int MAX_YEAR = 2100;
    private static final int PERSON_TIMES_SCALE = 4;
    private static final BigDecimal EXCEL_NUMBER_EPSILON = new BigDecimal("0.000000001");

    @Resource
    private ProjectMapper projectMapper;

    @Override
    public DataStoreDTO<WorkPlanVO> page(Pageable pageable, WorkPlanQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        QueryWrapper<WorkPlan> queryWrapper = this.buildQuery(queryDTO);
        Page<WorkPlan> page = PageUtils.transferPage(pageable);
        Page<WorkPlan> result = this.page(page, queryWrapper);
        return new DataStoreDTO<>(result.getTotal(), this.transferFromEntity(result.getRecords()));
    }

    @Override
    public List<WorkPlanVO> list(Sort sort, WorkPlanQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        QueryWrapper<WorkPlan> queryWrapper = this.buildQuery(queryDTO);
        PageUtils.transferSort(queryWrapper, sort);
        return this.transferFromEntity(this.list(queryWrapper));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(WorkPlanDTO dto) {
        this.checkDataAndFillProject(dto);
        Assert.isTrue(!this.existsByProjectAndYear(dto.getTenantId(), null, dto.getProjectId(), dto.getYear()),
                "该项目年份的工作计划已存在");
        this.save(this.transferToEntity(null, dto));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(WorkPlanDTO dto) {
        Assert.hasText(dto.getId(), "ID不能为空");
        this.checkDataAndFillProject(dto);
        WorkPlan entity = this.getById(dto.getId());
        Assert.notNull(entity, "找不到id为" + dto.getId() + "的记录");
        Assert.isTrue(!this.existsByProjectAndYear(dto.getTenantId(), dto.getId(), dto.getProjectId(), dto.getYear()),
                "该项目年份的工作计划已存在");
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
    public WorkPlanVO get(String id) {
        Assert.hasText(id, "ID不能为空");
        WorkPlan entity = this.getById(id);
        Assert.notNull(entity, "找不到ID为" + id + "的记录");
        return this.transferFromEntity(entity);
    }

    private QueryWrapper<WorkPlan> buildQuery(WorkPlanQueryDTO queryDTO) {
        QueryWrapper<WorkPlan> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getTenantId()), WorkPlan::getTenantId, queryDTO.getTenantId());
        queryWrapper.lambda().in(CollUtil.isNotEmpty(queryDTO.getIds()), WorkPlan::getId, queryDTO.getIds());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getProjectId()), WorkPlan::getProjectId, queryDTO.getProjectId());
        queryWrapper.lambda().like(StrUtil.isNotBlank(queryDTO.getProjectNo()), WorkPlan::getProjectNo, queryDTO.getProjectNo());
        queryWrapper.lambda().like(StrUtil.isNotBlank(queryDTO.getProjectName()), WorkPlan::getProjectName, queryDTO.getProjectName());
        queryWrapper.lambda().eq(Objects.nonNull(queryDTO.getYear()), WorkPlan::getYear, queryDTO.getYear());
        return queryWrapper;
    }

    private void checkDataAndFillProject(WorkPlanDTO dto) {
        Assert.hasText(dto.getTenantId(), "租户ID不能为空");
        Assert.hasText(dto.getProjectId(), "项目ID不能为空");
        Assert.notNull(dto.getYear(), "年份不能为空");
        Assert.isTrue(dto.getYear() >= MIN_YEAR && dto.getYear() <= MAX_YEAR, "年份必须在1900到2100之间");
        dto.setFirstQuarterPersonTimes(this.normalizePersonTimes(dto.getFirstQuarterPersonTimes(), "一季度（人/次）"));
        dto.setSecondQuarterPersonTimes(this.normalizePersonTimes(dto.getSecondQuarterPersonTimes(), "二季度（人/次）"));
        dto.setThirdQuarterPersonTimes(this.normalizePersonTimes(dto.getThirdQuarterPersonTimes(), "三季度（人/次）"));
        dto.setFourthQuarterPersonTimes(this.normalizePersonTimes(dto.getFourthQuarterPersonTimes(), "四季度（人/次）"));

        Project project = projectMapper.selectOne(Wrappers.lambdaQuery(Project.class)
                .eq(Project::getTenantId, dto.getTenantId())
                .eq(Project::getId, dto.getProjectId())
                .last("limit 1"));
        Assert.notNull(project, "项目不存在");
        dto.setProjectNo(project.getCode());
        dto.setProjectName(project.getName());
    }

    private BigDecimal normalizePersonTimes(BigDecimal value, String field) {
        if (Objects.isNull(value)) {
            return null;
        }
        Assert.isTrue(value.compareTo(BigDecimal.ZERO) >= 0, field + "必须大于等于0");
        Assert.isTrue(value.stripTrailingZeros().scale() <= PERSON_TIMES_SCALE, field + "最多保留4位小数");
        return value.setScale(PERSON_TIMES_SCALE, RoundingMode.UNNECESSARY);
    }

    private Boolean existsByProjectAndYear(String tenantId, String id, String projectId, Integer year) {
        QueryWrapper<WorkPlan> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WorkPlan::getTenantId, tenantId);
        queryWrapper.lambda().eq(WorkPlan::getProjectId, projectId);
        queryWrapper.lambda().eq(WorkPlan::getYear, year);
        queryWrapper.lambda().ne(StrUtil.isNotBlank(id), WorkPlan::getId, id);
        return this.count(queryWrapper) >= 1;
    }

    private List<WorkPlanVO> transferFromEntity(List<WorkPlan> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return Lists.newArrayList();
        }
        return entities.stream().map(this::transferFromEntity).collect(Collectors.toList());
    }

    private WorkPlanVO transferFromEntity(WorkPlan entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        WorkPlanVO vo = new WorkPlanVO();
        ENTITY_TO_VO.copy(entity, vo, null);
        return vo;
    }

    private WorkPlan transferToEntity(WorkPlan entity, WorkPlanDTO dto) {
        if (Objects.isNull(entity)) {
            entity = new WorkPlan();
        }
        DTO_TO_ENTITY.copy(dto, entity, null);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResultDTO<?> importExcel(String tenantId, MultipartFile file, Integer startRowNum, Integer startCellNum) throws Exception {
        List<Project> allProjects = Optional.ofNullable(projectMapper.selectList(Wrappers.lambdaQuery(Project.class)
                .eq(Project::getTenantId, tenantId)
                .select(Project::getId, Project::getCode, Project::getName)))
                .orElse(Lists.newArrayList());
        Map<String, Project> projectNameMap = allProjects.stream()
                .filter(project -> StrUtil.isNotBlank(project.getName()))
                .collect(Collectors.toMap(Project::getName, Function.identity(), (a, b) -> a));

        List<WorkPlan> allWorkPlans = super.list(Wrappers.lambdaQuery(WorkPlan.class)
                .eq(WorkPlan::getTenantId, tenantId)
                .select(WorkPlan::getId, WorkPlan::getProjectId, WorkPlan::getYear));
        Map<String, WorkPlan> existMap = allWorkPlans.stream()
                .collect(Collectors.toMap(item -> buildUniqueKey(item.getProjectId(), item.getYear()), Function.identity(), (a, b) -> a));

        List<ExcelImportField> fields = Lists.newArrayList();
        this.buildExcelFields(fields, projectNameMap.keySet());
        ExcelReader excelReader = this.buildExcelReader(file, startRowNum, startCellNum, fields);
        List<ExcelImportRow> rows = excelReader.readRows();

        if (!excelReader.hasError()) {
            this.saveOrUpdateList(tenantId, rows, projectNameMap, existMap);
            return RestResultDTO.newSuccess(rows.size(), "导入成功");
        }

        String errorFileId = excelReader.writeError();
        RestResultDTO<Object> fail = RestResultDTO.newFail("导入失败");
        fail.setData(errorFileId);
        return fail;
    }

    private void saveOrUpdateList(String tenantId,
                                  List<ExcelImportRow> rows,
                                  Map<String, Project> projectNameMap,
                                  Map<String, WorkPlan> existMap) {
        List<WorkPlan> entities = Lists.newArrayList();
        for (ExcelImportRow row : rows) {
            WorkPlan entity = new WorkPlan();
            entity.setTenantId(tenantId);

            for (ExcelImportCell cell : row.getCells()) {
                String key = cell.getField().getKey();
                Object value = cell.getTargetValue();
                switch (key) {
                    case "projectName":
                        if (Objects.nonNull(value)) {
                            Project project = projectNameMap.get(value.toString());
                            if (Objects.nonNull(project)) {
                                entity.setProjectId(project.getId());
                                entity.setProjectNo(project.getCode());
                                entity.setProjectName(project.getName());
                            }
                        }
                        break;
                    case "year":
                        if (Objects.nonNull(value)) {
                            entity.setYear(Integer.valueOf(value.toString()));
                        }
                        break;
                    case "firstQuarterPersonTimes":
                        entity.setFirstQuarterPersonTimes(toBigDecimal(value));
                        break;
                    case "secondQuarterPersonTimes":
                        entity.setSecondQuarterPersonTimes(toBigDecimal(value));
                        break;
                    case "thirdQuarterPersonTimes":
                        entity.setThirdQuarterPersonTimes(toBigDecimal(value));
                        break;
                    case "fourthQuarterPersonTimes":
                        entity.setFourthQuarterPersonTimes(toBigDecimal(value));
                        break;
                    default:
                        break;
                }
            }

            WorkPlan exist = existMap.get(buildUniqueKey(entity.getProjectId(), entity.getYear()));
            if (Objects.nonNull(exist)) {
                entity.setId(exist.getId());
            }
            entities.add(entity);
        }

        if (CollectionUtils.isNotEmpty(entities)) {
            this.saveOrUpdateBatch(entities);
        }
    }

    private void buildExcelFields(List<ExcelImportField> fields, Set<String> validProjectNames) {
        fields.add(ExcelImportField.builder().key("projectName").title("项目名称").required(true).dictSet(validProjectNames).build());
        fields.add(ExcelImportField.builder().key("year").title("年份").required(true)
                .convertFunction((messages, source) -> normalizeYear(source, messages)).build());
        fields.add(ExcelImportField.builder().key("firstQuarterPersonTimes").title("一季度（人/次）")
                .convertFunction((messages, source) -> normalizePersonTimes(source, "一季度（人/次）", messages)).build());
        fields.add(ExcelImportField.builder().key("secondQuarterPersonTimes").title("二季度（人/次）")
                .convertFunction((messages, source) -> normalizePersonTimes(source, "二季度（人/次）", messages)).build());
        fields.add(ExcelImportField.builder().key("thirdQuarterPersonTimes").title("三季度（人/次）")
                .convertFunction((messages, source) -> normalizePersonTimes(source, "三季度（人/次）", messages)).build());
        fields.add(ExcelImportField.builder().key("fourthQuarterPersonTimes").title("四季度（人/次）")
                .convertFunction((messages, source) -> normalizePersonTimes(source, "四季度（人/次）", messages)).build());
    }

    private Object normalizeYear(Object source, List<String> messages) {
        if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
            return null;
        }
        String value = source.toString().trim();
        if (value.matches("^\\d+\\.0+$")) {
            value = value.substring(0, value.indexOf('.'));
        }
        try {
            Integer year = Integer.valueOf(value);
            if (year < MIN_YEAR || year > MAX_YEAR) {
                messages.add("年份必须在1900到2100之间");
                return null;
            }
            return year.toString();
        } catch (Exception e) {
            messages.add("年份必须为整数");
            return null;
        }
    }

    private Object normalizePersonTimes(Object source, String field, List<String> messages) {
        if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
            return null;
        }
        try {
            BigDecimal value = parseImportPersonTimes(source);
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                messages.add(field + "必须大于等于0");
                return null;
            }
            BigDecimal normalized = normalizeImportPersonTimes(value);
            if (Objects.isNull(normalized)) {
                messages.add(field + "最多保留4位小数");
                return null;
            }
            return normalized.toPlainString();
        } catch (Exception e) {
            messages.add(field + "必须为数字且最多保留4位小数");
            return null;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (Objects.isNull(value) || StrUtil.isBlank(value.toString())) {
            return null;
        }
        BigDecimal normalized = normalizeImportPersonTimes(parseImportPersonTimes(value));
        Assert.notNull(normalized, "人次最多保留4位小数");
        return normalized;
    }

    private BigDecimal parseImportPersonTimes(Object source) {
        if (source instanceof BigDecimal value) {
            return value;
        }
        if (source instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(source.toString().trim());
    }

    private BigDecimal normalizeImportPersonTimes(BigDecimal value) {
        if (value.stripTrailingZeros().scale() <= PERSON_TIMES_SCALE) {
            return value.setScale(PERSON_TIMES_SCALE, RoundingMode.UNNECESSARY);
        }
        BigDecimal rounded = value.setScale(PERSON_TIMES_SCALE, RoundingMode.HALF_UP);
        // Excel 数值单元格可能带二进制浮点尾差，例如 84.65 被读成 84.65000000000001；这种误差不应当按第 5 位小数拦截。
        if (value.subtract(rounded).abs().compareTo(EXCEL_NUMBER_EPSILON) <= 0) {
            return rounded;
        }
        return null;
    }

    private ExcelReader buildExcelReader(MultipartFile file, Integer startRowNum, Integer startCellNum, List<ExcelImportField> fields) throws Exception {
        return ExcelReader.builder()
                .inputStream(file.getInputStream())
                .fields(fields)
                .startRowNum(startRowNum)
                .startColNum(startCellNum)
                .rowValidateFunction((readRows, currentRow) -> {
                })
                .build();
    }

    private String buildUniqueKey(String projectId, Integer year) {
        return StrUtil.blankToDefault(projectId, "") + "|" + Optional.ofNullable(year).map(String::valueOf).orElse("");
    }
}
