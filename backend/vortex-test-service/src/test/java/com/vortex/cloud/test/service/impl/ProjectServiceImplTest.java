package com.vortex.cloud.test.service.impl;

import com.vortex.cloud.sdk.api.dto.ums.SimpleStaffDTO;
import com.vortex.cloud.sdk.api.service.IUmsService;
import com.vortex.cloud.test.domain.Project;
import com.vortex.cloud.test.dto.ProjectDTO;
import com.vortex.cloud.test.dto.ProjectQueryDTO;
import com.vortex.cloud.test.mapper.ProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ProjectServiceImplTest {

    @InjectMocks
    private ProjectServiceImpl service;
    @Mock
    private IUmsService umsService;
    @Mock
    private ProjectMapper projectMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", projectMapper);
    }

    @Test
    void save_whenTlInvalid_shouldThrow() {
        ProjectDTO dto = buildValidDto();
        when(umsService.loadSimpleStaffs("tenant-1")).thenReturn(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(dto));
        assertEquals("TL人员不存在", ex.getMessage());
        verify(projectMapper, never()).insert(any(Project.class));
    }

    @Test
    void save_whenCodeInvalid_shouldThrow() {
        ProjectDTO dto = buildValidDto();
        dto.setCode("A#1");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(dto));
        assertEquals("编号格式不正确，仅支持字母、数字、下划线、中划线", ex.getMessage());
    }

    @Test
    void save_success_shouldFillTlName() {
        ProjectDTO dto = buildValidDto();
        SimpleStaffDTO staff = new SimpleStaffDTO();
        staff.setId("s1");
        staff.setName("张三");
        when(umsService.loadSimpleStaffs("tenant-1")).thenReturn(List.of(staff));
        when(projectMapper.selectCount(any())).thenReturn(0L);
        when(projectMapper.insert(any(Project.class))).thenReturn(1);

        service.save(dto);

        assertEquals("张三", dto.getTlName());
        verify(projectMapper).insert(any(Project.class));
    }

    @Test
    void update_whenIdEmpty_shouldThrow() {
        ProjectDTO dto = buildValidDto();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.update(dto));
        assertEquals("ID不能为空", ex.getMessage());
    }

    @Test
    void page_shouldRequireTenantId() {
        ProjectQueryDTO queryDTO = new ProjectQueryDTO();
        queryDTO.setTenantId("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.page(PageRequest.of(0, 10), queryDTO));
        assertEquals("租户ID不能为空", ex.getMessage());
    }

    @Test
    void list_shouldRequireTenantId() {
        ProjectQueryDTO queryDTO = new ProjectQueryDTO();
        queryDTO.setTenantId("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.list(Sort.unsorted(), queryDTO));
        assertEquals("租户ID不能为空", ex.getMessage());
    }

    private ProjectDTO buildValidDto() {
        ProjectDTO dto = new ProjectDTO();
        dto.setTenantId("tenant-1");
        dto.setCode("P_001");
        dto.setName("项目A");
        dto.setTlId("s1");
        return dto;
    }
}
