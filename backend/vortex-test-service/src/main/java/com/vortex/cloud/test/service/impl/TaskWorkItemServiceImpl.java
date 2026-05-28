package com.vortex.cloud.test.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.vortex.cloud.sdk.api.dto.ums.SimpleStaffDTO;
import com.vortex.cloud.sdk.api.service.IUmsService;
import com.vortex.cloud.test.domain.Project;
import com.vortex.cloud.test.domain.TaskWorkItem;
import com.vortex.cloud.test.dto.TaskWorkItemDTO;
import com.vortex.cloud.test.dto.TaskWorkItemQueryDTO;
import com.vortex.cloud.test.dto.TaskWorkItemVO;
import com.vortex.cloud.test.dto.TaskWorkItemWeeklyOccupancyColumnVO;
import com.vortex.cloud.test.dto.TaskWorkItemWeeklyOccupancyTableRowVO;
import com.vortex.cloud.test.dto.TaskWorkItemWeeklyOccupancyVO;
import com.vortex.cloud.test.mapper.ProjectMapper;
import com.vortex.cloud.test.mapper.TaskWorkItemMapper;
import com.vortex.cloud.test.service.TaskWorkItemService;
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

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务工单服务实现
 */
@Transactional(readOnly = true)
@Service
public class TaskWorkItemServiceImpl extends ServiceImpl<TaskWorkItemMapper, TaskWorkItem> implements TaskWorkItemService {

    private static final BeanCopier ENTITY_TO_VO = BeanCopier.create(TaskWorkItem.class, TaskWorkItemVO.class, false);
    private static final BeanCopier DTO_TO_ENTITY = BeanCopier.create(TaskWorkItemDTO.class, TaskWorkItem.class, false);

    @Resource
    private IUmsService umsService;

    @Resource
    private ProjectMapper projectMapper;

    @Override
    public DataStoreDTO<TaskWorkItemVO> page(Pageable pageable, TaskWorkItemQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        this.fillDefaultDateRange(queryDTO);
        QueryWrapper<TaskWorkItem> queryWrapper = this.buildQuery(queryDTO);
        Page<TaskWorkItem> page = PageUtils.transferPage(pageable);
        Page<TaskWorkItem> result = this.page(page, queryWrapper);
        return new DataStoreDTO<>(result.getTotal(), this.transferFromEntity(result.getRecords()));
    }

    @Override
    public List<TaskWorkItemVO> list(Sort sort, TaskWorkItemQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        this.fillDefaultDateRange(queryDTO);
        QueryWrapper<TaskWorkItem> queryWrapper = this.buildQuery(queryDTO);
        PageUtils.transferSort(queryWrapper, sort);
        return this.transferFromEntity(this.list(queryWrapper));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(TaskWorkItemDTO dto) {
        this.checkDataAndFillAssociation(dto);
        this.save(this.transferToEntity(null, dto));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(TaskWorkItemDTO dto) {
        Assert.hasText(dto.getId(), "ID不能为空");
        this.checkDataAndFillAssociation(dto);
        TaskWorkItem entity = this.getById(dto.getId());
        Assert.notNull(entity, "找不到id为" + dto.getId() + "的记录");
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
    public TaskWorkItemVO get(String id) {
        Assert.hasText(id, "ID不能为空");
        TaskWorkItem entity = this.getById(id);
        Assert.notNull(entity, "找不到ID为" + id + "的记录");
        return this.transferFromEntity(entity);
    }

    @Override
    public TaskWorkItemWeeklyOccupancyVO weeklyOccupancy(TaskWorkItemQueryDTO queryDTO) {
        return this.buildWeeklyOccupancy(queryDTO, OccupancyDimension.OWNER);
    }

    @Override
    public TaskWorkItemWeeklyOccupancyVO projectWeeklyOccupancy(TaskWorkItemQueryDTO queryDTO) {
        return this.buildWeeklyOccupancy(queryDTO, OccupancyDimension.PROJECT);
    }

    private TaskWorkItemWeeklyOccupancyVO buildWeeklyOccupancy(TaskWorkItemQueryDTO queryDTO, OccupancyDimension dimension) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        this.fillDefaultDateRange(queryDTO);
        Assert.notNull(queryDTO.getStartDateBegin(), "开始日期-起不能为空");
        Assert.notNull(queryDTO.getStartDateEnd(), "开始日期-止不能为空");

        QueryWrapper<TaskWorkItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TaskWorkItem::getTenantId, queryDTO.getTenantId());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getProjectId()), TaskWorkItem::getProjectId, queryDTO.getProjectId());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getOwnerTlId()), TaskWorkItem::getOwnerTlId, queryDTO.getOwnerTlId());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getStatus()), TaskWorkItem::getStatus, queryDTO.getStatus());
        queryWrapper.lambda().ge(TaskWorkItem::getEndDate, queryDTO.getStartDateBegin());
        queryWrapper.lambda().le(TaskWorkItem::getEndDate, queryDTO.getStartDateEnd());

        List<TaskWorkItem> items = this.list(queryWrapper);
        List<WeekMeta> weeks = buildWeekMetas(queryDTO.getStartDateBegin(), queryDTO.getStartDateEnd());
        Map<String, WeekMeta> weekMetaMap = weeks.stream().collect(Collectors.toMap(WeekMeta::getRangeKey, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        Map<String, TaskWorkItemWeeklyOccupancyTableRowVO> rowMap = new LinkedHashMap<>();
        for (TaskWorkItem item : items) {
            if (Objects.isNull(item.getEndDate()) || StrUtil.isBlank(item.getOwnerId())) {
                continue;
            }
            String weekKey = toWeekKey(item.getEndDate());
            WeekMeta meta = weekMetaMap.get(weekKey);
            if (Objects.isNull(meta)) {
                continue;
            }
            String rowId = this.getOccupancyRowId(item, dimension);
            if (StrUtil.isBlank(rowId)) {
                continue;
            }
            String rowName = this.getOccupancyRowName(item, dimension, rowId);
            TaskWorkItemWeeklyOccupancyTableRowVO row = rowMap.computeIfAbsent(rowId, k -> {
                TaskWorkItemWeeklyOccupancyTableRowVO vo = new TaskWorkItemWeeklyOccupancyTableRowVO();
                vo.setKey(rowId);
                vo.setName(rowName);
                vo.setTotalHours(0);
                Map<String, Integer> cells = new LinkedHashMap<>();
                weeks.forEach(w -> cells.put(w.getField(), 0));
                vo.setCells(cells);
                return vo;
            });
            Integer hours = preferHours(item);
            row.setTotalHours(row.getTotalHours() + hours);
            row.getCells().put(meta.getField(), row.getCells().getOrDefault(meta.getField(), 0) + hours);
        }

        TaskWorkItemWeeklyOccupancyVO result = new TaskWorkItemWeeklyOccupancyVO();
        List<TaskWorkItemWeeklyOccupancyColumnVO> columns = Lists.newArrayList();
        TaskWorkItemWeeklyOccupancyColumnVO nameCol = new TaskWorkItemWeeklyOccupancyColumnVO();
        nameCol.setField("name");
        nameCol.setTitleTop(dimension.getNameTitle());
        nameCol.setTitleBottom("");
        columns.add(nameCol);
        for (WeekMeta week : weeks) {
            TaskWorkItemWeeklyOccupancyColumnVO c = new TaskWorkItemWeeklyOccupancyColumnVO();
            c.setField(week.getField());
            c.setTitleTop(week.getTopTitle());
            c.setTitleBottom(week.getBottomTitle());
            columns.add(c);
        }
        TaskWorkItemWeeklyOccupancyColumnVO totalCol = new TaskWorkItemWeeklyOccupancyColumnVO();
        totalCol.setField("totalHours");
        totalCol.setTitleTop("总计");
        totalCol.setTitleBottom("(h)");
        columns.add(totalCol);

        result.setColumns(columns);
        result.setTableData(Lists.newArrayList(rowMap.values()));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResultDTO<?> importExcel(String tenantId, MultipartFile file, Integer startRowNum, Integer startCellNum) throws Exception {
        List<TaskWorkItem> allItems = super.list(Wrappers.lambdaQuery(TaskWorkItem.class)
                .eq(TaskWorkItem::getTenantId, tenantId)
                .select(TaskWorkItem::getId, TaskWorkItem::getProjectNo, TaskWorkItem::getModuleName, TaskWorkItem::getStartDate));
        Map<String, TaskWorkItem> existMap = allItems.stream()
                .collect(Collectors.toMap(item -> buildUniqueKey(item.getProjectNo(), item.getModuleName(), item.getStartDate()), Function.identity(), (a, b) -> a));

        List<Project> allProjects = Optional.ofNullable(projectMapper.selectList(Wrappers.lambdaQuery(Project.class)
                .eq(Project::getTenantId, tenantId)
                .select(Project::getId, Project::getCode, Project::getName)))
                .orElse(Lists.newArrayList());
        Map<String, Project> projectNameMap = allProjects.stream()
                .filter(project -> StrUtil.isNotBlank(project.getName()))
                .collect(Collectors.toMap(Project::getName, Function.identity(), (a, b) -> a));

        List<SimpleStaffDTO> staffs = Optional.ofNullable(umsService.loadSimpleStaffs(tenantId)).orElse(Lists.newArrayList());
        Map<String, SimpleStaffDTO> staffNameMap = staffs.stream()
                .filter(staff -> StrUtil.isNotBlank(staff.getName()) && StrUtil.isNotBlank(staff.getId()))
                .collect(Collectors.toMap(SimpleStaffDTO::getName, Function.identity(), (a, b) -> a));

        List<ExcelImportField> fields = Lists.newArrayList();
        this.buildExcelFields(fields, projectNameMap.keySet(), staffNameMap.keySet());
        ExcelReader excelReader = this.buildExcelReader(file, startRowNum, startCellNum, fields);
        List<ExcelImportRow> rows = excelReader.readRows();

        if (!excelReader.hasError()) {
            this.saveOrUpdateList(tenantId, rows, existMap, projectNameMap, staffNameMap);
            return RestResultDTO.newSuccess(rows.size(), "导入成功");
        }
        String errorFileId = excelReader.writeError();
        RestResultDTO<Object> fail = RestResultDTO.newFail("导入失败");
        fail.setData(errorFileId);
        return fail;
    }

    private void saveOrUpdateList(String tenantId,
                                  List<ExcelImportRow> rows,
                                  Map<String, TaskWorkItem> existMap,
                                  Map<String, Project> projectNameMap,
                                  Map<String, SimpleStaffDTO> staffNameMap) {
        List<TaskWorkItem> entities = Lists.newArrayList();
        for (ExcelImportRow row : rows) {
            TaskWorkItem entity = new TaskWorkItem();
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
                    case "ownerTlName":
                        if (Objects.nonNull(value)) {
                            SimpleStaffDTO staff = staffNameMap.get(value.toString());
                            if (Objects.nonNull(staff)) {
                                entity.setOwnerTlId(staff.getId());
                                entity.setOwnerTlName(staff.getName());
                            }
                        }
                        break;
                    case "moduleName":
                        entity.setModuleName((String) value);
                        break;
                    case "taskDesc":
                        entity.setTaskDesc((String) value);
                        break;
                    case "startDate":
                        if (Objects.nonNull(value)) {
                            entity.setStartDate(LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
                        }
                        break;
                    case "endDate":
                        if (Objects.nonNull(value)) {
                            entity.setEndDate(LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
                        }
                        break;
                    case "estimatedHours":
                        if (Objects.nonNull(value)) {
                            entity.setEstimatedHours(Integer.valueOf(value.toString()));
                        }
                        break;
                    case "ownerName":
                        if (Objects.nonNull(value)) {
                            SimpleStaffDTO staff = staffNameMap.get(value.toString());
                            if (Objects.nonNull(staff)) {
                                entity.setOwnerId(staff.getId());
                                entity.setOwnerName(staff.getName());
                            }
                        }
                        break;
                    case "status":
                        entity.setStatus((String) value);
                        break;
                    case "actualFinishDate":
                        if (Objects.nonNull(value) && StrUtil.isNotBlank(value.toString())) {
                            entity.setActualFinishDate(LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
                        }
                        break;
                    case "actualHours":
                        if (Objects.nonNull(value) && StrUtil.isNotBlank(value.toString())) {
                            entity.setActualHours(Integer.valueOf(value.toString()));
                        }
                        break;
                    case "actualOwnerName":
                        if (Objects.nonNull(value) && StrUtil.isNotBlank(value.toString())) {
                            SimpleStaffDTO staff = staffNameMap.get(value.toString());
                            if (Objects.nonNull(staff)) {
                                entity.setActualOwnerId(staff.getId());
                                entity.setActualOwnerName(staff.getName());
                            }
                        }
                        break;
                    case "progressNote":
                        entity.setProgressNote((String) value);
                        break;
                    default:
                        break;
                }
            }

            TaskWorkItem exist = existMap.get(buildUniqueKey(entity.getProjectNo(), entity.getModuleName(), entity.getStartDate()));
            if (Objects.nonNull(exist)) {
                entity.setId(exist.getId());
            }
            entities.add(entity);
        }

        if (CollectionUtils.isNotEmpty(entities)) {
            this.saveOrUpdateBatch(entities);
        }
    }

    private void buildExcelFields(List<ExcelImportField> fields, Set<String> validProjectNames, Set<String> validStaffNames) {
        fields.add(ExcelImportField.builder().key("projectName").title("项目名称").required(true).dictSet(validProjectNames).build());
        fields.add(ExcelImportField.builder().key("ownerTlName").title("所属TL").required(true).dictSet(validStaffNames).build());
        fields.add(ExcelImportField.builder().key("moduleName").title("模块").required(true)
                .convertFunction((messages, source) -> limitText(source, 100, "模块长度不能超过100", messages)).build());
        fields.add(ExcelImportField.builder().key("taskDesc").title("任务描述").required(true)
                .convertFunction((messages, source) -> limitText(source, 2000, "任务描述长度不能超过2000", messages)).build());
        fields.add(ExcelImportField.builder().key("startDate").title("开始日期").required(true)
                .convertFunction((messages, source) -> normalizeDate(source, "开始日期", messages)).build());
        fields.add(ExcelImportField.builder().key("endDate").title("结束日期").required(true)
                .convertFunction((messages, source) -> normalizeDate(source, "结束日期", messages)).build());
        fields.add(ExcelImportField.builder().key("estimatedHours").title("预计工时")
                .convertFunction((messages, source) -> normalizeNonNegativeInt(source, "预计工时", messages, false)).build());
        fields.add(ExcelImportField.builder().key("ownerName").title("责任人").required(true).dictSet(validStaffNames).build());
        fields.add(ExcelImportField.builder().key("status").title("完成状态").required(true).dictSet(java.util.stream.Stream.of("完成", "延期").collect(Collectors.toSet())).build());
        fields.add(ExcelImportField.builder().key("actualFinishDate").title("实际完成日期")
                .convertFunction((messages, source) -> normalizeDate(source, "实际完成日期", messages)).build());
        fields.add(ExcelImportField.builder().key("actualHours").title("实际工时")
                .convertFunction((messages, source) -> normalizeNonNegativeInt(source, "实际工时", messages, false)).build());
        fields.add(ExcelImportField.builder().key("actualOwnerName").title("实际完成人").dictSet(validStaffNames).build());
        fields.add(ExcelImportField.builder().key("progressNote").title("任务进度跟进描述")
                .convertFunction((messages, source) -> limitText(source, 2000, "任务进度跟进描述长度不能超过2000", messages)).build());
    }

    private Object normalizeDate(Object source, String field, List<String> messages) {
        if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
            return null;
        }
        String dateString = source instanceof Date
                ? DateUtil.format((Date) source, DatePattern.NORM_DATE_PATTERN)
                : source.toString().trim();
        try {
            String normalized = DateUtil.format(DateUtil.parse(dateString, DatePattern.NORM_DATE_PATTERN), DatePattern.NORM_DATE_PATTERN);
            if (!dateString.equals(normalized)) {
                messages.add(field + "格式错误，必须为yyyy-MM-dd");
                return null;
            }
            return normalized;
        } catch (Exception e) {
            messages.add(field + "格式错误，必须为yyyy-MM-dd");
            return null;
        }
    }

    private Object normalizeNonNegativeInt(Object source, String field, List<String> messages, boolean required) {
        if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
            if (required) {
                messages.add(field + "不能为空");
            }
            return null;
        }
        String value = source.toString().trim();
        try {
            Integer intValue = Integer.valueOf(value);
            if (intValue < 0) {
                messages.add(field + "必须大于等于0");
                return null;
            }
            return intValue.toString();
        } catch (Exception e) {
            messages.add(field + "必须为整数");
            return null;
        }
    }

    private Object limitText(Object source, int maxLen, String message, List<String> messages) {
        if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
            return null;
        }
        String text = source.toString().trim();
        if (text.length() > maxLen) {
            messages.add(message);
            return null;
        }
        return text;
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

    private void checkDataAndFillAssociation(TaskWorkItemDTO dto) {
        Assert.hasText(dto.getTenantId(), "租户ID不能为空");
        Assert.isTrue(Objects.isNull(dto.getEstimatedHours()) || dto.getEstimatedHours() >= 0, "预计工时必须大于等于0");
        Assert.isTrue(Objects.isNull(dto.getActualHours()) || dto.getActualHours() >= 0, "实际工时必须大于等于0");
        Assert.isTrue(!dto.getEndDate().isBefore(dto.getStartDate()), "结束日期不能早于开始日期");

        Project project = projectMapper.selectOne(Wrappers.lambdaQuery(Project.class)
                .eq(Project::getTenantId, dto.getTenantId())
                .eq(Project::getId, dto.getProjectId())
                .last("limit 1"));
        Assert.notNull(project, "项目不存在");
        dto.setProjectNo(project.getCode());
        dto.setProjectName(project.getName());

        List<SimpleStaffDTO> staffs = Optional.ofNullable(umsService.loadSimpleStaffs(dto.getTenantId())).orElse(Lists.newArrayList());
        Map<String, SimpleStaffDTO> staffMap = staffs.stream()
                .filter(staff -> StrUtil.isNotBlank(staff.getId()))
                .collect(Collectors.toMap(SimpleStaffDTO::getId, Function.identity(), (a, b) -> a));

        SimpleStaffDTO tl = staffMap.get(dto.getOwnerTlId());
        Assert.notNull(tl, "所属TL不存在");
        dto.setOwnerTlName(tl.getName());

        SimpleStaffDTO owner = staffMap.get(dto.getOwnerId());
        Assert.notNull(owner, "责任人不存在");
        dto.setOwnerName(owner.getName());

        if (StrUtil.isNotBlank(dto.getActualOwnerId())) {
            SimpleStaffDTO actualOwner = staffMap.get(dto.getActualOwnerId());
            Assert.notNull(actualOwner, "实际完成人不存在");
            dto.setActualOwnerName(actualOwner.getName());
        } else {
            dto.setActualOwnerName(null);
        }
    }

    private QueryWrapper<TaskWorkItem> buildQuery(TaskWorkItemQueryDTO queryDTO) {
        QueryWrapper<TaskWorkItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getTenantId()), TaskWorkItem::getTenantId, queryDTO.getTenantId());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getProjectId()), TaskWorkItem::getProjectId, queryDTO.getProjectId());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getOwnerTlId()), TaskWorkItem::getOwnerTlId, queryDTO.getOwnerTlId());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getStatus()), TaskWorkItem::getStatus, queryDTO.getStatus());
        queryWrapper.lambda().ge(Objects.nonNull(queryDTO.getStartDateBegin()), TaskWorkItem::getStartDate, queryDTO.getStartDateBegin());
        queryWrapper.lambda().le(Objects.nonNull(queryDTO.getStartDateEnd()), TaskWorkItem::getStartDate, queryDTO.getStartDateEnd());
        queryWrapper.lambda().in(CollUtil.isNotEmpty(queryDTO.getIds()), TaskWorkItem::getId, queryDTO.getIds());
        return queryWrapper;
    }

    private void fillDefaultDateRange(TaskWorkItemQueryDTO queryDTO) {
        if (Objects.nonNull(queryDTO.getStartDateBegin()) && Objects.nonNull(queryDTO.getStartDateEnd())) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (Objects.isNull(queryDTO.getStartDateBegin())) {
            queryDTO.setStartDateBegin(LocalDate.of(today.getYear(), 1, 1));
        }
        if (Objects.isNull(queryDTO.getStartDateEnd())) {
            queryDTO.setStartDateEnd(today);
        }
    }

    private Integer preferHours(TaskWorkItem item) {
        if (Objects.nonNull(item.getActualHours())) {
            return Math.max(item.getActualHours(), 0);
        }
        if (Objects.nonNull(item.getEstimatedHours())) {
            return Math.max(item.getEstimatedHours(), 0);
        }
        return 0;
    }

    private String getOccupancyRowId(TaskWorkItem item, OccupancyDimension dimension) {
        if (dimension == OccupancyDimension.PROJECT) {
            return item.getProjectId();
        }
        return item.getOwnerId();
    }

    private String getOccupancyRowName(TaskWorkItem item, OccupancyDimension dimension, String fallback) {
        if (dimension == OccupancyDimension.PROJECT) {
            return StrUtil.blankToDefault(item.getProjectName(), fallback);
        }
        return StrUtil.blankToDefault(item.getOwnerName(), fallback);
    }

    private List<WeekMeta> buildWeekMetas(LocalDate begin, LocalDate end) {
        List<WeekMeta> weekMetas = Lists.newArrayList();
        LocalDate cursor = begin.with(DayOfWeek.MONDAY);
        LocalDate tail = end.with(DayOfWeek.SUNDAY);
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);
        while (!cursor.isAfter(tail)) {
            LocalDate weekEnd = cursor.plusDays(6);
            int weekYear = weekEnd.getYear();
            int weekNo = weekEnd.get(weekFields.weekOfYear());
            WeekMeta meta = new WeekMeta();
            meta.setRangeKey(cursor + "~" + weekEnd);
            meta.setField("week_" + cursor.format(DateTimeFormatter.BASIC_ISO_DATE));
            meta.setTopTitle(weekYear + "年" + formatChineseWeekNo(weekNo));
            meta.setBottomTitle(formatMonthDay(cursor) + "-" + formatMonthDay(weekEnd));
            weekMetas.add(meta);
            cursor = cursor.plusWeeks(1);
        }
        return weekMetas;
    }

    private String toWeekKey(LocalDate date) {
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        return monday + "~" + sunday;
    }

    private String formatMonthDay(LocalDate date) {
        return date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
    }

    private String formatChineseWeekNo(int weekNo) {
        String[] chinese = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        if (weekNo <= 10) {
            return "第" + chinese[weekNo] + "周";
        }
        return "第" + weekNo + "周";
    }

    private static class WeekMeta {
        private String rangeKey;
        private String field;
        private String topTitle;
        private String bottomTitle;

        public String getRangeKey() {
            return rangeKey;
        }

        public void setRangeKey(String rangeKey) {
            this.rangeKey = rangeKey;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getTopTitle() {
            return topTitle;
        }

        public void setTopTitle(String topTitle) {
            this.topTitle = topTitle;
        }

        public String getBottomTitle() {
            return bottomTitle;
        }

        public void setBottomTitle(String bottomTitle) {
            this.bottomTitle = bottomTitle;
        }
    }

    private enum OccupancyDimension {
        OWNER("责任人"),
        PROJECT("项目名称");

        private final String nameTitle;

        OccupancyDimension(String nameTitle) {
            this.nameTitle = nameTitle;
        }

        public String getNameTitle() {
            return nameTitle;
        }
    }

    private List<TaskWorkItemVO> transferFromEntity(List<TaskWorkItem> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return Lists.newArrayList();
        }
        return entities.stream().map(this::transferFromEntity).collect(Collectors.toList());
    }

    private TaskWorkItemVO transferFromEntity(TaskWorkItem entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        TaskWorkItemVO vo = new TaskWorkItemVO();
        ENTITY_TO_VO.copy(entity, vo, null);
        return vo;
    }

    private TaskWorkItem transferToEntity(TaskWorkItem entity, TaskWorkItemDTO dto) {
        if (Objects.isNull(entity)) {
            entity = new TaskWorkItem();
        }
        DTO_TO_ENTITY.copy(dto, entity, null);
        return entity;
    }

    private String buildUniqueKey(String projectNo, String moduleName, LocalDate startDate) {
        return StrUtil.format("{}##{}##{}",
                StrUtil.blankToDefault(projectNo, ""),
                StrUtil.blankToDefault(moduleName, ""),
                Objects.nonNull(startDate) ? startDate.toString() : "");
    }
}
