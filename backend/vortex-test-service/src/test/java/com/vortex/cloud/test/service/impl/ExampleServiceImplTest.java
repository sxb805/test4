package com.vortex.cloud.test.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vortex.cloud.sdk.api.dto.ums.ParamSettingDTO;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.vortex.cloud.test.domain.Example;
import com.vortex.cloud.test.dto.ExampleDTO;
import com.vortex.cloud.test.dto.ExampleQueryDTO;
import com.vortex.cloud.test.dto.ExampleVO;
import com.vortex.cloud.test.mapper.ExampleMapper;
import com.vortex.cloud.sdk.api.service.IUmsService;
import com.vortex.cloud.test.support.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ExampleServiceImplTest {

    @InjectMocks
    private ExampleServiceImpl service;
    @Mock
    private IUmsService umsService;
    @Mock
    private ExampleMapper exampleMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", exampleMapper);
    }

    @Test
    void save_success_shouldInsert() {
        ExampleDTO dto = buildValidDto();
        when(exampleMapper.selectCount(any())).thenReturn(0L);
        when(exampleMapper.insert(any(Example.class))).thenReturn(1);

        service.save(dto);

        ArgumentCaptor<Example> captor = ArgumentCaptor.forClass(Example.class);
        verify(exampleMapper).insert(captor.capture());
        assertEquals("tenant-1", captor.getValue().getTenantId());
        assertEquals("C001", captor.getValue().getCode());
        assertEquals("名称A", captor.getValue().getName());
    }

    @Test
    void save_withEmptyTenantId_shouldThrowAndNeverCallMapper() {
        ExampleDTO dto = buildValidDto();
        dto.setTenantId("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(dto));
        assertEquals("租户ID不能为空", ex.getMessage());
        verifyNoInteractions(exampleMapper);
    }

    @Test
    void save_withInvalidDateType_shouldThrowAndNeverCallMapper() {
        ExampleDTO dto = buildValidDto();
        dto.setDateType("INVALID");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(dto));
        assertEquals("日期类型：INVALID不存在", ex.getMessage());
        verifyNoInteractions(exampleMapper);
    }

    @Test
    void save_whenCodeExists_shouldThrowIllegalArgumentException() {
        ExampleDTO dto = buildValidDto();
        when(exampleMapper.selectCount(any())).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(dto));
        assertEquals("编码为C001的记录已存在", ex.getMessage());
        verify(exampleMapper, never()).insert(any(Example.class));
    }

    @Test
    void update_whenRecordNotFound_shouldThrowIllegalArgumentException() {
        ExampleDTO dto = buildValidDto();
        dto.setId("id-1");
        when(exampleMapper.selectCount(any())).thenReturn(0L);
        when(exampleMapper.selectById("id-1")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.update(dto));
        assertEquals("找不到id为id-1 的记录", ex.getMessage());
        verify(exampleMapper, never()).updateById(any(Example.class));
    }

    @Test
    void update_success_shouldUpdateById() {
        ExampleDTO dto = buildValidDto();
        dto.setId("id-2");
        Example origin = new Example();
        origin.setId("id-2");
        origin.setTenantId("tenant-1");
        when(exampleMapper.selectCount(any())).thenReturn(0L);
        when(exampleMapper.selectById("id-2")).thenReturn(origin);
        when(exampleMapper.updateById(any(Example.class))).thenReturn(1);

        service.update(dto);

        ArgumentCaptor<Example> captor = ArgumentCaptor.forClass(Example.class);
        verify(exampleMapper).updateById(captor.capture());
        assertEquals("id-2", captor.getValue().getId());
        assertEquals("C001", captor.getValue().getCode());
        assertEquals("名称A", captor.getValue().getName());
    }

    @Test
    void update_withEmptyId_shouldThrowAndNeverCallMapper() {
        ExampleDTO dto = buildValidDto();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.update(dto));
        assertEquals("ID不能为空", ex.getMessage());
        verifyNoInteractions(exampleMapper);
    }

    @Test
    void save_whenManagerStaffNotExists_shouldThrowAndNeverCallMapper() {
        ExampleDTO dto = buildValidDto();
        dto.setManagerStaffId("staff-1");
        when(umsService.loadSimpleStaffs("tenant-1")).thenReturn(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(dto));
        assertEquals("管理人员不存在", ex.getMessage());
        verify(exampleMapper, never()).insert(any(Example.class));
    }

    @Test
    void save_whenTypeNotExists_shouldThrowAndNeverCallMapper() {
        ExampleDTO dto = buildValidDto();
        dto.setType("T01");
        when(umsService.getByParamTypeCode("tenant-1", Constants.PARAM_TYPE_EXAMPLE_TYPE)).thenReturn(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(dto));
        assertEquals("类型编码：T01不存在", ex.getMessage());
        verify(exampleMapper, never()).insert(any(Example.class));
    }

    @Test
    void page_withEmptyTenantId_shouldThrowAndNeverCallMapper() {
        ExampleQueryDTO queryDTO = new ExampleQueryDTO();
        queryDTO.setTenantId("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.page(PageRequest.of(0, 10), queryDTO));
        assertEquals("租户ID不能为空", ex.getMessage());
        verifyNoInteractions(exampleMapper);
    }

    @Test
    void page_success_withEmptyRecords_shouldReturnEmptyDataStore() {
        ExampleQueryDTO queryDTO = new ExampleQueryDTO();
        queryDTO.setTenantId("tenant-1");
        when(exampleMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.page(PageRequest.of(0, 10), queryDTO);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertEquals(0, result.getRows().size());
    }

    @Test
    void list_withEmptyTenantId_shouldThrowAndNeverCallMapper() {
        ExampleQueryDTO queryDTO = new ExampleQueryDTO();
        queryDTO.setTenantId("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.list(Sort.unsorted(), queryDTO));
        assertEquals("租户ID不能为空", ex.getMessage());
        verifyNoInteractions(exampleMapper);
    }

    @Test
    void list_success_withEmptyRecords_shouldReturnEmptyList() {
        ExampleQueryDTO queryDTO = new ExampleQueryDTO();
        queryDTO.setTenantId("tenant-1");
        when(exampleMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<ExampleVO> result = service.list(Sort.unsorted(), queryDTO);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void get_withEmptyId_shouldThrowAndNeverCallMapper() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.get(""));
        assertEquals("ID不能为空", ex.getMessage());
        verifyNoInteractions(exampleMapper);
    }

    @Test
    void get_whenRecordNotFound_shouldThrow() {
        when(exampleMapper.selectById("id-404")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.get("id-404"));
        assertEquals("找不到ID为id-404 的记录", ex.getMessage());
    }

    @Test
    void get_success_shouldReturnVo() {
        Example entity = new Example();
        entity.setId("id-1");
        entity.setTenantId("tenant-1");
        entity.setCode("C001");
        entity.setName("名称A");
        when(exampleMapper.selectById("id-1")).thenReturn(entity);
        when(umsService.loadSimpleStaffs("tenant-1")).thenReturn(Collections.emptyList());
        when(umsService.getByParamTypeCode("tenant-1", Constants.PARAM_TYPE_EXAMPLE_TYPE)).thenReturn(Collections.emptyList());

        ExampleVO vo = service.get("id-1");

        assertEquals("id-1", vo.getId());
        assertEquals("C001", vo.getCode());
        assertEquals("名称A", vo.getName());
    }

    @Test
    void delete_withEmptyIds_shouldNotCallMapper() {
        service.delete(Collections.emptySet());
        verifyNoInteractions(exampleMapper);
    }

    @Test
    void delete_withIds_shouldCallMapper() {
        Set<String> ids = new HashSet<>();
        ids.add("id-1");
        ids.add("id-2");
        when(exampleMapper.deleteByIds(eq(ids))).thenReturn(2);

        service.delete(ids);

        verify(exampleMapper).deleteByIds(eq(ids));
    }

    @Test
    void exist_withEmptyTenantId_shouldThrowAndNeverCallMapper() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.exist("", null, "code", "C001"));
        assertEquals("租户ID不能为空", ex.getMessage());
        verifyNoInteractions(exampleMapper);
    }

    @Test
    void exist_withEmptyKey_shouldThrowAndNeverCallMapper() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.exist("tenant-1", null, "", "C001"));
        assertEquals("字段名不能为空", ex.getMessage());
        verifyNoInteractions(exampleMapper);
    }

    @Test
    void exist_whenCountIsOne_shouldReturnTrue() {
        when(exampleMapper.selectCount(any())).thenReturn(1L);

        Boolean result = service.exist("tenant-1", null, "code", "C001");

        assertTrue(result);
    }

    @Test
    void exist_whenCountIsZero_shouldReturnFalse() {
        when(exampleMapper.selectCount(any())).thenReturn(0L);

        Boolean result = service.exist("tenant-1", "id-1", "code", "C001");

        assertFalse(result);
    }

    @Test
    void importExcel_inPureUnitTest_shouldThrowMybatisPlusException() {
        assertThrows(MybatisPlusException.class, () -> service.importExcel("tenant-1", null, 1, 1));
    }

    private ExampleDTO buildValidDto() {
        ExampleDTO dto = new ExampleDTO();
        dto.setTenantId("tenant-1");
        dto.setCode("C001");
        dto.setName("名称A");
        return dto;
    }
}
