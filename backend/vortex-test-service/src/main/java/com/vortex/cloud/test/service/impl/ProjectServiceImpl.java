package com.vortex.cloud.test.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.vortex.cloud.sdk.api.dto.ums.SimpleStaffDTO;
import com.vortex.cloud.sdk.api.service.IUmsService;
import com.vortex.cloud.test.domain.Project;
import com.vortex.cloud.test.dto.ProjectDTO;
import com.vortex.cloud.test.dto.ProjectQueryDTO;
import com.vortex.cloud.test.dto.ProjectVO;
import com.vortex.cloud.test.enums.ProjectTypeEnum;
import com.vortex.cloud.test.mapper.ProjectMapper;
import com.vortex.cloud.test.service.ProjectService;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import com.vortex.cloud.vfs.lite.base.excel.ExcelImportCell;
import com.vortex.cloud.vfs.lite.base.excel.ExcelImportField;
import com.vortex.cloud.vfs.lite.base.excel.ExcelImportRow;
import com.vortex.cloud.vfs.lite.base.excel.ExcelReader;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 项目服务实现
 */
@Transactional(readOnly = true)
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private static final BeanCopier ENTITY_TO_VO = BeanCopier.create(Project.class, ProjectVO.class, false);
    private static final BeanCopier DTO_TO_ENTITY = BeanCopier.create(ProjectDTO.class, Project.class, false);
    private static final Set<String> PROJECT_TYPE_NAMES = ProjectTypeEnum.valueSet();

    @Resource
    private IUmsService umsService;

    @Override
    public DataStoreDTO<ProjectVO> page(Pageable pageable, ProjectQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        QueryWrapper<Project> queryWrapper = this.buildQuery(queryDTO);
        Page<Project> page = PageUtils.transferPage(pageable);
        Page<Project> result = this.page(page, queryWrapper);
        return new DataStoreDTO<>(result.getTotal(), this.transferFromEntity(result.getRecords()));
    }

    @Override
    public List<ProjectVO> list(Sort sort, ProjectQueryDTO queryDTO) {
        Assert.hasText(queryDTO.getTenantId(), "租户ID不能为空");
        QueryWrapper<Project> queryWrapper = this.buildQuery(queryDTO);
        PageUtils.transferSort(queryWrapper, sort);
        return this.transferFromEntity(this.list(queryWrapper));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(ProjectDTO dto) {
        this.checkDataAndFillTlName(dto);
        Assert.isTrue(!this.exist(dto.getTenantId(), dto.getId(), "code", dto.getCode()), "编号为" + dto.getCode() + "的记录已存在");
        this.save(this.transferToEntity(null, dto));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(ProjectDTO dto) {
        Assert.hasText(dto.getId(), "ID不能为空");
        this.checkDataAndFillTlName(dto);
        Assert.isTrue(!this.exist(dto.getTenantId(), dto.getId(), "code", dto.getCode()), "编号为" + dto.getCode() + "的记录已存在");
        Project entity = this.getById(dto.getId());
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
    public ProjectVO get(String id) {
        Assert.hasText(id, "ID不能为空");
        Project entity = this.getById(id);
        Assert.notNull(entity, "找不到ID为" + id + " 的记录");
        return this.transferFromEntity(entity);
    }

    @Override
    public Boolean exist(String tenantId, String id, String key, String value) {
        Assert.hasText(tenantId, "租户ID不能为空");
        Assert.hasText(key, "字段名不能为空");

        QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Project::getTenantId, tenantId);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.lambda().ne(Project::getId, id);
        }
        queryWrapper.eq(StrUtil.toUnderlineCase(key), value);
        return this.count(queryWrapper) >= 1;
    }

    private QueryWrapper<Project> buildQuery(ProjectQueryDTO queryDTO) {
        QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getTenantId()), Project::getTenantId, queryDTO.getTenantId());
        queryWrapper.lambda().like(StrUtil.isNotBlank(queryDTO.getCode()), Project::getCode, queryDTO.getCode());
        queryWrapper.lambda().like(StrUtil.isNotBlank(queryDTO.getName()), Project::getName, queryDTO.getName());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getType()), Project::getType, queryDTO.getType());
        queryWrapper.lambda().eq(StrUtil.isNotBlank(queryDTO.getTlId()), Project::getTlId, queryDTO.getTlId());
        queryWrapper.lambda().in(CollUtil.isNotEmpty(queryDTO.getIds()), Project::getId, queryDTO.getIds());
        return queryWrapper;
    }

    private void checkDataAndFillTlName(ProjectDTO dto) {
        Assert.hasText(dto.getTenantId(), "租户ID不能为空");
        Assert.hasText(dto.getCode(), "编号不能为空");
        Assert.isTrue(dto.getCode().length() <= 32, "编号长度不能超过32");
        Assert.isTrue(dto.getCode().matches("^[A-Za-z0-9_-]+$"), "编号格式不正确，仅支持字母、数字、下划线、中划线");
        Assert.hasText(dto.getName(), "名称不能为空");
        Assert.isTrue(dto.getName().length() <= 100, "名称长度不能超过100");
        Assert.hasText(dto.getType(), "类型不能为空");
        Assert.isTrue(ProjectTypeEnum.containsKey(dto.getType()), "类型必须为项目或产品");
        Assert.hasText(dto.getTlId(), "TL人员ID不能为空");

        List<SimpleStaffDTO> simpleStaffList = Optional.ofNullable(this.umsService.loadSimpleStaffs(dto.getTenantId()))
                .orElse(Lists.newArrayList());
        Map<String, SimpleStaffDTO> staffMap = simpleStaffList.stream()
                .filter(staff -> StrUtil.isNotBlank(staff.getId()))
                .collect(Collectors.toMap(SimpleStaffDTO::getId, Function.identity(), (a, b) -> a));
        SimpleStaffDTO tlStaff = staffMap.get(dto.getTlId());
        Assert.notNull(tlStaff, "TL人员不存在");
        Assert.hasText(tlStaff.getName(), "TL人员名称不存在");
        dto.setTlName(tlStaff.getName());
    }

    private List<ProjectVO> transferFromEntity(List<Project> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return Lists.newArrayList();
        }
        return entities.stream().map(this::transferFromEntity).collect(Collectors.toList());
    }

    private ProjectVO transferFromEntity(Project entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        ProjectVO vo = new ProjectVO();
        ENTITY_TO_VO.copy(entity, vo, null);
        ProjectTypeEnum projectType = ProjectTypeEnum.getByKey(entity.getType());
        if (Objects.nonNull(projectType)) {
            vo.setTypeName(projectType.getValue());
        }
        return vo;
    }

    private Project transferToEntity(Project entity, ProjectDTO dto) {
        if (Objects.isNull(entity)) {
            entity = new Project();
        }
        DTO_TO_ENTITY.copy(dto, entity, null);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResultDTO<?> importExcel(String tenantId, MultipartFile file, Integer startRowNum, Integer startCellNum) throws Exception {
        List<Project> allProjects = super.list(Wrappers.lambdaQuery(Project.class)
                .eq(Project::getTenantId, tenantId)
                .select(Project::getId, Project::getCode));
        Map<String, Project> existProjects = allProjects.stream()
                .collect(Collectors.toMap(Project::getCode, Function.identity(), (a, b) -> a));

        List<SimpleStaffDTO> simpleStaffDTOS = Optional.ofNullable(umsService.loadSimpleStaffs(tenantId))
                .orElse(Lists.newArrayList());
        Map<String, String> staffNameToIdMap = simpleStaffDTOS.stream()
                .filter(staff -> StrUtil.isNotBlank(staff.getName()) && StrUtil.isNotBlank(staff.getId()))
                .collect(Collectors.toMap(SimpleStaffDTO::getName, SimpleStaffDTO::getId, (a, b) -> a));

        List<ExcelImportField> fields = Lists.newArrayList();
        this.buildExcelFields(fields, existProjects.keySet(), staffNameToIdMap.keySet());
        ExcelReader excelReader = this.buildExcelReader(file, startRowNum, startCellNum, fields);
        List<ExcelImportRow> excelImportRows = excelReader.readRows();

        if (!excelReader.hasError()) {
            this.saveOrUpdateList(tenantId, excelImportRows, existProjects, staffNameToIdMap);
            return RestResultDTO.newSuccess(excelImportRows.size(), "导入成功");
        }

        String errorFileId = excelReader.writeError();
        RestResultDTO<Object> fail = RestResultDTO.newFail("导入失败");
        fail.setData(errorFileId);
        return fail;
    }

    private void saveOrUpdateList(String tenantId,
                                  List<ExcelImportRow> excelImportRows,
                                  Map<String, Project> existProjects,
                                  Map<String, String> staffNameToIdMap) {
        List<Project> projects = Lists.newArrayList();
        for (ExcelImportRow row : excelImportRows) {
            Project project = new Project();
            project.setTenantId(tenantId);

            for (ExcelImportCell cell : row.getCells()) {
                switch (cell.getField().getKey()) {
                    case "code":
                        project.setCode((String) cell.getTargetValue());
                        break;
                    case "name":
                        project.setName((String) cell.getTargetValue());
                        break;
                    case "type":
                        project.setType((String) cell.getTargetValue());
                        break;
                    case "tlName":
                        if (Objects.nonNull(cell.getTargetValue())) {
                            String tlName = cell.getTargetValue().toString();
                            project.setTlName(tlName);
                            project.setTlId(staffNameToIdMap.get(tlName));
                        }
                        break;
                    default:
                        break;
                }
            }
            Project existProject = existProjects.get(project.getCode());
            if (Objects.nonNull(existProject)) {
                project.setId(existProject.getId());
            }
            projects.add(project);
        }

        if (CollectionUtils.isNotEmpty(projects)) {
            this.saveOrUpdateBatch(projects);
        }
    }

    private void buildExcelFields(List<ExcelImportField> fields, Set<String> existCodes, Set<String> validTlNames) {
        fields.add(ExcelImportField.builder().key("code").title("编号").required(true).unique(true).uniqueSet(existCodes)
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String value = source.toString().trim();
                    if (value.length() > 32) {
                        messages.add("编号长度不能超过32");
                        return null;
                    }
                    if (!value.matches("^[A-Za-z0-9_-]+$")) {
                        messages.add("编号格式不正确，仅支持字母、数字、下划线、中划线");
                        return null;
                    }
                    return value;
                }).build());
        fields.add(ExcelImportField.builder().key("name").title("名称").required(true)
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String value = source.toString().trim();
                    if (value.length() > 100) {
                        messages.add("名称长度不能超过100");
                        return null;
                    }
                    return value;
                }).build());
        fields.add(ExcelImportField.builder().key("type").title("类型").required(true).dictSet(PROJECT_TYPE_NAMES)
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String value = source.toString().trim();
                    ProjectTypeEnum projectType = ProjectTypeEnum.getByValue(value);
                    if (Objects.isNull(projectType)) {
                        messages.add("必须为项目或产品");
                        return null;
                    }
                    return projectType.getKey();
                }).build());
        fields.add(ExcelImportField.builder().key("tlName").title("TL").required(true).dictSet(validTlNames)
                .convertFunction((messages, source) -> {
                    if (Objects.isNull(source) || StrUtil.isBlank(source.toString())) {
                        return null;
                    }
                    String value = source.toString().trim();
                    if (value.length() > 50) {
                        messages.add("TL名称长度不能超过50");
                        return null;
                    }
                    return value;
                }).build());
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
}
