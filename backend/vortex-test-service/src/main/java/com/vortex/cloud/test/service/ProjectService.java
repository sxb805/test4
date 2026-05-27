package com.vortex.cloud.test.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vortex.cloud.test.domain.Project;
import com.vortex.cloud.test.dto.ProjectDTO;
import com.vortex.cloud.test.dto.ProjectQueryDTO;
import com.vortex.cloud.test.dto.ProjectVO;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * 项目服务
 */
public interface ProjectService extends IService<Project> {

    DataStoreDTO<ProjectVO> page(Pageable pageable, ProjectQueryDTO queryDTO);

    List<ProjectVO> list(Sort sort, ProjectQueryDTO queryDTO);

    void save(ProjectDTO dto);

    void update(ProjectDTO dto);

    void delete(Set<String> ids);

    ProjectVO get(String id);

    Boolean exist(String tenantId, String id, String key, String value);

    RestResultDTO<?> importExcel(String tenantId, MultipartFile file, Integer startRowNum, Integer startCellNum) throws Exception;
}
