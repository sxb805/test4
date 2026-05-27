package com.vortex.cloud.test.controller;

import com.vortex.cloud.test.dto.ProjectQueryDTO;
import com.vortex.cloud.test.dto.ProjectVO;
import com.vortex.cloud.test.service.ProjectService;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @InjectMocks
    private ProjectController controller;

    @Mock
    private ProjectService projectService;

    private ProjectVO vo;

    @BeforeEach
    void setUp() {
        vo = new ProjectVO();
        vo.setId("p1");
        vo.setCode("P001");
        vo.setName("项目一");
        vo.setTlId("s1");
        vo.setTlName("张三");
    }

    @Test
    void page_shouldReturnData() {
        DataStoreDTO<ProjectVO> store = new DataStoreDTO<>(1L, List.of(vo));
        when(projectService.page(any(Pageable.class), any(ProjectQueryDTO.class))).thenReturn(store);

        var result = controller.page(PageRequest.of(0, 10), "t1", "u1", new ProjectQueryDTO());

        assertEquals(0, result.getResult());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getTotal());
        assertEquals("P001", result.getData().getRows().get(0).getCode());
    }

    @Test
    void list_shouldReturnData() {
        when(projectService.list(any(Sort.class), any(ProjectQueryDTO.class))).thenReturn(List.of(vo));

        var result = controller.list(Sort.by(Sort.Order.desc("createTime")), "t1", "u1", new ProjectQueryDTO());

        assertEquals(0, result.getResult());
        assertEquals(1, result.getData().size());
        assertEquals("项目一", result.getData().get(0).getName());
    }

    @Test
    void get_shouldReturnData() {
        when(projectService.get("p1")).thenReturn(vo);

        var result = controller.get("p1");

        assertEquals(0, result.getResult());
        assertEquals("张三", result.getData().getTlName());
    }
}
