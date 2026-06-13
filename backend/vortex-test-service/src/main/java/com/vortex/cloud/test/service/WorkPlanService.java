package com.vortex.cloud.test.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vortex.cloud.test.domain.WorkPlan;
import com.vortex.cloud.test.dto.WorkPlanDTO;
import com.vortex.cloud.test.dto.WorkPlanQueryDTO;
import com.vortex.cloud.test.dto.WorkPlanVO;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * 工作计划服务
 */
public interface WorkPlanService extends IService<WorkPlan> {

    DataStoreDTO<WorkPlanVO> page(Pageable pageable, WorkPlanQueryDTO queryDTO);

    List<WorkPlanVO> list(Sort sort, WorkPlanQueryDTO queryDTO);

    void save(WorkPlanDTO dto);

    void update(WorkPlanDTO dto);

    void delete(Set<String> ids);

    WorkPlanVO get(String id);

    RestResultDTO<?> importExcel(String tenantId, MultipartFile file, Integer startRowNum, Integer startCellNum) throws Exception;
}
